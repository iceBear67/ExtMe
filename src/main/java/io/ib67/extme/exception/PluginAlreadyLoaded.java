package io.ib67.extme.exception;

public class PluginAlreadyLoaded extends IllegalStateException{
    public PluginAlreadyLoaded(String s) {
        super(s);
    }
}
