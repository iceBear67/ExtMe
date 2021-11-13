package io.ib67.extme;

import io.ib67.extme.constructor.SimpleConstructor;
import io.ib67.extme.plugin.Plugin;
import io.ib67.extme.plugin.PluginDescription;
import io.ib67.extme.plugin.PluginLoader;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class PluginManager {
    private final Path pluginDir;
    private final BiPredicate<PluginDescription,PluginDescription> strategy;
    private final PluginLoader loader;
    private PluginManager(Path pluginDir, BiPredicate<PluginDescription,PluginDescription> strategy, Function<Class<?>, Plugin> constructor){
        this.pluginDir=pluginDir;
        this.strategy=strategy;
        this.loader = new PluginLoader(pluginDir,this,strategy,constructor);
    }

    public static PluginManager of(Path pluginDir,BiPredicate<PluginDescription,PluginDescription> strategy,Function<Class<?>,Plugin> constructor){
        return new PluginManager(pluginDir, strategy, constructor);
    }
    public static PluginManager of(Path pluginDir){
        return of(pluginDir,ClassSharingStrategy.DEPEND,new SimpleConstructor());
    }
    public static PluginManager of(Path pluginDir,Function<Class<?>,Plugin> constructor){
        return of(pluginDir,ClassSharingStrategy.DEPEND,constructor);
    }
    public static PluginManager of(Path pluginDir,BiPredicate<PluginDescription,PluginDescription> strategy){
        return new PluginManager(pluginDir,strategy,new SimpleConstructor());
    }
    public static PluginManager of(Path pluginDir,ClassSharingStrategy strategy){
        return new PluginManager(pluginDir,strategy,new SimpleConstructor());
    }

    public Plugin getPluginById(String id){
        return loader.getPluginById(id);
    }

    public void enablePlugin(Plugin plugin){
        loader.enablePlugin(plugin);
    }

    public void disablePlugin(Plugin plugin){
        loader.disablePlugin(plugin);
    }

    public Collection<? extends Plugin> getPlugins(){
        return loader.getPlugins();
    }

    @SneakyThrows
    public void loadPlugins(){
        Files.walk(pluginDir,0, FileVisitOption.FOLLOW_LINKS).forEach(file -> {
            try {
                loader.loadPlugin(file.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
