package jp.kentan.minecraft.neko_core.component.zone;

public enum ZoneType {
    BUY_UP("買い切り"),
    RENTAL("レンタル");

    private final String NAME;

    ZoneType(String name) {
        NAME = name;
    }

    public String getName() {
        return NAME;
    }
}
