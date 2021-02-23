package dev.neeffect.nee.effects.monitoring


sealed class EntryType {
    object Begin : EntryType()

    data class End(val elapsedTime: Long) : EntryType()

    data class InternalError(val msg: String) : EntryType() {
        override fun toString(): String = "Error($msg)"

    }

}
