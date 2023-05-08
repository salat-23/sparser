package commands

import enums.ScheduleType
import io.salat.sparser.annotations.Argument
import io.salat.sparser.annotations.Command
import io.salat.sparser.annotations.Default

@Command(
    name = "Get health command",
    description = "Fetches recent metrics on system and checks if the system is under-performing",
    identifiers = ["health", "h"]
)
class GetHealthCommand(
    @Argument(
        name = "Include GPU",
        description = "Decides if GPU metric should be included or not",
        identifiers = ["gpu", "g"],
    )
    @Default("false")
    val includeGpu: Boolean,
    @Argument(
        name = "Timeout",
        description = "Aborts the command if timeout exceeds this value",
        identifiers = ["timeout", "t"]
    )
    @Default("1000")
    val timeout: Long,
    @Argument(
        name = "Schedule type",
        description = "Which schedule type should be used",
        identifiers = ["schedule", "s"],
        required = true
    )
    val scheduleType: ScheduleType


) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetHealthCommand

        if (includeGpu != other.includeGpu) return false
        if (timeout != other.timeout) return false
        return scheduleType == other.scheduleType
    }

    override fun hashCode(): Int {
        var result = includeGpu.hashCode()
        result = 31 * result + timeout.hashCode()
        result = 31 * result + scheduleType.hashCode()
        return result
    }
}