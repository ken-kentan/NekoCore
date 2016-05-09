package jp.kentan.minecraft.core;

public class ConfigManager {
	private static NekoCore nekoCore;
	
	public static void init(NekoCore _nekoCore){
		nekoCore = _nekoCore;
		
		TwitterBot.consumerKey       = nekoCore.getConfig().getString("Twitter.consumerKey");
		TwitterBot.consumerSecret    = nekoCore.getConfig().getString("Twitter.consumerSecret");
		TwitterBot.accessToken       = nekoCore.getConfig().getString("Twitter.accessToken");
		TwitterBot.accessTokenSecret = nekoCore.getConfig().getString("Twitter.accessTokenSecret");
		
		//init All List
		TwitterBot.nekoFaceList.clear();
		TwitterBot.msgPlayerActionList.clear();
		TwitterBot.msgUnkownCommandList.clear();
		TwitterBot.msgRejectCommandList.clear();
		TwitterBot.msgThanksList.clear();
		TwitterBot.msgLuckyList.clear();
		TwitterBot.msgMorningList.clear();
		TwitterBot.msgWeatherList.clear();
		TwitterBot.msgNyanList.clear();
		
		TwitterBot.nekoFaceList         = nekoCore.getConfig().getStringList("Bot.nekoFace");
		TwitterBot.msgPlayerActionList  = nekoCore.getConfig().getStringList("Bot.msgPlayerAction");
		TwitterBot.msgUnkownCommandList = nekoCore.getConfig().getStringList("Bot.msgUnknownCommand");
		TwitterBot.msgRejectCommandList = nekoCore.getConfig().getStringList("Bot.msgRejectCommand");
		TwitterBot.msgThanksList        = nekoCore.getConfig().getStringList("Bot.msgThanks");
		TwitterBot.msgLuckyList         = nekoCore.getConfig().getStringList("Bot.msgLucky");
		TwitterBot.msgMorningList       = nekoCore.getConfig().getStringList("Bot.msgGoodMorning");
		TwitterBot.msgWeatherList       = nekoCore.getConfig().getStringList("Bot.msgWeather");
		TwitterBot.msgNyanList          = nekoCore.getConfig().getStringList("Bot.msgNyan");
		
		nekoCore.getLogger().info("設定ファイルを正常に読み込みました。");
	}

}
