package jp.kentan.minecraft.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

import com.ibm.icu.text.Transliterator;

public class Shiritori {
	public enum RESULT{WIN_N, WIN_NOTMATCH, WIN_USED, LOSE, CONTINUE};
	
	private static final String MATCH_HIRAGANA = "^[\\u3040-\\u309F]+$";
//	private static final String MATCH_KATAKANA = "^[\\u30A0-\\u30FF]+$";
	
	private SQLManager sql;
	private Tokenizer tokenizer = null;
	
	private Map<String, String> dictionary = new HashMap<String, String>(); //word, reading
	private Map<Character, Character> lowerCharMap = new HashMap<Character, Character>();
	
	private List<String> usedWords = new ArrayList<String>();
	
	private RESULT currentResult = RESULT.CONTINUE;
	private String user, matchWord = null, userWord = "";
	private char prevLastChar = '\0';
	private boolean isOffline = false , isFinishGame = false;
	private int unknownWords = 0;
	
	public Shiritori(String user){
		sql = new SQLManager();
		tokenizer = Tokenizer.builder().build();
		this.user = user;
		
		initDictionary();
		
		//小文字
		lowerCharMap.put('ァ', 'ア');
		lowerCharMap.put('ィ', 'イ');
		lowerCharMap.put('ゥ', 'ウ');
		lowerCharMap.put('ェ', 'エ');
		lowerCharMap.put('ォ', 'オ');
		lowerCharMap.put('ヵ', 'カ');
		lowerCharMap.put('ヶ', 'ケ');
		lowerCharMap.put('ッ', 'ツ');
		lowerCharMap.put('ャ', 'ヤ');
		lowerCharMap.put('ュ', 'ユ');
		lowerCharMap.put('ョ', 'ヨ');
		lowerCharMap.put('ヮ', 'ワ');
		lowerCharMap.put('ォ', 'オ');
		
		checkOverlap();
	}
	
	public String getUser(){
		return this.user;
	}
	
	public String getResult(){
		switch (currentResult) {
		case WIN_N:
			isFinishGame = true;
			return userWord + "は「ン」が付いてるよ！ わーぃ！ぼくの勝ち！！";
		case WIN_NOTMATCH:
			isFinishGame = true;
			return userWord + "は前の単語に繋がってないよ！ わーぃ！勝った！！";
		case WIN_USED:
			isFinishGame = true;
			return userWord + "はもう使われてるよ！ いぇーい！勝った！！";
		case LOSE:
			isFinishGame = true;
			return "負けたーぁ。強いね！";
		default:
			if(matchWord == null){
				prevLastChar = 'メ';
				return "ぼくからね！ 「しりとりはじめ」";
			}
			return "「" + matchWord + "」";
		}
	}
	
	public boolean isFinish(){
		return isFinishGame;
	}
	
	private void initDictionary(){
		dictionary.clear();
		ResultSet rs = sql.query("SELECT * FROM `shiritori_words`");
		NekoCore.LOG.info("基礎辞書取得中...");

		try {
			while (rs.next()) {
				String word = rs.getString("word");
				String reading = rs.getString("reading");

				dictionary.put(word, reading);
			}
			NekoCore.LOG.info("取得中完了.");
		} catch (Exception e) {
			NekoCore.LOG.info(e.getMessage());
		} finally{
			sql.close();
		}
		
		rs = sql.query("SELECT * FROM `shiritori_words_learned` WHERE `user` = '" + user + "'");
		NekoCore.LOG.info("学習辞書取得中(" + user + ")...");

		try {
			while (rs.next()) {
				if(rs.getString("user").equals(user)){
					String word = rs.getString("word");
					String reading = rs.getString("reading");
	
					dictionary.put(word, reading);
				}
			}
			NekoCore.LOG.info("取得中完了.");
		} catch (Exception e) {
			NekoCore.LOG.info(e.getMessage());
		} finally{
			sql.close();
		}
		
		if(dictionary.size() <= 0){
			BufferedReader reader;
			NekoCore.LOG.info("前回のキャッシュから辞書を作成します.");
			
			isOffline = true;
			
			try{
				reader = new BufferedReader(new FileReader("./cache.txt"));
				
				String line = null;
				while((line = reader.readLine()) != null){
					if(!line.equals("")){
						dictionary.put(line, getReading(line));
					}
				}
			}catch(Exception e){
				NekoCore.LOG.info(e.getMessage());
			}
		}else{
			BufferedWriter writer;
			NekoCore.LOG.info("キャッシュを作成中...");
			try{
				writer = new BufferedWriter(new FileWriter("./cache.txt"));
				
				for(Entry<String, String> entry : dictionary.entrySet()){
					writer.write(entry.getKey());
					writer.newLine();
				}
				
				writer.close();
				NekoCore.LOG.info("作成完了.");
			}catch(Exception e){
				
			}
		}
		
		NekoCore.LOG.info("辞書取得完了(" + dictionary.size() + "件).");
	}
	
