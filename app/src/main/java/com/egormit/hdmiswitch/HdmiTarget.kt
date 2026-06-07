package com.egormit.hdmiswitch

enum class HdmiTarget(
    val displayName: String,
    val labelAliases: List<String>,
    val deviceSelectLogicalAddress: Int,
    private val inputLogicalAddresses: Set<Int>,
) {
    PS5(
        displayName = "PlayStation 5",
        labelAliases = listOf(
            "PlayStation 5",
            "PS5",
        ),
        deviceSelectLogicalAddress = 4,
        inputLogicalAddresses = setOf(4),
    ),
    APPLE_TV(
        displayName = "Apple TV",
        labelAliases = listOf(
            "Apple TV",
            "Apple TV 4K",
            "Playback_2",
        ),
        deviceSelectLogicalAddress = 8,
        inputLogicalAddresses = setOf(8),
    ),
    ;

    fun matchesLabel(label: CharSequence): Boolean =
        label.toString().normalized() in labelAliases.map { it.normalized() }

    private fun matchesLogicalAddress(address: Int): Boolean =
        address in inputLogicalAddresses

    companion object {
        fun forPackageName(packageName: String): HdmiTarget? =
            when {
                packageName.endsWith(".ps5") -> PS5
                packageName.endsWith(".appletv") -> APPLE_TV
                else -> null
            }

        fun fromInputId(inputId: String): HdmiTarget? {
            val logicalAddress = CEC_CHILD_INPUT_ID_PATTERN.find(inputId)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull(16)
                ?: return null
            return entries.firstOrNull { target ->
                target.matchesLogicalAddress(logicalAddress)
            }
        }

        fun fromLabel(label: CharSequence): HdmiTarget? =
            entries.firstOrNull { target ->
                target.matchesLabel(label)
            }

        private val CEC_CHILD_INPUT_ID_PATTERN = Regex("""HDMI[0-9A-Fa-f]{4}([0-9A-Fa-f]{2})""")
    }
}

fun String.normalized(): String = trim().lowercase()
