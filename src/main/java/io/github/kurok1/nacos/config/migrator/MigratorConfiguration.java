package io.github.kurok1.nacos.config.migrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:khanc.dev@gmail.com">韩超</a>
 * @since 1.0
 */
public class MigratorConfiguration {

    private String sourceHost;
    private String targetHost;
    private boolean sourceSecure = false;
    private String sourceUserName;
    private String sourcePassword;
    private boolean targetSecure = false;
    private String targetUserName;
    private String targetPassword;

    private List<String> namespaces = new ArrayList<>();

    public boolean createNamespaceIfNotExists = true;

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public boolean isSourceSecure() {
        return sourceSecure;
    }

    public void setSourceSecure(boolean sourceSecure) {
        this.sourceSecure = sourceSecure;
    }

    public boolean isTargetSecure() {
        return targetSecure;
    }

    public void setTargetSecure(boolean targetSecure) {
        this.targetSecure = targetSecure;
    }

    public String getSourceUserName() {
        return sourceUserName;
    }

    public void setSourceUserName(String sourceUserName) {
        this.sourceUserName = sourceUserName;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getTargetPassword() {
        return targetPassword;
    }

    public void setTargetPassword(String targetPassword) {
        this.targetPassword = targetPassword;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }

    public boolean isCreateNamespaceIfNotExists() {
        return createNamespaceIfNotExists;
    }

    public void setCreateNamespaceIfNotExists(boolean createNamespaceIfNotExists) {
        this.createNamespaceIfNotExists = createNamespaceIfNotExists;
    }

    public static MigratorConfiguration readFromProperties(Properties properties) {
        MigratorConfiguration config = new MigratorConfiguration();
        config.setSourceHost(properties.getProperty("source.host"));
        config.setTargetHost(properties.getProperty("target.host"));

        config.setSourceSecure(Boolean.parseBoolean(properties.getProperty("source.secure", "false")));
        config.setTargetSecure(Boolean.parseBoolean(properties.getProperty("target.secure", "false")));

        config.setSourceUserName(properties.getProperty("source.username"));
        config.setSourcePassword(properties.getProperty("source.password"));
        config.setTargetUserName(properties.getProperty("target.username"));
        config.setTargetPassword(properties.getProperty("target.password"));

        String namespaces = properties.getProperty("namespaces");
        if (namespaces != null) {
            for (String namespace : namespaces.split(",")) {
                config.getNamespaces().add(namespace);
            }
        }
        config.setCreateNamespaceIfNotExists(Boolean.parseBoolean(properties.getProperty("create.namespace.if.not.exists", "true")));
        return config;
    }
}
