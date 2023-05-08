package io.salat.sparser.converters

import io.salat.sparser.ArgumentConverter
import io.salat.sparser.ParsingErrorType
import io.salat.sparser.ParsingException
import io.salat.sparser.keymaps.LayoutKeymap
import io.salat.sparser.annotations.ArgumentSerialized

class EnumConverter(
    private val keymaps: List<LayoutKeymap>
): ArgumentConverter() {
    override fun convert(value: String, targetType: Class<*>): Any {
        val identifierToEnumOrdinalIndex = ArgumentSerialized.getEnumIdentifierToOrdinalMap(targetType, keymaps)

        if (!identifierToEnumOrdinalIndex.containsKey(value))
            throw ParsingException(
                "No such value available: $value", ParsingErrorType.ENUM_VALUE_DOES_NOT_EXIST
            )

        val ordinalIndex = identifierToEnumOrdinalIndex[value]!!
        val enumValue = targetType.enumConstants[ordinalIndex]!!

        return enumValue
    }

    override fun canConvert(targetType: Class<*>): Boolean {
        return targetType.isEnum
    }
}