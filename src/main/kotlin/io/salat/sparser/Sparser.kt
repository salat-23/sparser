package io.salat.sparser

import io.salat.sparser.annotations.Command

interface Sparser {
    fun parse(text: String): Any
    fun generateCall(commandObject: Any): String
}