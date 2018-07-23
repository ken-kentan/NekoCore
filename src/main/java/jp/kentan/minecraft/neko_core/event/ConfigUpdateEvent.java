package jp.kentan.minecraft.neko_core.event;

public interface ConfigUpdateEvent<T> {
    void onConfigUpdate(T data);
}
