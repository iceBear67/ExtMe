package io.ib67.extme.plugin;

import com.google.gson.Gson;
import io.ib67.extme.PluginManager;
import io.ib67.extme.exception.IllegalClassSharingException;
import io.ib67.extme.exception.InvalidDependency;
import io.ib67.extme.exception.InvalidPluginException;
import io.ib67.extme.exception.PluginAlreadyLoaded;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PluginLoader {
    private static final Gson GSON = new Gson();


    private final Path pluginsPath;
    private final PluginManager pm;
    private final Map<String, PluginHolder> plugins = new ConcurrentHashMap<>();
    private final BiPredicate<PluginDescription, PluginDescription> depPredicate;
    private final Function<Class<?>, Plugin> pluginConstructor;

    public Collection<? extends Plugin> getPlugins() {
        return plugins.values().stream().map(PluginHolder::getPlugin).collect(Collectors.toList());
    }

    public Plugin getPluginById(String id) {
        if (!plugins.containsKey(id)) { // containsKey was optimized by concurrentMap.
            return null;
        }
        return plugins.get(id).getPlugin();
    }

    Class<?> findClass(String clazzName, PluginClassLoader unexcepted) throws ClassNotFoundException {

        for (PluginHolder value : plugins.values()) {
            if (value.getClassLoader() == unexcepted) {
                continue;
            }
            try {
                Class<?> clazz = value.getClassLoader().findClass(clazzName, false);
                if (depPredicate.test(value.getClassLoader().getDescription(), unexcepted.getDescription())) {
                    return clazz;
                }
                throw new IllegalClassSharingException(unexcepted.getDescription() + " attempts to use class " + clazzName + " from " + value.getDescription());
            } catch (ClassNotFoundException ignored) {
                // just skip it
            }
        }
        throw new ClassNotFoundException("Can't find " + clazzName);
    }

    public void enablePlugin(Plugin plugin) {
        plugin.onEnable();
    }

    public void disablePlugin(Plugin plugin) {
        plugin.onDisable();
    }

    public boolean isPluginLoaded(String id) {
        return plugins.containsKey(id);
    }

    @SneakyThrows
    public PluginDescription resolveDescription(File file) {
        JarFile jf = new JarFile(file,true);
        JarEntry pluginDescription = jf.getJarEntry("plugin.json");
        try (InputStream is = jf.getInputStream(pluginDescription);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int i;
            while ((i = is.read(buf)) != -1) {
                out.write(buf);
            }
            PluginDescription description = GSON.fromJson(out.toString(), PluginDescription.class);
            return description;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Plugin loadPlugin(File file) throws InvalidPluginException, IOException {
        PluginDescription description = resolveDescription(file);
        if (description==null || !description.validate()) {
            throw new InvalidPluginException("Invalid plugin.json");
        }
        if (isPluginLoaded(description.getId())) {
            throw new PluginAlreadyLoaded(description.toString());
        }
        for (String dependency : description.getDependencies()) {
            if (!isPluginLoaded(dependency))
                throw new InvalidDependency("Can't find dependency " + dependency + " for " + description);
        }
        for (String dependency : description.getConflicts()) {
            if (isPluginLoaded(dependency))
                throw new InvalidPluginException(dependency + " is conflict with " + description);
        }

        PluginClassLoader pl = new PluginClassLoader(file, this, description);
        try {
            Class<?> pluginClazz = pl.findClass(description.getMain(), false);
            plugins.put(description.getId(),new PluginHolder(pl,pluginConstructor.apply(pluginClazz),description));
            return getPluginById(description.getId());
        } catch (ClassNotFoundException e) {
            throw new InvalidPluginException("Can't find main class " + description.getMain() + " for plugin " + description, e);
        }
    }
}
