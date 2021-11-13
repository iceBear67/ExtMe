package io.ib67.extme.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final JarFile jar;
    private final PluginLoader loader;
    @Getter
    private final PluginDescription description;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Plugin plugin;
    private final Manifest manifest;
    private final URL url;

    @SneakyThrows
    public PluginClassLoader(File plugin, PluginLoader loader, PluginDescription description) {
        super(new URL[]{ ((Supplier<URL>)(() -> {
            try {
                return plugin.toURI().toURL();
            } catch (MalformedURLException impossible) {
            }
            throw new IllegalStateException("Impossible null");
        })).get()});
        url = plugin.toURI().toURL();
        this.jar = new JarFile(plugin);
        this.manifest = jar.getManifest();
        this.description = description;
        this.loader = loader;

    }

    @Override
    protected Class<?> findClass(String s) throws ClassNotFoundException {
        return findClass(s, true);
    }

    Class<?> findClass(String clazz, boolean searchGlobal) throws ClassNotFoundException {
        Class<?> target = classes.get(clazz);
        if (target == null) {
            String path = clazz.replace('.', '/').concat(".class");
            JarEntry entry = jar.getJarEntry(path);

            if (entry != null) {
                byte[] classBytes;

                try (InputStream is = jar.getInputStream(entry);
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[4096];
                    int i;
                    while (( i = is.read(buf)) != -1){
                        outputStream.write(buf);
                    }
                    classBytes = outputStream.toByteArray();
                } catch (IOException ex) {
                    throw new ClassNotFoundException(clazz, ex);
                }
                int dot = clazz.lastIndexOf('.');
                if (dot != -1) {
                    String pkgName = clazz.substring(0, dot);
                    if (getPackage(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getPackage(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }

                CodeSigner[] signers = entry.getCodeSigners();
                CodeSource source = new CodeSource(url, signers);
                target = defineClass(clazz, classBytes, 0, classBytes.length, source);
                if (target != null) {
                    classes.put(clazz, target);
                    return target;
                }
            }

            if (target == null) {
                try {
                    target = super.findClass(clazz);
                } catch (ClassNotFoundException ignored) {

                }
            }
            if (searchGlobal) {
                target = loader.findClass(clazz, this);
                return target;
            }
        }
        if (target != null) {
            return target;
        }
        throw new ClassNotFoundException("Couldn't find class " + clazz + " for plugin: " + description);
    }
}
