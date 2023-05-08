package io.salat.sparser.keymaps

interface LayoutKeymap {
    fun getKeymap(): Map<String, String>

    fun getKeymapReversed(): Map<String, String>
}