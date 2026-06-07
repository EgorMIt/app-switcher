package com.egormit.hdmiswitch

class AppleTvHdmiActivity : HdmiSwitchActivity() {
    override val target: HdmiTarget
        get() = HdmiTarget.APPLE_TV
}
