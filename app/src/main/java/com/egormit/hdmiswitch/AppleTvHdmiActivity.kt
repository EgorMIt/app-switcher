package com.egormit.hdmiswitch

class AppleTvHdmiActivity : HdmiSwitchActivity() {
    override val passthroughPath: String
        get() = "com.mediatek.tis%2F.HdmiInputService%2FHDMI300008"
}
