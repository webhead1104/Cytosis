package net.cytonic.cytosis.managers;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor
public class InstanceManager {

    public CompletableFuture<Instance> loadDatabaseWorld(String databaseName, @Nullable UUID uuid) {
        CompletableFuture<Instance> future = new CompletableFuture<>();
        Cytosis.getDatabaseManager().getMysqlDatabase().getWorld(databaseName).whenComplete((world, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            InstanceContainer container = new InstanceContainer(uuid == null ? UUID.randomUUID() : uuid, DimensionType.OVERWORLD);
            container.setChunkLoader(new PolarLoader(world));
            future.complete(container);
        });
        return future;
    }

    public CompletableFuture<Instance> loadDatabaseWorld(String databaseName) {
        CompletableFuture<Instance> future = new CompletableFuture<>();
        Cytosis.getDatabaseManager().getMysqlDatabase().getWorld(databaseName).whenComplete((world, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            InstanceContainer container = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
            container.setChunkLoader(new PolarLoader(world));
            Cytosis.getMinestomInstanceManager().registerInstance(container);
            future.complete(container);
        });
        return future;
    }
}
