package settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PluginSettingsConfigurable implements Configurable {

    private JTextField serverUrlField;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "CodeCom Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel mainPanel = new JPanel();
        serverUrlField = new JTextField(20);
        mainPanel.add(new JLabel("Server URL:"));
        mainPanel.add(serverUrlField);
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        PluginSettings settings = PluginSettings.getInstance();
        return !serverUrlField.getText().equals(settings.serverUrl);
    }

    @Override
    public void apply() {
        PluginSettings settings = PluginSettings.getInstance();
        settings.serverUrl = serverUrlField.getText();
    }

    @Override
    public void reset() {
        PluginSettings settings = PluginSettings.getInstance();
        serverUrlField.setText(settings.serverUrl);
    }
}
