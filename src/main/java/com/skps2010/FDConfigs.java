package com.skps2010;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FDConfigs {
    public static FDConfig CFG;

    public static void load() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("fooddiversity.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.notExists(p)) {
                CFG = new FDConfig();
                Files.createDirectories(p.getParent());
                Files.writeString(p, gson.toJson(CFG));
            } else {
                CFG = gson.fromJson(Files.readString(p), FDConfig.class);
                if (CFG == null) CFG = new FDConfig();
            }
        } catch (IOException e) {
            CFG = new FDConfig(); // 失敗就用預設
        }
    }
}
