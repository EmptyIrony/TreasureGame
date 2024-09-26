package me.cunzai.treasuregame.util

import me.cunzai.treasuregame.config.ConfigLoader

val enabled: Boolean
    get() {
        return ConfigLoader.enableTime.isEnable()
    }