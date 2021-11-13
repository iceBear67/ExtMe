package io.ib67.extme.constructor;

import com.google.inject.Injector;
import io.ib67.extme.plugin.Plugin;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class GuiceConstructor implements Function<Class<?>, Plugin> {
    private final Injector injector;

    @Override
    public Plugin apply(Class<?> aClass) {
        return (Plugin) injector.getInstance(aClass);
    }
}
