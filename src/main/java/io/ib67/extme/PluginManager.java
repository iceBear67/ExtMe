package io.ib67.extme;

import io.ib67.extme.constructor.SimpleConstructor;
import io.ib67.extme.exception.IllegalDependLoopException;
import io.ib67.extme.exception.InvalidDependency;
import io.ib67.extme.plugin.Plugin;
import io.ib67.extme.plugin.PluginDescription;
import io.ib67.extme.plugin.PluginLoader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class PluginManager {
    private final Path pluginDir;
    private final BiPredicate<PluginDescription, PluginDescription> strategy;
    private final PluginLoader loader;

    private PluginManager(Path pluginDir, BiPredicate<PluginDescription, PluginDescription> strategy, Function<Class<?>, Plugin> constructor) {
        this.pluginDir = pluginDir;
        this.strategy = strategy;
        this.loader = new PluginLoader(pluginDir, this, strategy, constructor);
    }

    public static PluginManager of(Path pluginDir, BiPredicate<PluginDescription, PluginDescription> strategy, Function<Class<?>, Plugin> constructor) {
        return new PluginManager(pluginDir, strategy, constructor);
    }

    public static PluginManager of(Path pluginDir) {
        return of(pluginDir, ClassSharingStrategy.DEPEND, new SimpleConstructor());
    }

    public static PluginManager of(Path pluginDir, Function<Class<?>, Plugin> constructor) {
        return of(pluginDir, ClassSharingStrategy.DEPEND, constructor);
    }

    public static PluginManager of(Path pluginDir, BiPredicate<PluginDescription, PluginDescription> strategy) {
        return new PluginManager(pluginDir, strategy, new SimpleConstructor());
    }

    public static PluginManager of(Path pluginDir, ClassSharingStrategy strategy) {
        return new PluginManager(pluginDir, strategy, new SimpleConstructor());
    }

    public Plugin getPluginById(String id) {
        return loader.getPluginById(id);
    }

    public void enablePlugin(Plugin plugin) {
        loader.enablePlugin(plugin);
    }

    public void disablePlugin(Plugin plugin) {
        loader.disablePlugin(plugin);
    }

    public Collection<? extends Plugin> getPlugins() {
        return loader.getPlugins();
    }

    @SneakyThrows
    public void loadPlugins() {
        // resolve dependencies.
        @RequiredArgsConstructor
        class DescriptionAndFile {
            final PluginDescription description;
            final File file;
        }
        Map<String, DescriptionAndFile> descs = new HashMap<>();

        Files.walk(pluginDir, 0, FileVisitOption.FOLLOW_LINKS).forEach(file -> {
            PluginDescription pd = loader.resolveDescription(file.toFile());
            if (pd != null) {
                descs.put(pd.getId(), new DescriptionAndFile(pd, file.toFile()));
            }
        });

        //check graph.
        for (Map.Entry<String, DescriptionAndFile> stringPluginDescriptionEntry : descs.entrySet()) {
            for (String dependency : stringPluginDescriptionEntry.getValue().description.getDependencies()) {
                if (!descs.containsKey(dependency))
                    throw new InvalidDependency("Can't find dependency " + dependency + " for " + stringPluginDescriptionEntry.getKey());
            }
        }
        for (Map.Entry<String, DescriptionAndFile> stringPluginDescriptionEntry : descs.entrySet()) {
            for (String dependency : stringPluginDescriptionEntry.getValue().description.getConflicts()) {
                if (descs.containsKey(dependency))
                    throw new InvalidDependency(dependency + " is conflict with " + stringPluginDescriptionEntry.getValue());
            }
        }

        //try to load.

        @RequiredArgsConstructor
        class DependencyResolver {
            private final Map<String, DescriptionAndFile> descs;
            private final Set<String> failedPlugins = new HashSet<>();

            void resolve(String id) {
                resolve(id, new Stack<>());
            }

            void resolve(String id, Stack<String> resolvingStacks) {
                DescriptionAndFile description = descs.get(id);

                //check
                if (loader.isPluginLoaded(id)) {
                    return; // already resolved.
                }
                if (failedPlugins.contains(id)) {
                    throw new InvalidDependency(id + " was unable to resolve.");
                }
                if (resolvingStacks.contains(id)) {
                    throw new IllegalDependLoopException("Dependency loop detected: " + resolvingStacks);
                }
                resolvingStacks.add(id);
                // load dependency
                for (String dependency : description.description.getDependencies()) {
                    try {
                        resolve(dependency, resolvingStacks);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        resolvingStacks.pop();
                        return;
                    }
                }

                // after dependencies are resolved.
                // load itself.
                try {
                    loader.loadPlugin(description.file);
                } catch (IOException e) {
                    e.printStackTrace();
                    failedPlugins.add(id);
                }
                resolvingStacks.pop();
            }
        }
        DependencyResolver resolver = new DependencyResolver(descs);
        descs.keySet().forEach(resolver::resolve); // load plugins.
    }
}
