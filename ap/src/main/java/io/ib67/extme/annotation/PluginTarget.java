package io.ib67.extme.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface PluginTarget {
    String name();
    String id();
    String description() default "No description provided";
    String version();
    String[] depends() default {};
    String[] conflicts() default {};
}
