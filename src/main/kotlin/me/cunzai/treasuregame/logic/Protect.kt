package me.cunzai.treasuregame.logic

import me.cunzai.treasuregame.config.ConfigLoader
import me.cunzai.treasuregame.util.enabled
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.sendLang

object Protect {


    @SubscribeEvent(priority = EventPriority.LOW)
    fun banCommands(e: PlayerCommandPreprocessEvent) {
        val message = e.message
        val player = e.player
        if (player.world.name != ConfigLoader.treasureWorld) {
            return
        }

        if (player.hasPermission("treasure.admin")) return

        val commandLabel = message.removePrefix("/")
        val allowedToUse = ConfigLoader.whitelistCommands.any {
            commandLabel.lowercase().startsWith(it)
        }

        if (!allowedToUse) {
            e.isCancelled = true
            player.sendLang("command")
        }
    }

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        if (e.to.world.name != ConfigLoader.treasureWorld) {
            return
        }

        if (e.player.hasPermission("treasure.admin")) return

        if (!enabled) {
            e.isCancelled = true
        }
    }

}