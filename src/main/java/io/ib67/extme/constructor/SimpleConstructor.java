package io.ib67.extme.constructor;

import io.ib67.extme.plugin.Plugin;
import lombok.SneakyThrows;

import java.util.function.Function;

public class SimpleConstructor implements Function<Class<?>,Plugin> {
    @SneakyThrows
    @Override
    public Plugin apply(Class<?> aClass) {
        return (Plugin) aClass.getDeclaredConstructor().newInstance();
    }
}
