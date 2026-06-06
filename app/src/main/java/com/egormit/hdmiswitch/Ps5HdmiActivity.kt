package com.egormit.hdmiswitch

class Ps5HdmiActivity : HdmiSwitchActivity() {
    override val inputLabelAliases: List<String>
        get() = listOf(
            "PlayStation 5",
            "PS5",
        )
}
