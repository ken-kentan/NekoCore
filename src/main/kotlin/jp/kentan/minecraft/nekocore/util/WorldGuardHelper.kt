package jp.kentan.minecraft.nekocore.util

import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.epiceric.shopchest.ShopChest
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player

fun ProtectedRegion.setMember(player: Player?) {
    members = DefaultDomain().apply {
        if (player != null) {
            addPlayer(player.uniqueId)
        }
    }
}

fun ProtectedRegion.containBlock(world: World): Boolean {
    val min = minimumPoint
    val max = maximumPoint

    val minX = min.blockX
    val minY = min.blockY
    val minZ = min.blockZ

    val maxX = max.blockX
    val maxY = max.blockY
    val maxZ = max.blockZ

    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                if (world.getBlockAt(x, y, z).state.block.type != Material.AIR) {
                    return true
                }
            }
        }
    }

    return false
}

private val shopUtils by lazy { ShopChest.getInstance().shopUtils }

@Throws(Exception::class)
fun ProtectedRegion.clean(world: World) {
    shopUtils.shopsCopy
        .asSequence()
        .filter {
            it.location.world == world && contains(it.location.blockX, it.location.blockY, it.location.blockZ)
        }
        .forEach { shop ->
            shopUtils.removeShop(shop, true)
        }

    val min = minimumPoint
    val max = maximumPoint

    val minX = min.blockX
    val minY = min.blockY
    val minZ = min.blockZ

    val maxX = max.blockX
    val maxY = max.blockY
    val maxZ = max.blockZ

    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                world.getBlockAt(x, y, z).type = Material.AIR
            }
        }
    }
}