package me.cunzai.treasuregame.scheduler

import me.cunzai.treasuregame.config.ConfigLoader
import me.cunzai.treasuregame.util.enabled
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.module.chat.Components
import taboolib.module.lang.asLangText
import taboolib.platform.util.isRightClickBlock
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendLang

object GameScheduler {
    var lastEnableStatus = false

    var nextTreasureSeconds = -1
        get() {
            return if (field == -1) {
                nextTreasureSeconds = ConfigLoader.periodSeconds
                ConfigLoader.periodSeconds
            } else {
                field
            }
        }

    val lastSpawnedChest = ArrayList<Location>()

    val openedChest = HashSet<Location>()

    @Schedule(period = 20)
    fun s() {
        val currentEnabled = enabled
        if ((currentEnabled && !lastEnableStatus) || (!enabled && lastEnableStatus)) {
            broadcastCurrentStatus()
        }

        lastEnableStatus = currentEnabled

        if (!currentEnabled) return

        nextTreasureSeconds--
        if (nextTreasureSeconds <= 0) {
            summonChests()
            nextTreasureSeconds = ConfigLoader.periodSeconds
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.isRightClickBlock()) {
            if (openedChest.add(e.clickedBlock.location)) {
                val location = e.clickedBlock.location
                location.world.strikeLightningEffect(e.clickedBlock.location)
                console().asLangText("chest_open", location.blockX, location.blockY, location.blockZ)
            }
        }
    }

    private fun broadcastCurrentStatus() {
        if (enabled) {
            Components.text(
                console().asLangText("game_start")
            ).clickRunCommand("/tm join")
                .broadcast()
        } else {
            Components.text(
                console().asLangText("game_end")
            ).clickRunCommand("/tm join")
                .broadcast()

            for (player in Bukkit.getWorld(ConfigLoader.treasureWorld).players) {
                player.teleport(Bukkit.getWorlds().first().spawnLocation)
                player.sendLang("leave")
            }
        }
    }

    private fun summonChests() {
        val chests = ArrayList<ConfigLoader.ChestType>()
        repeat(ConfigLoader.spawnChestNumber) {
            randomChest(chests)
        }

        if (ConfigLoader.removePreviousTreasures) {
            for (location in lastSpawnedChest) {
                (location.block.state as? Chest)?.blockInventory?.clear()
                location.block.type = Material.AIR
            }
        }

        lastSpawnedChest.clear()

        for (chest in chests) {
            val spawn = chest.spawn()
            spawn.world.strikeLightningEffect(spawn)
            lastSpawnedChest += spawn
            onlinePlayers.forEach { player ->
                player.sendLang(
                    "produce",
                    spawn.blockX,
                    spawn.blockY,
                    spawn.blockZ,
                    chest.displayName
                )
            }
        }
    }

    private fun randomChest(list: ArrayList<ConfigLoader.ChestType>) {
        val chestType = ConfigLoader.randomChestList.random()!!
        val splitNumber = chestType.splitNumber
        if (splitNumber > 0) {
            val lastIndex = list.indexOfLast {
                it.displayName == chestType.displayName
            }
            if (lastIndex != -1 && lastIndex < splitNumber) {
                randomChest(list)
                return
            }
        }

        list += chestType
    }

}