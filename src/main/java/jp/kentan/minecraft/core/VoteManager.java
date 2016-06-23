package jp.kentan.minecraft.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class VoteManager {
	private NekoCore nekoCore = null;

	public static int maxSuccession = 7;

	int[] a = new int[5];

	public static List<List<String>> rewardList = new ArrayList<List<String>>();
	public static List<String> rewardDetailList = new ArrayList<String>();

	public VoteManager(NekoCore nekoCore) {
		this.nekoCore = nekoCore;

		nekoCore.getLogger().info("Successfully initialized the VoteManager.");
	}

	public void test() {
		nekoCore.getLogger().info("a");
	}

	public void voteThisServer(String strPlayer) {
		Date dateLastVoted = null, dateNow = new Date();
		Player player = null;
		int currentSuccession = 1;

		if ((player = getPlayerOnline(strPlayer)) == null) { //offline
			nekoCore.config.writeLastVotedDate(strPlayer, dateNow);
			nekoCore.config.writeSuccessionVote(strPlayer, currentSuccession);
			return;
		}

		if ((dateLastVoted = nekoCore.config.getLastVotedDate(strPlayer)) != null
				&& differenceDays(dateNow, dateLastVoted) == 1) {
			currentSuccession = nekoCore.config.getSuccessionVote(strPlayer) + 1;
		}

		currentSuccession = Math.min(currentSuccession, maxSuccession);

		runRewardCommand(currentSuccession, strPlayer);

		nekoCore.config.writeLastVotedDate(strPlayer, dateNow);
		nekoCore.config.writeSuccessionVote(strPlayer, currentSuccession);

		player.sendMessage(NekoCore.nc_tag + "投票ありがとにゃ" + nekoCore.tw.bot.getNekoFace() + ChatColor.AQUA + " "
				+ currentSuccession + "day" + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + "をゲット！");
		player.sendMessage(NekoCore.nc_tag + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + ": "
				+ rewardDetailList.get(currentSuccession - 1));
		player.sendMessage(NekoCore.nc_tag + "ステータス: " + generateSuccessionMessage(currentSuccession, maxSuccession));
		player.sendMessage(NekoCore.nc_tag + ChatColor.GRAY + "1日1回、続けて投票するとステータスがたまります.");
	}

	private void runRewardCommand(int day, String strPlayer) {
		Server server = nekoCore.getServer();

		int i = 0;
		for (List<String> dayRewardList : rewardList) {
			if (++i == day) {
				for (String command : dayRewardList) {
					server.dispatchCommand(server.getConsoleSender(), command.replace("{player}", strPlayer));
				}

				return;
			}

			nekoCore.getLogger().info(i + " , " + day);
		}
	}

	private String generateSuccessionMessage(int current, int max) {
		StringBuilder builder = new StringBuilder();

		for (int i = 1; i <= max; ++i) {
			if (i <= current) {
				builder.append(ChatColor.AQUA + String.valueOf(i) + "day" + ChatColor.GRAY + "[" + ChatColor.YELLOW
						+ "★" + ChatColor.GRAY + "] ");
			} else {
				builder.append(ChatColor.DARK_GRAY + String.valueOf(i) + "day[☆] ");
			}
		}

		return builder.toString();
	}

	public Player getPlayerOnline(String strPlayer) {
		Player player = null;
		try {
			player = Bukkit.getServer().getPlayer(strPlayer);
		} catch (Exception e) {
			return null;
		}

		return player;
	}

	public static int differenceDays(Date date1, Date date2) {
		long datetime1 = date1.getTime();
		long datetime2 = date2.getTime();
		long one_date_time = 1000 * 60 * 60 * 24;
		long diffDays = (datetime1 - datetime2) / one_date_time;
		return (int) diffDays;
	}

}
