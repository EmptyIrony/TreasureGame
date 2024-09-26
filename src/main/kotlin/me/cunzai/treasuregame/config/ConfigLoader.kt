package me.cunzai.treasuregame.config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common5.RandomList
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import java.util.Calendar
import kotlin.random.Random
import kotlin.random.nextInt

object ConfigLoader {

    @Config
    lateinit var config: Configuration

    lateinit var treasureWorld: String

    var periodSeconds = 10
    var spawnChestNumber = 10

    var removePreviousTreasures = false

    lateinit var randomLocation: RandomTeleport

    lateinit var enableTime: EnableTime

    val chests = ArrayList<ChestType>()

    lateinit var randomChestList: RandomList<ChestType>

    val whitelistCommands = HashSet<String>()

    @Awake(LifeCycle.ENABLE)
    fun loadConfig() {
        removePreviousTreasures = config.getBoolean("remove_previous_treasures")
        treasureWorld = config.getString("world")!!
        randomLocation = config.getConfigurationSection("rtp")!!.let {
            RandomTeleport(
                it.getString("x")!!.let { xString ->
                    val split = xString.split("~")
                    split[0].toInt() .. split[1].toInt()
                },
                it.getString("z")!!.let { zString ->
                    val split = zString.split("~")
                    split[0].toInt() .. split[1].toInt()
                },
            )
        }

        whitelistCommands.clear()
        whitelistCommands += config.getStringList("whitelist_commands").map { it.lowercase() }

        periodSeconds = config.getInt("period")
        spawnChestNumber = config.getInt("spawn_chest_number")
        enableTime = config.getString("enable_time")!!.let {
            val split = it.split("-")
            EnableTime(
                split[0].toInt(), split[1].toInt()
            )
        }

        chests.clear()
        config.getConfigurationSection("chest_type")!!.let { section ->
            for (nodeName in section.getKeys(false)) {
                val chestSection = section.getConfigurationSection(nodeName)!!
                chests += ChestType(
                    chestSection.getStringColored("display_name")!!,
                    chestSection.getInt("weight"),
                    chestSection.getInt("split_number"),
                    chestSection.getInt("min_items"),
                    chestSection.getInt("max_items"),
                    chestSection.getConfigurationSection("items")!!.let { itemSection ->
                        itemSection.getKeys(false).map {
                            val itemString = itemSection.getString(it)
                            val split = itemString!!.split(":")
                            split[0].toIntOrNull()?.let { materialId ->
                                VanillaItemReward(materialId, split[1].toInt())
                            } ?: MythicMobsItemReward(split[0], split[1].toInt())
                        }
                    }
                )
            }
        }

        randomChestList = RandomList(chests.map { it to it.weight })
    }

    data class RandomTeleport(
        val xRange: IntRange,
        val zRange: IntRange,
    ) {
        fun random(): Location {
            return Location(
                Bukkit.getWorld(treasureWorld),
                xRange.random().toDouble(),
                0.0,
                zRange.random().toDouble()
            ).let {
                it.world.getHighestBlockAt(it).location.clone()
            }
        }
    }

    data class EnableTime(
        val start: Int,
        val end: Int,
    ) {
        fun isEnable(): Boolean {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()

            return calendar.get(Calendar.HOUR_OF_DAY) in start .. end
        }
    }

    data class ChestType(
        val displayName: String,
        val weight: Int,
        val splitNumber: Int,
        val minItems: Int,
        val maxItems: Int,
        val items: List<ItemReward>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ChestType

            return displayName == other.displayName
        }

        override fun hashCode(): Int {
            return displayName.hashCode()
        }

        fun spawn(): Location {
            val location = randomLocation.random()
            val block = location.block
            block.type = Material.CHEST
            val chest = block.state as Chest

            val list = items.toMutableList()

            val slotsShuffled = (0 until chest.blockInventory.size).shuffled().toMutableList()

            val itemCount = Random.nextInt(minItems, maxItems)
            repeat(itemCount) {
                val slot = slotsShuffled.removeFirst()
                val index = Random.nextInt(0, list.size - 1)
                chest.blockInventory.setItem(slot, list.removeAt(index).toItemStack())
            }

            return location
        }
    }

    interface ItemReward {
        fun toItemStack(): ItemStack
    }

    data class VanillaItemReward(
        val materialId: Int,
        val amount: Int,
    ): ItemReward {
        override fun toItemStack(): ItemStack {
            return ItemStack(Material.getMaterial(materialId), amount)
        }
    }

    data class MythicMobsItemReward(
        val mmid: String,
        val amount: Int
    ): ItemReward {
        override fun toItemStack(): ItemStack {
            TODO("Not yet implemented")
        }
    }

}