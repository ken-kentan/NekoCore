package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.component.AdvertiseFrequency;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerConfigProvider extends BaseConfig {

    private final String DATA_FOLDER_PATH;

    private final DateTimeFormatter OLD_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.JAPAN);


    PlayerConfigProvider(File dataFolder) {
        DATA_FOLDER_PATH = dataFolder + File.separator + "players" + File.separator;
    }

    public ZonedDateTime getLastServerVoteDate(UUID uuid) {
        changePlayerConfig(uuid);

        String strDate = (String) super.get("Vote.date", null);

        if (strDate == null) {
            return null;
        }


        //新フォーマット
        try {
            return ZonedDateTime.parse(strDate);
        } catch (Exception ignored) {
        }

        //旧フォーマット互換
        return ZonedDateTime.from(OLD_DATE_FORMATTER.parse(strDate));
    }

    public int getServerVoteContinuous(UUID uuid) {
        changePlayerConfig(uuid);

        return (int) super.get("Vote.continuous", 1);
    }

    public AdvertiseFrequency getAdvertiseFrequency(UUID uuid) {
        changePlayerConfig(uuid);

        try {
            return AdvertiseFrequency.valueOf((String) super.get("Advertisement.frequency", AdvertiseFrequency.NORMAL.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return AdvertiseFrequency.NORMAL;
        }
    }

    public List<String> getStackCommandList(UUID uuid) {
        changePlayerConfig(uuid);
        return super.getStringList("stackCommands");
    }

    public boolean addStackCommands(UUID uuid, List<String> commandList) {
        changePlayerConfig(uuid);

        List<String> stackCommandList = super.getStringList("stackCommands");
        stackCommandList.addAll(commandList);

        return save(uuid, new HashMap<String, Object>() {
            {
                put("stackCommands", stackCommandList);
            }
        });
    }

    public boolean saveServerVoteData(UUID uuid, ZonedDateTime date, int continuous) {
        return save(uuid, new LinkedHashMap<String, Object>() {
            {
                put("Vote.date", date.toString());
                put("Vote.continuous", continuous);
            }
        });
    }

    public boolean saveAdvertiseFrequency(UUID uuid, AdvertiseFrequency freq) {
        return save(uuid, new HashMap<String, Object>() {
            {
                put("Advertisement.frequency", freq.toString());
            }
        });
    }

    public boolean save(UUID uuid, Map<String, Object> dataMap) {
        File file = new File(DATA_FOLDER_PATH + uuid + ".yml");

        try {
            if (!file.exists() && file.createNewFile()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        super.mConfigFile = file;

        return super.save(dataMap);
    }

    private void changePlayerConfig(UUID uuid) {
        super.mConfigFile = new File(DATA_FOLDER_PATH + uuid + ".yml");
    }
}
