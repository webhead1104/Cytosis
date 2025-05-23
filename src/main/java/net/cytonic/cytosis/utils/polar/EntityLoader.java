package net.cytonic.cytosis.utils.polar;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class EntityLoader extends AnvilLoader {
    Path entitiesPath;

    public EntityLoader(@NotNull Path path) {
        super(path);
        this.entitiesPath = path.resolve("entities");
    }

    public EntityLoader(@NotNull String path) {
        this(Path.of(path));
    }

    @Override
    public @NotNull Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        var future = super.loadChunk(instance, chunkX, chunkZ);

        final int regionX = CoordConversion.chunkToRegion(chunkX);
        final int regionZ = CoordConversion.chunkToRegion(chunkZ);

        File entityFile = entitiesPath.resolve("r." + regionX + "." + regionZ + ".mca").toFile();
        if (!entityFile.exists()) return future;
        try (AccessibleRegionFile AccessibleRegionFile = new AccessibleRegionFile(entityFile.toPath())) {
            CompoundBinaryTag data = AccessibleRegionFile.readChunkData(chunkX, chunkZ);
            if (data != null) {
                ListBinaryTag entitiesList = data.getList("Entities");

                entitiesList.forEach(entityTag -> {
                    CompoundBinaryTag binaryTag = (CompoundBinaryTag) entityTag;
                    // read the entities namespace id from the binarytag
                    String id = binaryTag.getString("id");
                    EntityType entityType = EntityType.fromKey(id);
                    if (entityType == null) throw new RuntimeException("Unknown entity type from id " + id);

                    // read the pos and rotation tags
                    ListBinaryTag posTag = (ListBinaryTag) binaryTag.get("Pos");
                    ListBinaryTag rotationTag = (ListBinaryTag) binaryTag.get("Rotation");

                    // convert the pos to a double[]
                    double[] posList = new double[]{0, 0, 0};
                    if (posTag != null) {
                        for (int i = 0; i < posTag.size(); i++) {
                            DoubleBinaryTag tag = (DoubleBinaryTag) posTag.get(i);
                            posList[i] = tag.value();
                        }
                    }

                    // convert the rotation to a float[]
                    float[] rotationList = new float[]{0, 0};
                    if (rotationTag != null) {
                        for (int i = 0; i < rotationTag.size(); i++) {
                            FloatBinaryTag tag = (FloatBinaryTag) rotationTag.get(i);
                            rotationList[i] = tag.value();
                        }
                    }

                    // convert the pos and rotation arrays into a Pos
                    Pos pos = new Pos(
                            posList[0],
                            posList[1],
                            posList[2],
                            rotationList[0],
                            rotationList[1]
                    );
                    // create and spawn the entity
                    Entity entity = new Entity(entityType);
                    entity.setInstance(instance, pos);
                });
            }

        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return future;
    }
}
