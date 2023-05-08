package io.salat.sparser

import io.salat.sparser.annotations.Argument
import io.salat.sparser.annotations.ArgumentSerialized
import io.salat.sparser.annotations.Command

class SparserV1(
    private val commandContext: CommandContext,
    private val preprocessor: Preprocessor,
    private val converters: List<ArgumentConverter>
) : Sparser {
    private val argumentPrefix = "-"
    override fun parse(text: String): Any {

        val splitRegex = "\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex()
        // if amount of quotes is odd then throw exception
        //if (input.chars().filter { it.toChar() == '"' }.toList().size % 2 != 0)

        // split everything by white spaces and remove all quotes afterward
        // we get a list of tokens that text consists of
        val tokens = preprocessor
            .process(text)
            .split(splitRegex)
            .map { it.replace("\"", "") }
            .toMutableList()

        if (tokens.isEmpty()) throw ParsingException(
            "No tokens present",
            ParsingErrorType.NO_TOKENS_PRESENT
        )
        // we can be sure that tokens are not empty
        val commandToken = tokens[0].lowercase()
        // if our map does not contain any command with mapped token we throw exception
        if (!commandContext.identifierToCommandInfo.any { it.key == commandToken })
            throw ParsingException("Command not found: $commandToken", ParsingErrorType.COMMAND_DOES_NOT_EXIST)
        val commandInfo = commandContext.identifierToCommandInfo[commandToken]!!
        val commandClass = commandInfo.clazz.java
        val commandMetadata = commandInfo.metadata

//        var implicitArgument: Pair<Field, Argument>? = null
//        commandClass.declaredFields.forEach { field ->
//            // continue if field has no Argument annotation
//            val argumentMetadata = field.getDeclaredAnnotation(Argument::class.java) ?: return@forEach
//            if (argumentMetadata.implicit) { // todo create checks while creating app about default values and what not
//                implicitArgument = field to argumentMetadata
//            }
//            // add this to arguments and associate its identifiers with the field
//            argumentsMetadata.add(argumentMetadata)
//            argumentIdentifierToField.putAll(argumentMetadata.identifiers.map { Pair(it, field) })
//            // assign a default value for this field in case it is not explicitly provided
//            val defaultArgumentMetadata = field.getDeclaredAnnotation(Default::class.java)
//            if (defaultArgumentMetadata != null) {
//                fieldToDefaultArgument[field] = defaultArgumentMetadata.value
//            }
//        }

        // remove the command token afterward
        tokens.removeFirst()
        // now we only have argument related tokens

        // this map will contain all the necessary objects to pass to our @Command annotated class constructor
        val instantiationArguments = mutableMapOf<String, Any>()
        while (tokens.isNotEmpty()) {
            // this variable will contain the amount of tokens we will remove after this iteration
            var entriesToRemoveAmount = 0
            val currentToken = tokens.first()
            entriesToRemoveAmount++


            // check if argument name starts with '-'. throw exception if not
            // todo plan to make custom argument prefixes
            if (!currentToken.lowercase()
                    .startsWith(argumentPrefix.lowercase()) && commandInfo.implicitArgument == null
            )
                throw ParsingException(
                    "Unexpected argument name format: $currentToken",
                    ParsingErrorType.ARGUMENT_WRONG_NAME_FORMAT
                )

            // this pizdec decides if we should use implicit argument name or do usual logic
            // ================================
            var addFakeToken = false
            // get the argument identifier
            val argumentIdentifier = if (
            // example:
            // sayhi -name john
            // sayhi john

            // if "name" is an implicit argument, then implicit argument value will not contain "-"
            // in the beginning and we may be sure that it is implicit,
            // if it does (for example username is "-john", then argument name should be stated explicitly)
                currentToken.first().toString() != argumentPrefix && commandInfo.implicitArgument != null
            ) { // if argument is implicit
                // state that we add the fake token
                addFakeToken = true
                // return the saved primary identifier of our implicit argument to use
                commandInfo.implicitArgument.primaryIdentifier // return
            } else { // if argument is explicit
                // remove prefix
                currentToken.substring(argumentPrefix.length) // return
            }

            // we basically duplicate the first token if we decided to add fake token
            // we will be getting token value the same way all the time, so we must do this for parser to work properly
            if (commandInfo.implicitArgument != null && addFakeToken) {
                tokens.add(1, tokens.first())
            }
            // =================

            val argumentInfo = commandInfo.identifierToArgument[argumentIdentifier]
                ?: throw ParsingException(
                    "Could not parse argument: $currentToken",
                    ParsingErrorType.ARGUMENT_DOES_NOT_EXIST
                )
            val argumentMetadata = argumentInfo.metadata
            val argumentField = argumentInfo.field
            val argumentType = argumentField.type

            // run this piece of code once and make a tag that we can break out of
            // this block of code should decide argument type for instantiating an object
            run@ do {

                for (converter in converters) {
                    if (!converter.canConvert(argumentType)) continue

                    if (converter.isAbleToBeValueless()) {
                        // in case this is the only argument left, we say that it is truthful regardless
                        if (tokens.size - entriesToRemoveAmount == 0) {
                            instantiationArguments[argumentField.name] = converter.getDefaultWhenValueless()
                            break@run
                        }
                        // if the next token is another argument name - we say that it is truthful regardless
                        if (tokens[1].startsWith(argumentPrefix)) {
                            instantiationArguments[argumentField.name] = converter.getDefaultWhenValueless()
                            break@run
                        }
                    }

                    val convertedValue = converter.convert(tokens[1], argumentType)
                    entriesToRemoveAmount++
                    instantiationArguments[argumentField.name] = convertedValue
                }
                break@run

                /*
            // in case this is boolean argument
            // if boolean arguments are declared without value specifiers then we count them as truthful anyway
            if (argumentType == Boolean::class.java) {
                // in case this is the only argument left, we say that it is truthful regardless
                if (tokens.size - entriesToRemoveAmount == 0) {
                    instantiationArguments[argumentField.name] = true
                    break@run
                }

                // if the next token is another argument name - we say that it is truthful regardless
                if (tokens[1].startsWith(argumentPrefix)) {
                    instantiationArguments[argumentField.name] = true
                    break@run
                }

                // get the actual value (true\false) otherwise
                val booleanValue = tokens[1]
                entriesToRemoveAmount++


                try {
                    instantiationArguments[argumentField.name] =
                        convertStringValueToArgumentValue(booleanValue, argumentType)
                } catch (exception: IllegalStateException) {
                    throw ParsingException(
                        "Could not determine boolean value: $booleanValue for argument: $currentToken",
                        ParsingErrorType.ArgumentValueTypeIsWrong
                    )
                }
            }


            if (argumentType == String::class.java) {
                // if there is no tokens left, that means value is not provided
                if (tokens.size - entriesToRemoveAmount < 1) {
                    throw ParsingException(
                        "Argument -$argumentIdentifier value is not provided!",
                        ParsingErrorType.ArgumentValueIsNotProvided
                    )
                }

                // even if the next token starts with -, we still count it as a value
                // not the new argument name

                val stringValue: String = tokens[1].map { char ->
                    // try to get mapped letter (ф -> a, ru -> en), if present - change, else do not change
                    keymaps.forEach { keymap -> keymap.getKeymapReversed()[char.toString()] ?: char }
                }.joinToString("")
                entriesToRemoveAmount++
                instantiationArguments[argumentField.name] =
                    convertStringValueToArgumentValue(stringValue, argumentType)
            }

            if (argumentType.kotlin.isSubclassOf(Number::class) || argumentType.) {
                // if there is no tokens left, that means value is not provided
                if (tokens.size - entriesToRemoveAmount < 1) {
                    throw ParsingException(
                        "Argument -$argumentIdentifier value is not provided!",
                        ParsingErrorType.ARGUMENT_VALUE_IS_NOT_PROVIDED
                    )
                }

                // even if the next token starts with -, we still count it as a value
                // not the new argument name

                val numberValue = tokens[1]
                entriesToRemoveAmount++

                instantiationArguments[argumentField.name] =
                    convertStringValueToArgumentValue(numberValue, argumentType)
            }

            if (argumentType.isEnum) {
                // if there is no tokens left, that means value is not provided
                if (tokens.size - entriesToRemoveAmount < 1) {
                    throw ParsingException(
                        "Argument -$argumentIdentifier value is not provided!",
                        ParsingErrorType.ARGUMENT_VALUE_IS_NOT_PROVIDED
                    )
                }

                // even if the next token starts with -, we still count it as a value
                // not the new argument name

                val enumValue = tokens[1]
                entriesToRemoveAmount++

                instantiationArguments[argumentField.name] =
                    convertStringValueToArgumentValue(enumValue, argumentType)
            }
            */
            } while (false)

            // remove parsed tokens
            while (entriesToRemoveAmount > 0) {
                tokens.removeFirst()
                entriesToRemoveAmount--
            }

        }

        // prepare field in the right order for creating an instance
        val allArgumentsNeededPrepared = mutableListOf<Any>()
        for (argument in commandInfo.arguments) {
            // the instantiatable object class
            val argumentType = argument.field.type
            // the final argument which is provided to command class constructor
            val providedArgument =
                instantiationArguments[argument.field.name]
                    ?: converters.single { it.canConvert(argumentType) }
                        .convert(
                            argument.defaultValue
                                ?: throw ParsingException(
                                    "Could not create command because required argument is missing: ${argument.metadata.name}",
                                    ParsingErrorType.REQUIRED_ARGUMENT_DOES_NOT_HAVE_DEFAULT_VALUE
                                ), argumentType
                        )
            allArgumentsNeededPrepared.add(providedArgument)
        }

        if (allArgumentsNeededPrepared.size != commandClass.declaredFields.size)
        // if this thing pops up - it means that you either declared excess class
        // fields that are not annotated with @Argument or something went really wrong
            throw IllegalStateException("Could not prepare all fields for calling class instance constructor")

        val commandClassConstructor = commandClass.declaredConstructors[0]
        val instance = commandClassConstructor.newInstance(*allArgumentsNeededPrepared.toTypedArray())
        // instance containing all arguments is now created
        return instance
    }

    override fun generateCall(commandObject: Any): String {
        val commandClass = commandObject::class.java
        val commandMetadata: Command = commandClass.getAnnotation(Command::class.java)
            ?: throw IllegalStateException("Could not get command metadata in class: ${commandClass.javaClass.canonicalName}")

        var commandCall = ""
        commandCall += commandMetadata.identifiers.first()

        for (field in commandClass.declaredFields) {
            val argumentMetadata =
                field.getAnnotation(Argument::class.java)//field.getDeclaredAnnotation(Argument::class.java)
                    ?: throw IllegalStateException("Could not get field metadata: ${field.name} in class: ${commandClass.canonicalName}")

//            var defaultValue: String? = null
//            if (!argumentMetadata.required) {
//                val defaultArgumentMetadata = field.getDeclaredAnnotation(Default::class.java)
//                    ?: throw IllegalStateException("Argument: ${field.name} in command: ${commandClass.canonicalName} does not have default value. Make argument required or add @Default annotation.")
//                defaultValue = defaultArgumentMetadata.value
//            }

            // get the field value of the command instance
            field.isAccessible = true
            // field.trySetAccessible()
            val fieldValue = field.get(commandObject)
            val argumentIdentifier = argumentMetadata.identifiers.first()
            val argumentValue = if (fieldValue is Enum<*>) {
                val enumClass = fieldValue::class.java
                val enumField = enumClass.getDeclaredField(fieldValue.name)
                val serializationMetadata = enumField.getDeclaredAnnotation(ArgumentSerialized::class.java)
                serializationMetadata.identifiers.first()
            } else fieldValue.toString()

            val argument = "$argumentPrefix$argumentIdentifier $argumentValue"
            commandCall += " $argument"
        }
        return commandCall
    }

}