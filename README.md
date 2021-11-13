# ExtMe
Extremely simple and lightweight(~26KiB) plugin framework.

```java
package io.ib67.extme;

import io.ib67.extme.plugin.Plugin;
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
public class Show extends Plugin {
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
    implementation ('com.github.iceBear67:ExtMe:Tag'){
        exclude 'com.google.code.gson:gson:2.8.9' // if you want
    }
}
```

Addition for plugin(@PluginTarget, which is a compiler plugin that validates & generates configuration):

```groovy
dependencies {
    implementation('com.github.iceBear67:ExtMe:Tag') {
        exclude 'com.google.code.gson:gson:2.8.9' // if you want
    }
    annotationProcessor 'com.github.iceBear67:ExtMe:Tag'
}
```

# Future Plans

- [ ] Dependency Versioning
- [x] Soft Dependencies
- [ ] `Provides` property