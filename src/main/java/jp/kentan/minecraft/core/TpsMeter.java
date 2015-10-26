//Referenced from https://www.spigotmc.org/threads/how-to-get-spigots-tick-count.1845/

package jp.kentan.minecraft.core;

import org.bukkit.scheduler.BukkitRunnable;

public class TpsMeter extends BukkitRunnable {

	public static double tps = -1;
	   
    private static long sec;
    private static long currentSec = 0;
    private static int  ticks = 0;
   
    @Override
    public void run() {
        sec = (System.currentTimeMillis() / 1000);
   
        if(currentSec == sec) {
            ticks++;
        } else {
            currentSec = sec;
            tps = (tps == 0 ? ticks : ((tps + ticks) / 2));
            ticks = 0;
        }
    }
}
