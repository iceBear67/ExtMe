# ExtMe
Extremely simple Java plugin framework.

```java
package io.ib67.extme;

import io.ib67.extme.annotation.PluginTarget;

import java.nio.file.Paths;

@PluginTarget(
        name = "showcasee eeeeeee",
        id = "showcase",
        description = "wow",
        version = "0.0.1-semverRequired",
        depends = {"a", "B"},
        conflicts = {"c"}
)
public class Show {
    {
        PluginManager pm = PluginManager.of(Paths.get("plugins"), ClassSharingStrategy.DEPEND);
        pm.loadPlugins();
        pm.getPluginById("sbnc");
    }
}
```
# Getting Started
```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
dependencies {
    annotationProcessor 'com.github.iceBear67:ExtMe'
}
```