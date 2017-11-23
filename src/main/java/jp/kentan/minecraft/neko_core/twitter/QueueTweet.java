package jp.kentan.minecraft.neko_core.twitter;

public class QueueTweet {
    private final String ID;
    final String TWEET;

    QueueTweet(String id, String tweet){
        ID = id;
        TWEET = tweet;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof String && ID.equals(obj);
    }
}
