package enums

import io.salat.sparser.annotations.ArgumentSerialized

enum class ScheduleType {
    @ArgumentSerialized("immediate", "i")
    IMMEDIATE,
    @ArgumentSerialized("deferred", "d")
    DEFERRED,
    @ArgumentSerialized("conditional", "c")
    CONDITIONAL
}