import commands.GetHealthCommand
import enums.ScheduleType
import io.salat.sparser.CommandContext
import io.salat.sparser.SparserBuilder
import io.salat.sparser.converters.BooleanConverter
import io.salat.sparser.converters.EnumConverter
import io.salat.sparser.converters.NumberConverter
import io.salat.sparser.converters.StringConverter
import io.salat.sparser.keymaps.RussianKeymap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test

class SparserGenerateCallTest {
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
    fun `Should generate call from command object, then parse generated call and get equal object`() {
        val healthCommand = GetHealthCommand(true, 1337, ScheduleType.CONDITIONAL)
        val generatedCall = parser.generateCall(healthCommand)
        val parsedObject = parser.parse(generatedCall)
        Assertions.assertEquals(healthCommand, parsedObject)
    }
}