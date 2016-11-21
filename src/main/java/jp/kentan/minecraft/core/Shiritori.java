package jp.kentan.minecraft.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import com.ibm.icu.text.Transliterator;

class Shiritori {
	enum RESULT{WIN_N, WIN_NOTMATCH, WIN_USED, LOSE, CONTINUE, NEW};
	
	private static final String MATCH_HIRAGANA = "^[\\u3040-\\u309F]+$";
//	private static final String MATCH_KATAKANA = "^[\\u30A0-\\u30FF]+$";
	
	private SQLManager sql;
	private JapaneseTokenizer tokenizer = null;
	private ReadingAttribute readingAttribute;
	private CharTermAttribute charTermAttribute;
	
	private Map<String, String> dictionary = new LinkedHashMap<>(); //word, reading
	private Map<Character, Character> fixCharMap = new HashMap<>();
	
	private List<String> usedWords = new ArrayList<>();
	
	private RESULT currentResult;
	private String user, matchWord = null, userWord = "";
	private char prevLastChar = '\0';
	private boolean isOffline = false , isFinishGame = false;
	private int unknownWords = 0;
	
	Shiritori(RESULT result, String user){
		sql = new SQLManager();
		this.user = user;
		this.currentResult = result;
		
		tokenizer = new JapaneseTokenizer(null, true, JapaneseTokenizer.Mode.NORMAL);
		readingAttribute  = tokenizer.addAttribute(ReadingAttribute.class);
		charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		
		initDictionary();
		checkOverlap();
		
		//小文字
		fixCharMap.put('ァ', 'ア');
		fixCharMap.put('ィ', 'イ');
		fixCharMap.put('ゥ', 'ウ');
		fixCharMap.put('ェ', 'エ');
		fixCharMap.put('ォ', 'オ');
		fixCharMap.put('ヵ', 'カ');
		fixCharMap.put('ヶ', 'ケ');
		fixCharMap.put('ッ', 'ツ');
		fixCharMap.put('ャ', 'ヤ');
		fixCharMap.put('ュ', 'ユ');
		fixCharMap.put('ョ', 'ヨ');
		fixCharMap.put('ヮ', 'ワ');
		fixCharMap.put('ォ', 'オ');
		fixCharMap.put('ヂ', 'ジ');
		fixCharMap.put('ヅ', 'ズ');
	}
	
	String getUser(){
		return this.user;
	}
	
	RESULT getResultStatus() {
		return currentResult;
	}
	
	String getResultWord(){
		switch (currentResult) {
		case WIN_N:
		case WIN_NOTMATCH:
		case WIN_USED:
			isFinishGame = true;
			return userWord;
		case LOSE:
			isFinishGame = true;
			return "";
		case NEW:
			prevLastChar = 'メ';
			return "しりとりはじめ";
		default:
			return matchWord;
		}
	}
	
	boolean isFinish(){
		if(isFinishGame) destroy();
		return isFinishGame;
	}
	
	private void destroy() {
		printResult();
		dictionary.clear();
		fixCharMap.clear();
		usedWords.clear();
	}
	
	private void initDictionary(){
		Map<String, String> dictionaryBaseWords = new LinkedHashMap<>();
		dictionary.clear();
		ResultSet rs = sql.query("SELECT * FROM `shiritori_words`");
		NekoCore.LOG.info("基礎辞書取得中...");

		try {
			while (rs.next()) {
				String word = rs.getString("word");
				String reading = rs.getString("reading");

				dictionaryBaseWords.put(word, reading);
			}
			NekoCore.LOG.info("取得完了(" + dictionaryBaseWords.size() + "件).");
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
			NekoCore.LOG.info("取得完了(" + dictionary.size() + "件).");
		} catch (Exception e) {
			NekoCore.LOG.info(e.getMessage());
		} finally {
			sql.close();
		}

		dictionary.putAll(dictionaryBaseWords);
		
		if(dictionary.size() <= 0){
			BufferedReader reader;
			NekoCore.LOG.info("前回のキャッシュから辞書を作成します.");
			
			isOffline = true;
			
			try{
				reader = new BufferedReader(new FileReader("./cache.txt"));
				
				String line;
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
				NekoCore.LOG.warning(e.getMessage());
			}
		}
		
		NekoCore.LOG.info("辞書取得完了(" + dictionary.size() + "件).");
	}
	
