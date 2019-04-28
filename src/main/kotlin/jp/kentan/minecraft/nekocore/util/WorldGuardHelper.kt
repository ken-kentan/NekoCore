package jp.kentan.minecraft.nekocore.util

import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.epiceric.shopchest.ShopChest
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Container
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

    val halfX = (max.blockX - min.blockX + 1) / 2.0
    val halfY = (max.blockY - min.blockY + 1) / 2.0
    val halfZ = (max.blockZ - min.blockZ + 1) / 2.0

    val center = Location(world, min.blockX + halfX, min.blockY + halfY, min.blockZ + halfZ)

    // Remove entity
    world.getNearbyEntities(center, halfX, halfY, halfZ).forEach { it.remove() }

    // Remove block
    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                val blockState = world.getBlockAt(x, y, z).state

                if (blockState is Container) {
                    blockState.inventory.clear()
                }

                blockState.block.setType(Material.AIR, false)
            }
        }
    }
}