package com.egormit.hdmiswitch

class Ps5HdmiActivity : HdmiSwitchActivity() {
    override val target: HdmiTarget
        get() = HdmiTarget.PS5
}
