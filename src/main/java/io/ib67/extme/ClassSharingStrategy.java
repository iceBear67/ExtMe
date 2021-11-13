package io.ib67.extme;

import io.ib67.extme.plugin.PluginDescription;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public enum ClassSharingStrategy implements BiPredicate<PluginDescription, PluginDescription> {
    ALL(
            (original, target) -> true
    ),
    DEPEND(
            (original, target) -> original.getDependencies().contains(target.getId())
    ),
    WARN(
            (original, target) -> {
                if (original.getDependencies().contains(target.getId())) {
                    return true;
                }
                Logger.getLogger("ExtMe").warning(original+" attempts to use classes from "+target+" but it's NOT declared in it's description file.");
                return true;
            }
    ),
    DENY(
            (o,t) -> false
    );
    private final BiPredicate<PluginDescription, PluginDescription> bp;

    ClassSharingStrategy(BiPredicate<PluginDescription, PluginDescription> bp) {
        this.bp = bp;
    }

    @Override
    public boolean test(PluginDescription description, PluginDescription description2) {
        return bp.test(description, description2);
    }
}
