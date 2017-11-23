package jp.kentan.minecraft.neko_core.config;

public interface ConfigUpdateListener<T> {
    void onUpdate(T data);
}
