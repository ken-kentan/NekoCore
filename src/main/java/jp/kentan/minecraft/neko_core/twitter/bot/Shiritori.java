package jp.kentan.minecraft.neko_core.twitter.bot;

import com.ibm.icu.text.Transliterator;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.sql.SqlProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import java.io.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Shiritori {
    enum Result{WIN_N, WIN_NOT_MATCH, WIN_USED, LOSE, CONTINUE, NEW}

    private final static Pattern WORD_PATTERN = Pattern.compile(".*「(.+)」.*");

    private static final Pattern HIRAGANA = Pattern.compile("^[\\u3040-\\u309F]+$");
//	private static final String MATCH_KATAKANA = "^[\\u30A0-\\u30FF]+$";

    private SqlProvider mSql;
    private JapaneseTokenizer mTokenizer;
    private ReadingAttribute mReadingAttribute;
    private CharTermAttribute mCharTermAttribute;

    private Map<String, String> mDictionary = new LinkedHashMap<>(); //word, reading
    private final static Map<Character, Character> FIX_CHAR_MAP = new HashMap<Character, Character>(){
        {
            put('ァ', 'ア');
            put('ィ', 'イ');
            put('ゥ', 'ウ');
            put('ェ', 'エ');
            put('ォ', 'オ');
            put('ヵ', 'カ');
            put('ヶ', 'ケ');
            put('ッ', 'ツ');
            put('ャ', 'ヤ');
            put('ュ', 'ユ');
            put('ョ', 'ヨ');
            put('ヮ', 'ワ');
            put('ォ', 'オ');
            put('ヂ', 'ジ');
            put('ヅ', 'ズ');
        }
    };

    private List<String> mUsedWordList = new ArrayList<>();

    private Result mResult;
    private String mUserName;

    private String mEnemyWord = "", mResultWord;
    private char mPreviousChar = '\0';
    private int mUnknownWords = 0;

    Shiritori(Result result, String userName){
        mSql = new SqlProvider(ConfigManager.getSqlConfig());
        mUserName = userName;

        mResult = result;

        mTokenizer = new JapaneseTokenizer(null, true, JapaneseTokenizer.Mode.NORMAL);
        mReadingAttribute  = mTokenizer.addAttribute(ReadingAttribute.class);
        mCharTermAttribute = mTokenizer.addAttribute(CharTermAttribute.class);

        setupDictionary();
    }

    String getResultWord(){
        switch (mResult) {
            case WIN_N:
            case WIN_NOT_MATCH:
            case WIN_USED:
                printResult();
                return mEnemyWord;
            case LOSE:
                printResult();
                return null;
            case NEW:
                mPreviousChar = 'メ';
                return "しりとりはじめ";
            default:
                return mResultWord;
        }
    }

    Result getResultStatus() {
        return mResult;
    }

    boolean isContinue() {
        return mResult == Result.NEW || mResult == Result.CONTINUE;
    }

    private void setupDictionary(){
        ResultSet rs = mSql.query("SELECT word, reading, date FROM shiritori_words UNION SELECT word, reading, date FROM shiritori_words_learned WHERE user = '" + mUserName + "' ORDER BY date DESC");
        Log.print("fetching dictionary...");

        try {
            while (rs.next()) {
                mDictionary.put(rs.getString("word"), rs.getString("reading"));
            }
            Log.print("done(" + mDictionary.size() + ").");
        } catch (Exception e) {
            Log.print(e.getMessage());
        } finally {
            mSql.close();
        }


        if(mDictionary.size() <= 0){
            BufferedReader reader;
            Log.print("create dictionary from cache.");

            try{
                reader = new BufferedReader(new FileReader("./shiritori_cache.txt"));

                String line;
                while((line = reader.readLine()) != null){
                    if(!line.equals("")){
                        mDictionary.put(line, getReading(line));
                    }
                }
            }catch(Exception e){
                Log.print(e.getMessage());
            }
        }else{
            BufferedWriter writer;
            Log.print("creating cache...");
            try{
                writer = new BufferedWriter(new FileWriter("./shiritori_cache.txt"));

                for(Entry<String, String> entry : mDictionary.entrySet()){
                    writer.write(entry.getKey());
                    writer.newLine();
                }

                writer.close();
                Log.print("done.");
            }catch(Exception e){
                Log.warn(e.getMessage());
            }
        }

        Log.print("finish(" + mDictionary.size() + ").");
    }

    private String getWord(String text){
        Matcher matcher = WORD_PATTERN.matcher(text);

        if(matcher.matches() || matcher.find()){
            return matcher.group(1);
        }else{
            Log.warn("\"" + text + "\" is not match regex.");
            throw new IllegalStateException("\"" + text + "\" is not match regex.");
        }
    }

    void analyze(String text){
        String word = getWord(text);

        if(isEmpty(word)){
            return;
        }

        String reading = getReading(word);
        char firstChar = getFirstChar(reading);
        char lastChar = getLastChar(reading);

        Log.print("reading: " + reading);
        Log.print("prev:" + mPreviousChar + " first: " + firstChar + " last: " + lastChar);

        mEnemyWord = reading;

        if(mPreviousChar != '\0' && firstChar != mPreviousChar){
            mResult = Result.WIN_NOT_MATCH;
            return;
        }
        if(lastChar == 'ン'){
            mResult = Result.WIN_N;
            return;
        }

        if(HIRAGANA.matcher(word.replaceAll("ー", "")).matches()){ //るーる => ルール
            Log.print("overwrite word: " + word + " to " + reading);
            word = reading;
        }

        if(mUsedWordList.contains(word)){
            mEnemyWord = reading;
            mResult = Result.WIN_USED;
            return;
        }

        mUsedWordList.add(word);

        learnIfUnknown(word, reading);

        for(Entry<String, String> entry : mDictionary.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            if(lastChar == getFirstChar(value)){
                String keyCompare = key;

                if(HIRAGANA.matcher(key.replaceAll("ー", "")).matches()){ //るーる => ルール
                    Log.print("overwrite key: " + key + " to " + value);
                    keyCompare = value;
                }

                if(mUsedWordList.contains(keyCompare)){
                    continue;
                }

                mResultWord = key;
                mPreviousChar = getLastChar(value);

                mUsedWordList.add(keyCompare);

                mResult = Result.CONTINUE;

                Log.print("match: " + key);
                return;
            }
        }

        mResult = Result.LOSE;
    }

    private void printResult(){
        Log.print("------結果------");
        Log.print("辞書単語数: " + mDictionary.size());
        Log.print("使用単語数: " + mUsedWordList.size());
        Log.print("未知単語: " + mUnknownWords);
    }

    private String getReading(String word){
        StringBuilder builder =  new StringBuilder();

        try{
            mTokenizer.setReader(new StringReader(word));
            mTokenizer.reset();
            while(mTokenizer.incrementToken()){
                String reading = mReadingAttribute.getReading();

                if(reading == null) {
                    reading = mCharTermAttribute.toString();
                }

                builder.append(reading);
            }
            mTokenizer.close();
        } catch (IOException e) {
            Log.warn(e.getMessage());
        }

        return toKatakana(builder.toString());
    }

    private void learnIfUnknown(String word, String reading){
        if(mDictionary.containsKey(word)){
            return;
        }

        mSql.update("INSERT INTO `shiritori_words_learned` VALUES({date}, '" + mUserName + "', '" + word + "', '" + reading + "')");
        mSql.close();

        ++mUnknownWords;
        Log.print("learned: " + word + ", " + reading);
    }

    private String toKatakana(String word){
        Transliterator transliterator = Transliterator.getInstance("Hiragana-Katakana");
        return transliterator.transliterate(word);
    }

    private char getFirstChar(String str) {
        char c = str.charAt(0);

        for (int i = 0; i < str.length(); ++i) {
            c = str.charAt(i);

            if(FIX_CHAR_MAP.get(c) != null){
                c = FIX_CHAR_MAP.get(c);
            }

            if (c != 'ー') break;
        }

        return c;
    }

    private char getLastChar(String str) {
        char c = str.charAt(str.length() - 1);

        for (int i = str.length() - 1; i >= 0; --i) {
            c = str.charAt(i);

            if(FIX_CHAR_MAP.get(c) != null){
                c = FIX_CHAR_MAP.get(c);
            }

            if (c != 'ー') break;
        }

        return c;
    }

    private boolean isEmpty(String str){
        if(str == null || str.length() <= 0){
            mResultWord = "(null)";
            mResult = Result.WIN_NOT_MATCH;
            return true;
        }else{
            return false;
        }
    }
}
