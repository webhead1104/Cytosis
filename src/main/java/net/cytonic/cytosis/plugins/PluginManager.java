package net.cytonic.cytosis.plugins;

import net.cytonic.cytosis.plugins.dependencies.DependencyUtils;
import net.cytonic.cytosis.plugins.dependencies.PluginDependency;
import net.cytonic.cytosis.plugins.loader.JavaPluginLoader;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles loading plugins and provides a registry for loaded plugins.
 */
public class PluginManager {
    private final Map<String, PluginContainer> pluginsById = new LinkedHashMap<>();
    private final Map<Object, PluginContainer> pluginInstances = new IdentityHashMap<>();
    Logger logger = LoggerFactory.getLogger("Plugin Manager");

    private void registerPlugin(PluginContainer plugin) {
        pluginsById.put(plugin.getDescription().getId(), plugin);
        Optional<CytosisPlugin> instance = plugin.getInstance();
        instance.ifPresent(o -> {
            pluginInstances.put(o, plugin);
            o.initialize();
        });
    }

    /**
     * Loads all plugins from the specified {@code directory}.
     *
     * @param directory the directory to load from
     * @throws IOException if we could not open the directory
     */
    public void loadPlugins(Path directory) throws IOException {
        checkNotNull(directory, "directory");
        checkArgument(directory.toFile().isDirectory(), "provided path isn't a directory");

        Map<String, PluginDescription> foundCandidates = new LinkedHashMap<>();
        JavaPluginLoader loader = new JavaPluginLoader();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, p -> p.toFile().isFile() && p.toString().endsWith(".jar"))) {
            for (Path path : stream) {
                try {
                    PluginDescription candidate = loader.loadCandidate(path);

                    // If we found a duplicate candidate (with the same ID), don't load it.
                    PluginDescription maybeExistingCandidate = foundCandidates.putIfAbsent(candidate.getId(), candidate);

                    if (maybeExistingCandidate != null) {
                        logger.error("Refusing to load plugin at path {} since we already " + "loaded a plugin with the same ID {} from {}", candidate.getSource().map(Objects::toString).orElse("<UNKNOWN>"), candidate.getId(), maybeExistingCandidate.getSource().map(Objects::toString).orElse("<UNKNOWN>"));
                    }
                } catch (Throwable e) {
                    logger.error("Unable to load plugin {}", path, e);
                }
            }
        }

        if (foundCandidates.isEmpty()) {
            // No plugins found
            return;
        }

        List<PluginDescription> sortedPlugins = DependencyUtils.sortCandidates(new ArrayList<>(foundCandidates.values()));

        Map<String, PluginDescription> loadedCandidates = new HashMap<>();
        Map<String, PluginContainer> pluginContainers = new LinkedHashMap<>();
        // Now load the plugins
        pluginLoad:
        for (PluginDescription candidate : sortedPlugins) {
            // Verify dependencies
            for (PluginDependency dependency : candidate.getDependencies()) {
                if (!dependency.isOptional() && !loadedCandidates.containsKey(dependency.getId())) {
                    logger.error("Can't load plugin {} due to missing dependency {}", candidate.getId(), dependency.getId());
                    continue pluginLoad;
                }
            }

            try {
                PluginDescription realPlugin = loader.createPluginFromCandidate(candidate);
                PluginContainer container = new PluginContainer(realPlugin);
                pluginContainers.put(realPlugin.getId(), container);
                loadedCandidates.put(realPlugin.getId(), realPlugin);
            } catch (Throwable e) {
                logger.error("Can't create module for plugin {}", candidate.getId(), e);
            }
        }


        for (Map.Entry<String, PluginContainer> plugin : pluginContainers.entrySet()) {
            PluginContainer container = plugin.getValue();
            PluginDescription description = container.getDescription();

            try {
                loader.createPlugin(container);
            } catch (Throwable e) {
                logger.error("Can't create plugin {}", description.getId(), e);
                continue;
            }

            logger.info("Loaded plugin {} {} by {}", description.getId(), description.getVersion().orElse("<UNKNOWN>"), description.getAuthors());
            registerPlugin(container);
        }
    }

    public Optional<PluginContainer> fromInstance(Object instance) {
        checkNotNull(instance, "instance");

        if (instance instanceof PluginContainer) {
            return Optional.of((PluginContainer) instance);
        }

        return Optional.ofNullable(pluginInstances.get(instance));
    }

    public Optional<PluginContainer> getPlugin(String id) {
        checkNotNull(id, "id");
        return Optional.ofNullable(pluginsById.get(id));
    }

    public Collection<PluginContainer> getPlugins() {
        return Collections.unmodifiableCollection(pluginsById.values());
    }

    public boolean isLoaded(String id) {
        return pluginsById.containsKey(id);
    }

    public void addToClasspath(Object plugin, Path path) {
        checkNotNull(plugin, "instance");
        checkNotNull(path, "path");
        Optional<PluginContainer> optContainer = fromInstance(plugin);
        checkArgument(optContainer.isPresent(), "plugin is not loaded");
        Optional<?> optInstance = optContainer.get().getInstance();
        checkArgument(optInstance.isPresent(), "plugin has no instance");

        ClassLoader pluginClassloader = optInstance.get().getClass().getClassLoader();
        if (pluginClassloader instanceof PluginClassLoader) {
            ((PluginClassLoader) pluginClassloader).addPath(path);
        } else {
            throw new UnsupportedOperationException("Operation is not supported on non-Java Cytosis plugins.");
        }
    }

    public void unloadPlugins() {
        for (PluginContainer value : pluginsById.values()) {
            value.getInstance().ifPresent(CytosisPlugin::shutdown);
        }
    }
}
