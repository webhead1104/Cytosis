package net.cytonic.cytosis.server.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.minestomunited.entrypoint.player.PlayerData;
import dev.minestomunited.entrypoint.player.PlayerService;
import io.ebean.DB;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

public class PlayerServiceImpl implements PlayerService {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    @Blocking
    @Override
    public @Nullable PlayerData loadPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k ->
            DB.find(PlayerDataEntity.class, k));
    }

    @Blocking
    @Override
    public void updatePlayerData(PlayerData playerData) {
        PlayerSkin skin = playerData.playerSkin();

        // PlayerDataEntity uses a manually-assigned @Id, so Ebean treats every freshly
        // created instance as new and INSERTs it. Fetch the existing row first and only
        // construct a new entity when absent, so repeated calls for the same UUID update.
        PlayerDataEntity playerDataEntity = DB.find(PlayerDataEntity.class, playerData.uuid());
        if (playerDataEntity == null) {
            playerDataEntity = new PlayerDataEntity();
            playerDataEntity.uuid(playerData.uuid());
        }
        playerDataEntity.username(playerData.username());
        playerDataEntity.ip(playerData.ip());
        playerDataEntity.proxy(playerData.proxy());
        if (skin != null) {
            playerDataEntity.skinSignature(skin.signature());
            playerDataEntity.skinTextures(skin.textures());
        }
        playerDataEntity.version(playerData.version());
        playerDataEntity.save();
        playerDataMap.put(playerData.uuid(), playerData);
    }

    @Blocking
    @Override
    public void unloadPlayerData(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        if (data != null) {
            updatePlayerData(data);
        }
        playerDataMap.remove(playerId);
    }
}
