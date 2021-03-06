package io.ib67.extme;


import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ib67.extme.annotation.PluginTarget;
import io.ib67.extme.plugin.Plugin;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes("io.ib67.extme.annotation.*")
public class CodeGen extends AbstractProcessor {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(PluginTarget.class)) {
            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                Map<String, Object> result = new HashMap<>();
                annotationMirror.getElementValues().forEach((k, v) -> {
                    result.put(k.getSimpleName().toString(), process(v.getValue()));
                });
                result.put("main", ((TypeElement) element).getQualifiedName().toString());
                result.put("_comment", "Generated by ExtMe AP");
                // validation
                String ver = (String) result.get("version");
                try {
                    Version.valueOf(ver);
                } catch (ParseException exception) {
                    throw new IllegalArgumentException("Invalid semantic version number: " + ver, exception);
                }
                String id = (String) result.get("id");
                if (id.trim().isEmpty()) {
                    throw new IllegalArgumentException("ID cant be blank.");
                }
                if (id.trim().contains(" ")) {
                    throw new IllegalArgumentException("ID cannot contain spaces.");
                }
                // must be a Plugin
                TypeElement clazz = ((TypeElement) element);
                if (!clazz.getSuperclass().toString().equals(Plugin.class.getCanonicalName())) {
                    throw new IllegalStateException("Plugin must be subtype of io.ib67.extme.plugin.Plugin");
                }
                try {
                    FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.json");
                    try (Writer wr = fo.openWriter()) {
                        wr.write(GSON.toJson(result));
                        wr.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    private static Object process(Object fuck){
        //((List<Attribut>)fuck)
        if(fuck instanceof List){
            List<AnnotationValue> list = (List<AnnotationValue>) fuck;
            List<Object> values = new ArrayList<>();
            for (AnnotationValue a : list) {
                values.add(a.getValue());
            }

            return values;
        }
        return fuck.toString();
    }
}
