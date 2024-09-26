package me.cunzai.treasuregame.command

import me.cunzai.treasuregame.config.ConfigLoader
import me.cunzai.treasuregame.util.enabled
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.expansion.submitChain
import taboolib.platform.util.sendLang

@CommandHeader(name = "treasure", aliases = ["tm"], permissionDefault = PermissionDefault.TRUE)
object TreasureCommands {

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val leave = subCommand {
        execute<Player> { sender, _, _ ->
            if (sender.world.name == ConfigLoader.treasureWorld) {
                sender.teleport(Bukkit.getWorlds().first().spawnLocation)
                sender.sendLang("leave")
            }
        }
    }



    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val join = subCommand {
        execute<Player> { sender, _, _ ->
            if (!enabled) return@execute

            if (sender.world.name == ConfigLoader.treasureWorld) return@execute

            sender.teleport(
                ConfigLoader.randomLocation.random()
            )

            sender.sendLang("join")
        }
    }


    @CommandBody(permission = "treasure.admin")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            ConfigLoader.config.reload()
            ConfigLoader.loadConfig()

            sender.sendMessage("ok")
        }
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }


}