package io.salat.sparser

interface Preprocessor {
    fun process(input: String): String
}