package com.skps2010;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FDConfigs {
    public static FDConfig CFG;

    public static void load() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("fooddiversity.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.notExists(p)) {
                CFG = new FDConfig();
            } else {
                CFG = gson.fromJson(Files.readString(p), FDConfig.class);
                if (CFG == null) CFG = new FDConfig();
            }
            // 每次載入後回寫一次，以補上新欄位
            Files.createDirectories(p.getParent());
            Files.writeString(p, gson.toJson(CFG));
        } catch (IOException e) {
            CFG = new FDConfig();
        }
    }
}
