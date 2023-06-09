package io.salat.sparser.annotations

import io.salat.sparser.keymaps.KeymapTranslator
import io.salat.sparser.keymaps.LayoutKeymap

/**
 * Serializes enum entry to be discoverable by parser and allows enum to be used as [Argument].
 */
annotation class ArgumentSerialized(
    vararg val identifiers: String
) {
    companion object {
        fun getEnumIdentifierToOrdinalMap(enumType: Class<*>, keymaps: List<LayoutKeymap>): Map<String, Int> {
            val identifierToEnumOrdinal = mutableMapOf<String, Int>()

            val enumConstantFields = enumType.declaredFields.filter { field ->
                field.isEnumConstant && field.getDeclaredAnnotation(ArgumentSerialized::class.java) != null
            }

            var currentOrdinal = 0
            enumConstantFields.forEach { constantField ->
                val identifiers =
                    constantField.getDeclaredAnnotation(ArgumentSerialized::class.java)!!.identifiers

                identifiers.forEach { identifier ->
                    identifierToEnumOrdinal[identifier] = currentOrdinal
                    identifierToEnumOrdinal += KeymapTranslator.translate(identifier, keymaps).associateWith { currentOrdinal }
                }

                currentOrdinal++
            }

            return identifierToEnumOrdinal
        }
    }
}