	public void analyze(String word){
		String reading = getReading(word);
		
		NekoCore.LOG.info("reading: " + reading);
		NekoCore.LOG.info("prev:" + prevLastChar + " first: " + getFirstChar(reading) + " last: " + getLastChar(reading));

		char firstChar = getFirstChar(reading);
		char lastChar = getLastChar(reading);
		
		if(prevLastChar != '\0' && firstChar != prevLastChar){
			userWord = reading;
			currentResult = RESULT.WIN_NOTMATCH;
			return;
		} else if(lastChar == 'ン'){
			userWord = reading;
			currentResult = RESULT.WIN_N;
			return;
		}
		
		if(isUnknown(word)){
			learning(word, reading);
		}
		
		for(Entry<String, String> entry : dictionary.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			
			if(lastChar == getFirstChar(value)){				
				if(usedWords.contains(word)){
					userWord = reading;
					currentResult = RESULT.WIN_USED;
					return;
				}
				
				if(usedWords.contains(key)){
					continue;
				}
				
				if(isMatches(MATCH_HIRAGANA, word)){
					NekoCore.LOG.info("overwrite: " + word + " to " + reading);
					word = reading;
				}

				matchWord = key;
				
				NekoCore.LOG.info("match: " + key);
				prevLastChar = getLastChar(value);
				
				usedWords.add(word);
				usedWords.add(key);
				
				currentResult = RESULT.CONTINUE;
				return;
			}
		}
		
		currentResult = RESULT.LOSE;
	}
	
	public void printResult(){
		NekoCore.LOG.info("------結果------");
		NekoCore.LOG.info("辞書単語数: " + dictionary.size());
		NekoCore.LOG.info("使用単語数: " + usedWords.size());
		NekoCore.LOG.info("未知単語数: " + unknownWords);
	}

	private String getReading(String str){
		List<Token> tokens = tokenizer.tokenize(str);
		
		StringBuilder strBuilder = new StringBuilder();
		for(Token token : tokens){
			if(token.getPartOfSpeech().contains("記号")){
				continue;
			}
			
			String readingToken = token.getReading();
			if(readingToken != null){//辞書語
				strBuilder.append(readingToken);
			}else{                   //未知語
				strBuilder.append(token.getSurfaceForm());
			}
		}
		
		return toKatakana(strBuilder.toString());
	}
	
	private void learning(String word, String reading){		
		if(isOffline){
			NekoCore.LOG.info("offline.");
			return;
		}
		
		if(isMatches(MATCH_HIRAGANA, word)){
			if(dictionary.get(reading) != null){
				NekoCore.LOG.info("similar: " + word);
				return;
			}
		}
		
		sql.update("INSERT INTO `shiritori_words_learned` VALUES({date}, '" + user + "', '" + word + "', '" + reading + "')");
		sql.close();
		
		++unknownWords;
		NekoCore.LOG.info("learned: " + word + ", " + reading);
	}
	
	private boolean isUnknown(String word){
		//check unknown word
		for(Entry<String, String> entry : dictionary.entrySet()){
			if(entry.getKey().equals(word)) return false;
		}
		
		return true;
	}
	
	public void checkOverlap(){
		NekoCore.LOG.info("辞書重複を検索中...");
		for(Entry<String, String> entry : dictionary.entrySet()){
			if(isMatches(MATCH_HIRAGANA, entry.getKey())){
				String katakana = toKatakana(entry.getKey());
				if(dictionary.get(katakana) != null){
					NekoCore.LOG.warning("hit: " + katakana);
				}
			}
		}
		NekoCore.LOG.info("検索終了.");
	}
	
	private boolean isMatches(String type, String word){
		return word.matches(type);
	}
	
	private String toKatakana(String word){
		Transliterator transliterator = Transliterator.getInstance("Hiragana-Katakana");
		return transliterator.transliterate(word);
	}
	
	private char getFirstChar(String str) {
		char c = str.charAt(0);
		
		for (int i = 0; i < str.length(); ++i) {
			c = str.charAt(i);

			if(lowerCharMap.get(c) != null){
				c = lowerCharMap.get(c);
			}

			if (c != 'ー') break;
		}

		return c;
	}
	
	private char getLastChar(String str) {
		char c = str.charAt(str.length() - 1);
		
		for (int i = str.length() - 1; i >= 0; --i) {
			c = str.charAt(i);

			if(lowerCharMap.get(c) != null){
				c = lowerCharMap.get(c);
			}

			if (c != 'ー') break;
		}

		return c;
	}
}
