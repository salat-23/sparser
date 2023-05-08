package io.salat.sparser

import io.salat.sparser.annotations.Argument
import io.salat.sparser.annotations.Command
import io.salat.sparser.annotations.Default
import io.salat.sparser.keymaps.KeymapTranslator
import io.salat.sparser.keymaps.LayoutKeymap
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass


class CommandContext(
    val keymaps: List<LayoutKeymap> = emptyList()
) {
    data class CommandInfo(
        val metadata: Command,
        val primaryIdentifier: String,
        val clazz: KClass<*>,
        val arguments: List<ArgumentInfo>,
        val identifierToArgument: Map<String, ArgumentInfo>,
        val implicitArgument: ArgumentInfo?
    )

    data class ArgumentInfo(
        val metadata: Argument,
        val primaryIdentifier: String,
        val defaultValue: String?,
        val field: Field
    )

    val identifierToCommandInfo: Map<String, CommandInfo>
    private val commands: List<CommandInfo>
    init {
        // create copy of the containers to later copy their contents to appropriate field's
        val identifierToCommandInfo = mutableMapOf<String, CommandInfo>()
        val commands = mutableSetOf<CommandInfo>()

        // todo add filtering by some packaage
        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated, Scanners.Resources, Scanners.SubTypes)
//                .filterInputsBy(FilterBuilder().includePackage()));
        )

        // get all classes annotated with @Command thus having necessary metadata
        val allAnnotatedClasses = reflections.getTypesAnnotatedWith(Command::class.java)


        for (commandClass in allAnnotatedClasses) {
            val commandMetadata: Command = commandClass.getAnnotation(Command::class.java)
                ?: throw IllegalStateException("Could not get command metadata in class: ${commandClass.javaClass.canonicalName}")

            // map which will contain pairs of argument name and argument metadata
            val identifierToArgumentInfo = mutableMapOf<String, ArgumentInfo>()
            //
            val arguments = mutableSetOf<ArgumentInfo>()


            // we think that there is no implicit argument in the beginning
            var hasImplicitArgument = false
            var implicitArgument: ArgumentInfo? = null
            for (field in commandClass.declaredFields) {
                val argumentMetadata =
                    field.getAnnotation(Argument::class.java)//field.getDeclaredAnnotation(Argument::class.java)
                        ?: throw IllegalStateException("Could not get field metadata: ${field.name} in class: ${commandClass.canonicalName}")

                // command can contain only 1 implicit argument. this rule is being checked here
                if (argumentMetadata.implicit) {
                    // this check uses boolean variable instead of checking implicitArgument for null.
                    if (hasImplicitArgument) throw IllegalStateException("Command ${commandClass.canonicalName} contains several implicit arguments.")
                    hasImplicitArgument = true
                }

                // default value for certain arguments must be defined if it is required. this rule is being checked here
                var defaultValue: String? = null
                if (!argumentMetadata.required) {
                    val defaultArgumentMetadata = field.getDeclaredAnnotation(Default::class.java)
                        ?: throw IllegalStateException("Argument: ${field.name} in command: ${commandClass.canonicalName} does not have default value. Make argument required or add @Default annotation.")
                    defaultValue = defaultArgumentMetadata.value
                }

                // check if argument contains
                if (argumentMetadata.identifiers.isEmpty()) throw IllegalStateException("Argument: ${field.name} in command: ${commandClass.canonicalName} has no identifiers")
                // first identifier specified is the primary one
                val primaryIdentifier = argumentMetadata.identifiers[0].lowercase()
                val info = ArgumentInfo(
                    metadata = argumentMetadata,
                    primaryIdentifier = primaryIdentifier,
                    defaultValue = defaultValue,
                    field = field
                )
                if (argumentMetadata.implicit) implicitArgument = info

                // this function will associate every argument identifier in command to the appropriate ArgumentInfo class
                val assignIdentifiersFunction = { identifier: String ->
                    // every key in map will be lowercase'd for case-insensitive checks
                    val identifier = identifier.lowercase()
                    // second check is made for other language keymap-translated identifiers to work properly
                    if (identifierToArgumentInfo.containsKey(identifier) && identifierToArgumentInfo[identifier]!! != info)
                        throw IllegalStateException("Duplicate argument identifier found: $identifier in ${field.name} and ${identifierToArgumentInfo[identifier]!!.field.name}")
                    if (identifier.isEmpty()) throw IllegalStateException("Identifier cannot be an empty string. Argument ${field.name} in ${commandClass.canonicalName}")
                    identifierToArgumentInfo[identifier] = info
                    arguments += info
                }
                // english identifiers
                argumentMetadata.identifiers.forEach(assignIdentifiersFunction)
                // other language keymap layouts
                argumentMetadata.identifiers.forEach { identifier ->
                    KeymapTranslator.translate(identifier, keymaps).forEach(assignIdentifiersFunction)
                }
            }

            val primaryIdentifier = commandMetadata.identifiers[0].lowercase()
            val info = CommandInfo(
                metadata = commandMetadata,
                primaryIdentifier = primaryIdentifier,
                clazz = commandClass.kotlin,
                arguments = arguments.toList(),
                identifierToArgument = identifierToArgumentInfo,
                implicitArgument = implicitArgument
            )

            val assignIdentifiers = { identifier: String ->
                // every key in map will be lowercase'd for case-insensitive checks
                val identifier = identifier.lowercase()
                if (identifierToCommandInfo.contains(identifier) && identifierToCommandInfo[identifier]!! != info)
                    throw IllegalStateException("Duplicate command identifier found: $identifier in ${commandClass.canonicalName} and ${identifierToCommandInfo[identifier]!!.clazz.java.canonicalName}")

                if (identifier.isEmpty()) throw IllegalStateException("Identifier cannot be empty. Command ${commandClass.canonicalName}")

                identifierToCommandInfo[identifier] = info
                commands += info
            }

            // english identifiers
            commandMetadata.identifiers.forEach(assignIdentifiers)
            // other language layouts
            commandMetadata.identifiers.forEach { identifier ->
                KeymapTranslator.translate(identifier, keymaps).forEach(assignIdentifiers)
            }
        }


        this.identifierToCommandInfo = identifierToCommandInfo
        this.commands = commands.toList()
    }


}