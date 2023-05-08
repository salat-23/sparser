package io.salat.sparser.converters

import io.salat.sparser.ArgumentConverter

class StringConverter: ArgumentConverter() {
    override fun convert(value: String, targetType: Class<*>): Any {
        return value
    }

    override fun canConvert(targetType: Class<*>): Boolean {
        return targetType == String::class.java
    }
}