package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	private static NekoCore nekoCore;
	
	static final private Charset CONFIG_CHAREST=StandardCharsets.UTF_8;
	
	public static void init(NekoCore _nekoCore){
		nekoCore = _nekoCore;
		
		String confFilePath= nekoCore.getDataFolder() + File.separator + "config.yml";

		// 設定ファイルを開く
		try(Reader reader=new InputStreamReader(new FileInputStream(confFilePath),CONFIG_CHAREST)){

			// 設定データ入出力クラスを作る
			FileConfiguration conf=new YamlConfiguration();

			// 設定を読み込む
			conf.load(reader);
			
			TwitterBot.consumerKey       = conf.getString("Twitter.consumerKey");
			TwitterBot.consumerSecret    = conf.getString("Twitter.consumerSecret");
			TwitterBot.accessToken       = conf.getString("Twitter.accessToken");
			TwitterBot.accessTokenSecret = conf.getString("Twitter.accessTokenSecret");
			
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
			
			TwitterBot.nekoFaceList         = conf.getStringList("Bot.nekoFace");
			TwitterBot.msgPlayerActionList  = conf.getStringList("Bot.msgPlayerAction");
			TwitterBot.msgUnkownCommandList = conf.getStringList("Bot.msgUnknownCommand");
			TwitterBot.msgRejectCommandList = conf.getStringList("Bot.msgRejectCommand");
			TwitterBot.msgThanksList        = conf.getStringList("Bot.msgThanks");
			TwitterBot.msgLuckyList         = conf.getStringList("Bot.msgLucky");
			TwitterBot.msgMorningList       = conf.getStringList("Bot.msgGoodMorning");
			TwitterBot.msgWeatherList       = conf.getStringList("Bot.msgWeather");
			TwitterBot.msgNyanList          = conf.getStringList("Bot.msgNyan");
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		nekoCore.getLogger().info("設定ファイルを正常に読み込みました。");
	}

}
