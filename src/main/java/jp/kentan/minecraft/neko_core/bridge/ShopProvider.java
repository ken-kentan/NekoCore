package jp.kentan.minecraft.neko_core.bridge;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;

public class ShopProvider {

    private final ShopUtils SHOP_UTILS;

    public ShopProvider() {
        SHOP_UTILS = ShopChest.getInstance().getShopUtils();
    }

    public void removeShopInRegion(World world, ProtectedRegion region) {
        for (Shop shop : SHOP_UTILS.getShops()) {
            if (shop.getLocation().getWorld() != world){
                continue;
            }

            Location loc = shop.getLocation();
            if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                InventoryHolder inventoryHolder = shop.getInventoryHolder();

                if (inventoryHolder != null) {
                    inventoryHolder.getInventory().clear();
                }
                SHOP_UTILS.removeShop(shop, true);
            }
        }
    }
}
