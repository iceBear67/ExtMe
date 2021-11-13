package io.ib67.extme.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PluginHolder {
    private final PluginClassLoader classLoader;
    private final Plugin plugin;
    private final PluginDescription description;
}
