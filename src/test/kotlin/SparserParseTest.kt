import io.salat.sparser.CommandContext
import io.salat.sparser.SparserBuilder
import io.salat.sparser.converters.BooleanConverter
import io.salat.sparser.converters.EnumConverter
import io.salat.sparser.converters.NumberConverter
import io.salat.sparser.converters.StringConverter
import io.salat.sparser.keymaps.RussianKeymap
import org.junit.jupiter.api.Assertions
import commands.GetHealthCommand
import enums.ScheduleType
import kotlin.test.Test

class SparserParseTest {

    private val commandContext = CommandContext()
    private val keymaps = listOf(RussianKeymap())

    private val parser = SparserBuilder()
        .registerContext(commandContext)
        .registerConverters(
            listOf(
                BooleanConverter(keymaps),
                EnumConverter(keymaps),
                NumberConverter(),
                StringConverter()
            )
        ).build()

    @Test
    fun `Parsed enum type should be correct`() {
        val resultObj = parser.parse("health -s deferred") as GetHealthCommand
        Assertions.assertEquals(ScheduleType.DEFERRED, resultObj.scheduleType)
    }

    @Test
    fun `Parsed timeout should be correct`() {
        val resultObj = parser.parse("health -schedule immediate -t 1000000") as GetHealthCommand
        Assertions.assertEquals(1000000, resultObj.timeout)
    }

    @Test
    fun `Parsed gpu mode should be correct (valueless)`() {
        val resultObj = parser.parse("health -schedule immediate -g") as GetHealthCommand
        Assertions.assertEquals(true, resultObj.includeGpu)
    }

    @Test
    fun `Parsed gpu mode should be correct`() {
        val resultObj = parser.parse("health -schedule immediate -g false") as GetHealthCommand
        Assertions.assertEquals(false, resultObj.includeGpu)
        val resultObj1 = parser.parse("health -schedule immediate -g true") as GetHealthCommand
        Assertions.assertEquals(true, resultObj1.includeGpu)
    }
}