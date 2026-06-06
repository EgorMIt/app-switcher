#!/usr/bin/env python3
"""
Генерация PNG иконок и баннеров из SVG (tools/icons/) для Android TV.
Требуется rsvg-convert: brew install librsvg
"""

from __future__ import annotations

import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
APPLETV_RES = ROOT / "app" / "src" / "appletv" / "res"
PS5_RES = ROOT / "app" / "src" / "ps5" / "res"
ICONS = ROOT / "tools" / "icons"

SVG_PS5 = ICONS / "icons8-playstation.svg"
SVG_APPLE = ICONS / "icons8-apple-tv.svg"

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

RSVG = shutil.which("rsvg-convert") or "/usr/local/bin/rsvg-convert"


def rsvg_png(svg: Path, width: int, height: int) -> Image.Image:
    if not svg.is_file():
        raise FileNotFoundError(svg)
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        out = Path(tmp.name)
    try:
        subprocess.run(
            [RSVG, "-w", str(width), "-h", str(height), str(svg), "-o", str(out)],
            check=True,
            capture_output=True,
            text=True,
        )
        return Image.open(out).convert("RGBA")
    finally:
        out.unlink(missing_ok=True)


def icon_ps5(size: int) -> Image.Image:
    """Синий круг + белый логотип из SVG."""
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    pad = max(2, size // 26)
    draw.ellipse(
        (pad, pad, size - pad - 1, size - pad - 1),
        fill=(13, 71, 161, 255),
    )
    logo_px = max(int(size * 0.52), 16)
    fg = rsvg_png(SVG_PS5, logo_px, logo_px)
    x = (size - fg.width) // 2
    y = (size - fg.height) // 2
    canvas.alpha_composite(fg, (x, y))
    return canvas


def icon_appletv(size: int) -> Image.Image:
    """Полная иконка Apple TV из SVG (уже с фоном)."""
    return rsvg_png(SVG_APPLE, size, size)


def banner_ps5(w: int, h: int) -> Image.Image:
    base = Image.new("RGB", (w, h), (13, 71, 161))
    draw = ImageDraw.Draw(base)
    for i in range(h):
        t = i / max(h - 1, 1)
        r = int(13 + (21 - 13) * t)
        g = int(71 + (101 - 71) * t)
        b = int(161 + (192 - 161) * t)
        draw.line([(0, i), (w, i)], fill=(r, g, b))
    logo_h = int(h * 0.55)
    logo_w = int(w * 0.35)
    fg = rsvg_png(SVG_PS5, logo_w, logo_h)
    x = (w - fg.width) // 2
    y = (h - fg.height) // 2
    base_rgba = base.convert("RGBA")
    base_rgba.alpha_composite(fg, (x, y))
    return base_rgba.convert("RGB")


def banner_appletv(w: int, h: int) -> Image.Image:
    """Баннер 16:9: фон + иконка по центру без растяжения."""
    base = Image.new("RGB", (w, h), (38, 50, 56))
    draw = ImageDraw.Draw(base)
    for i in range(h):
        t = i / max(h - 1, 1)
        r = int(38 + (69 - 38) * t)
        g = int(50 + (90 - 50) * t)
        b = int(56 + (100 - 56) * t)
        draw.line([(0, i), (w, i)], fill=(r, g, b))
    side = int(min(w, h) * 0.62)
    fg = rsvg_png(SVG_APPLE, side, side)
    x = (w - fg.width) // 2
    y = (h - fg.height) // 2
    out = base.convert("RGBA")
    out.alpha_composite(fg, (x, y))
    return out.convert("RGB")


def main() -> None:
    if not Path(RSVG).exists() and not shutil.which("rsvg-convert"):
        raise SystemExit(
            "Нужен rsvg-convert (brew install librsvg). Не найден в PATH.",
        )

    for folder, px in DENSITIES.items():
        ps5_dir = PS5_RES / folder
        appletv_dir = APPLETV_RES / folder
        ps5_dir.mkdir(parents=True, exist_ok=True)
        appletv_dir.mkdir(parents=True, exist_ok=True)
        icon_ps5(px).save(ps5_dir / "ic_launcher_ps5.png", format="PNG", optimize=True)
        icon_appletv(px).save(
            appletv_dir / "ic_launcher_appletv.png",
            format="PNG",
            optimize=True,
        )

    ps5_nodpi = PS5_RES / "drawable-nodpi"
    appletv_nodpi = APPLETV_RES / "drawable-nodpi"
    ps5_nodpi.mkdir(parents=True, exist_ok=True)
    appletv_nodpi.mkdir(parents=True, exist_ok=True)
    banner_ps5(320, 180).save(
        ps5_nodpi / "tv_banner_ps5.png",
        format="PNG",
        optimize=True,
    )
    banner_appletv(320, 180).save(
        appletv_nodpi / "tv_banner_appletv.png",
        format="PNG",
        optimize=True,
    )
    print("OK:", PS5_RES, APPLETV_RES)


if __name__ == "__main__":
    main()
