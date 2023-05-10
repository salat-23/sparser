package io.salat.sparser.converters

import io.salat.sparser.ArgumentConverter
import io.salat.sparser.keymaps.KeymapTranslator
import io.salat.sparser.keymaps.LayoutKeymap

/**
 * Converts string to boolean value.
 */
class BooleanConverter(keymaps: List<LayoutKeymap>) : ArgumentConverter() {
    // hardcoded bool identifiers because why not
    private val booleanTruthIdentifiers = KeymapTranslator.translate(listOf("true", "t"), keymaps, true)
    private val booleanFalseIdentifiers = KeymapTranslator.translate(listOf("false", "f"), keymaps, true)
    override fun convert(value: String, targetType: Class<*>): Any {
        if (targetType == Boolean::class.java) {
            if (booleanTruthIdentifiers.contains(value)) return true
            if (booleanFalseIdentifiers.contains(value)) return false
        }
        throw IllegalStateException("Can't parse boolean value")
    }

    override fun canConvert(targetType: Class<*>): Boolean {
        return targetType == Boolean::class.java
    }

    override fun isAbleToBeValueless(): Boolean = true
    override fun getDefaultWhenValueless(): Any = true
}