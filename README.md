### Sparser - advanced string to object parser library written in Kotlin

Included features:

- Command and arguments description using annotations
- Parse string commands into correlating command objects
- Generate calls from command objects
- Convert any value using custom converters
- Default converters work with numbers, strings, booleans and enums

Add Sparser to your project:

Add this to your pom.xml:
```xml
<dependency>
    <groupId>io.github.salat-23</groupId>
    <artifactId>sparser</artifactId>
    <version>1.0.0</version>
</dependency>
```
or if you are using gradle:
```groovy
implementation 'io.github.salat-23:sparser:1.0.0' // groovy dsl
```
```kotlin
implementation("io.github.salat-23:sparser:1.0.0") // kotlin dsl
```

Simple usage example:

Ideally, command must be a simple POJO with all arguments constructor available.

We have a command object which we must annotate with `@Command`, provide name and description and also provide list of
unique identifiers.
There is no limit on identifiers amount, the only requirement is that they should be unique.

Each field will act as an argument and must be annotated with `@Argument`. This annotation also takes name, description
and identifiers. Arguments must have unique identifiers in their command scope.

By default, each argument is optional, and must also be annotated with @Default, which will describe default string
provided value to it. If argument is required, `true` should be passed to `required` parameter.

There is also `implicit` parameter which states that the argument might be set right after command name without using
identifiers. In our case example would be: `health deferred` = `health -schedule deferred`.

```kotlin
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
        required = true,
        implicit = true
    )
    val scheduleType: ScheduleType
)
```

If you are using enum as an argument, you must provide `@ArgumentSerialized` annotations to each enum entry and give
them unique identifiers.

```kotlin
enum class ScheduleType {
    @ArgumentSerialized("immediate", "i")
    IMMEDIATE,

    @ArgumentSerialized("deferred", "d")
    DEFERRED,

    @ArgumentSerialized("conditional", "c")
    CONDITIONAL
}
```
And finally you can create command context and use your parser. Note, that you also can provide layout keymaps to your converters. Explanation what are those can be found here [TODO]
```kotlin
val commandContext = CommandContext()
val keymaps = listOf(RussianKeymap())
val parser = SparserBuilder()
    .registerContext(commandContext)
    .registerConverters(
        listOf(
            BooleanConverter(keymaps),
            EnumConverter(keymaps),
            NumberConverter(),
            StringConverter()
        )
    ).build()


val resultObj = parser.parse("health -s deferred") as GetHealthCommand
// resultObj.scheduleType == ScheduleType.DEFERRED
```

