package com.egormit.hdmiswitch

class Ps5HdmiActivity : HdmiSwitchActivity() {
    override val passthroughPath: String
        get() = "com.mediatek.tis%2F.HdmiInputService%2FHDMI100004"
}
