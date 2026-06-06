# HDMI Switch (Android TV)

Два отдельных приложения (разные `applicationId`), потому что **Google TV обычно показывает на полке только один ярлык на пакет**. Сборка через **product flavors** `ps5` и `appletv`.

| Flavor    | Пакет                             | Название на полке |
|-----------|-----------------------------------|-------------------|
| `ps5`     | `com.egormit.hdmiswitch.ps5`      | PS5 HDMI          |
| `appletv` | `com.egormit.hdmiswitch.appletv`  | Apple TV HDMI     |

## Лицензия и ассеты

- Код: [MIT](LICENSE).
- SVG в `tools/icons/` взяты с **Icons8** — соблюдай [их лицензию](https://icons8.com/license) и условия атрибуции при распространении.

## Первый клон / публикация

1. **Gradle:** файл `gradle.properties` **не коммитится** (там путь к JDK на твоей машине). Для нового окружения:

   ```bash
   cp gradle.properties.example gradle.properties
   ```

   Раскомментируй и пропиши `org.gradle.java.home` для JDK **21**, либо выставь `JAVA_HOME` и убери эту строку.

2. **Android SDK:** скопируй `local.properties.example` → `local.properties`, укажи `sdk.dir`.

3. Сборка:

   ```bash
   ./gradlew assemblePs5Debug assembleAppletvDebug
   ```

APK:

- `app/build/outputs/apk/ps5/debug/app-ps5-debug.apk`
- `app/build/outputs/apk/appletv/debug/app-appletv-debug.apk`

Установка:

```bash
adb install -r app/build/outputs/apk/ps5/debug/app-ps5-debug.apk
adb install -r app/build/outputs/apk/appletv/debug/app-appletv-debug.apk
```

Старый единый пакет `com.egormit.hdmiswitch` при необходимости: `adb uninstall com.egormit.hdmiswitch`.

## Требования

- Android SDK Platform **37.0**, Build Tools **37.0.0**, Platform Tools **37.0.0** (`local.properties`).
- Gradle **9.5.1**, Android Gradle Plugin **9.2.1**.
- JDK **21** для локальной сборки.

## Button Mapper

Действие «Запустить приложение» → **PS5 HDMI** или **Apple TV HDMI**. Пакет `com.mitv.livetv` на ТВ должен быть включён.

## Свой HDMI

Приложения не привязаны к номеру HDMI-порта: они ищут вход по названию из `TvInputManager`.
Если устройство переименовано на ТВ, обнови `inputLabelAliases` в `Ps5HdmiActivity.kt` / `AppleTvHdmiActivity.kt`.
Список доступных входов можно посмотреть через `adb shell dumpsys tv_input` или в логах приложения.

## Иконки (SVG → PNG)

```bash
python3 tools/gen_launcher_icons.py
```

Нужны: `pip install Pillow`, `rsvg-convert` (`brew install librsvg`). Затем пересобери APK.

Подробнее: `tools/icons/README.md`.
