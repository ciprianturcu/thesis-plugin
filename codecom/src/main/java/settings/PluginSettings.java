package settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

@State(name = "PluginSettings", storages = @Storage("MyPluginSettings.xml"))
@Service(Service.Level.APP)
public final class PluginSettings implements PersistentStateComponent<PluginSettings> {

    public String serverUrl = "http://127.0.0.1:8080";

    public static PluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettings.class);
    }

    @Override
    public @NotNull PluginSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginSettings state) {
        this.serverUrl = state.serverUrl;
    }
}
