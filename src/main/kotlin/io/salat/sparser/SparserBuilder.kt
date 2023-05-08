package io.salat.sparser

class SparserBuilder {
    private var commandContext: CommandContext? = null
    private var preprocessor: Preprocessor? = null
    private var converters: MutableList<ArgumentConverter> = mutableListOf()
    fun registerContext(commandContext: CommandContext): SparserBuilder {
        this.commandContext = commandContext
        return this
    }

    fun registerPreprocessor(preprocessor: Preprocessor): SparserBuilder {
        this.preprocessor = preprocessor
        return this
    }

    fun registerConverter(converter: ArgumentConverter): SparserBuilder {
        this.converters += converter
        return this
    }

    fun registerConverters(converters: List<ArgumentConverter>): SparserBuilder {
        this.converters += converters
        return this
    }

    fun build(): Sparser {
        if (commandContext == null) throw IllegalStateException("Command context must be provided!")
        if (preprocessor == null) preprocessor = object : Preprocessor {
            override fun process(input: String): String {
                return input
            }
        }
        return SparserV1(
            commandContext!!,
            preprocessor!!,
            converters
        )
    }
}