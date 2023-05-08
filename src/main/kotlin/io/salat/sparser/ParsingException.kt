package io.salat.sparser

class ParsingException(message: String, val type: ParsingErrorType): RuntimeException(message)