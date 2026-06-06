package com.egormit.hdmiswitch

class AppleTvHdmiActivity : HdmiSwitchActivity() {
    override val inputLabelAliases: List<String>
        get() = listOf(
            "Apple TV",
        )
}
