// CravingState.java
package com.skps2010;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class CravingState extends PersistentState {
    // 每位玩家的目前想吃：itemId 與 下一次切換 tick
    public static record Entry(String itemId, long nextChangeTick, int eatenInRound) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("itemId").forGetter(Entry::itemId),
                Codec.LONG.fieldOf("nextChangeTick").forGetter(Entry::nextChangeTick),
                Codec.INT.fieldOf("eatenInRound").forGetter(Entry::eatenInRound)
        ).apply(i, Entry::new));
        Entry withCount(int c){ return new Entry(itemId, nextChangeTick, c); }
    }

    private final Map<UUID, Entry> data = new HashMap<>();

    public static final Codec<CravingState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Uuids.CODEC, Entry.CODEC)
                    .optionalFieldOf("data", Map.of())
                    .forGetter(s -> s.data)
    ).apply(i, st -> {
        CravingState s = new CravingState();
        s.data.putAll(st);
        return s;
    }));

    public static final PersistentStateType<CravingState> TYPE =
            new PersistentStateType<>("fooddiversity_craving", CravingState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);

    public static CravingState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public void set(UUID uuid, Entry e) {
        data.put(uuid, e);
        markDirty();
    }

    public Entry get(UUID uuid) { return data.get(uuid); }
}
