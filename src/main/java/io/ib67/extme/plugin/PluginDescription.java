package io.ib67.extme.plugin;

import com.github.zafarkhaja.semver.Version;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PluginDescription {
    private final String id;
    private final String name;
    private final String main;
    @Builder.Default
    private final String description="No descriptions.";
    private final Version version;
    @Builder.Default
    private final List<String> dependencies = new ArrayList<>();
    @Builder.Default
    private final List<String> conflicts = new ArrayList<>();

    @Override
    public String toString() {
        return name+"("+id+") ver "+version.toString();
    }
    boolean validate(){
        return id != null && name != null && main != null && version != null;
    }
}
