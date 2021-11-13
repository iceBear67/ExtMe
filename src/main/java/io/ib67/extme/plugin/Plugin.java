package io.ib67.extme.plugin;

public abstract class Plugin {
    protected void onLoad(){}

    protected void onEnable(){}

    protected void onDisable(){}

    PluginClassLoader getClassLoader(){
        return (PluginClassLoader) this.getClassLoader();
    }

}