	void analyze(String word){
		if(isEmpty(word)) return;

		String reading = getReading(word);
		char firstChar = getFirstChar(reading);
		char lastChar = getLastChar(reading);
		
		NekoCore.LOG.info("reading: " + reading);
		NekoCore.LOG.info("prev:" + prevLastChar + " first: " + firstChar + " last: " + lastChar);
		
		if(prevLastChar != '\0' && firstChar != prevLastChar){
			userWord = reading;
			currentResult = RESULT.WIN_NOTMATCH;
			return;
		} else if(lastChar == 'ン'){
			userWord = reading;
			currentResult = RESULT.WIN_N;
			return;
		}
		
		if(isUnknown(word)) learnWord(word, reading);

		for(Entry<String, String> entry : dictionary.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			String keyCompare = key;

			if(lastChar == getFirstChar(value)){
				if(usedWords.contains(word)){
					userWord = reading;
					currentResult = RESULT.WIN_USED;
					return;
				}

				if(isMatches(MATCH_HIRAGANA, key)){
					NekoCore.LOG.info("overwrite key: " + key + " to " + value);
					keyCompare = value;
				}

				if(isMatches(MATCH_HIRAGANA, word)){
					NekoCore.LOG.info("overwrite word: " + word + " to " + reading);
					word = reading;
				}

				if(usedWords.contains(keyCompare)){
					continue;
				}

				matchWord = key;

				NekoCore.LOG.info("match: " + key);
				prevLastChar = getLastChar(value);

				usedWords.add(word);
				usedWords.add(keyCompare);
				
				currentResult = RESULT.CONTINUE;
				return;
			}
		}

		currentResult = RESULT.LOSE;
	}
	
	private void printResult(){
		NekoCore.LOG.info("------結果------");
		NekoCore.LOG.info("辞書単語数: " + dictionary.size());
		NekoCore.LOG.info("使用単語数: " + usedWords.size());
		NekoCore.LOG.info("未知単語: " + unknownWords);
	}
	
	private String getReading(String word){
		StringBuilder builder =  new StringBuilder();
		
		try{
            tokenizer.setReader(new StringReader(word));
            tokenizer.reset();
            while(tokenizer.incrementToken()){
            	String reading = readingAttribute.getReading();
            	
            	if(reading == null) {
            		reading = charTermAttribute.toString();
            	}
            	
            	builder.append(reading);
            }
            tokenizer.close();
        } catch (IOException e) {
            NekoCore.LOG.warning(e.getMessage());
        }
		
		return toKatakana(builder.toString());
	}
	
	private void learnWord(String word, String reading){
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
	
	private void checkOverlap(){
		NekoCore.LOG.info("辞書重複を検索中...");
		dictionary.entrySet().stream().filter(entry -> isMatches(MATCH_HIRAGANA, entry.getKey())).forEach(entry -> {
			String katakana = toKatakana(entry.getKey());
			if (dictionary.get(katakana) != null) {
				NekoCore.LOG.warning("hit: " + katakana);
			}
		});
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

			if(fixCharMap.get(c) != null){
				c = fixCharMap.get(c);
			}

			if (c != 'ー') break;
		}

		return c;
	}
	
	private char getLastChar(String str) {
		char c = str.charAt(str.length() - 1);
		
		for (int i = str.length() - 1; i >= 0; --i) {
			c = str.charAt(i);

			if(fixCharMap.get(c) != null){
				c = fixCharMap.get(c);
			}

			if (c != 'ー') break;
		}

		return c;
	}

	private boolean isEmpty(String str){
		if(str == null || str.length() <= 0){
			matchWord = "(null)";
			currentResult = RESULT.WIN_NOTMATCH;
			return true;
		}else{
			return false;
		}
	}
}
