package io.github.kurok1.nacos;

import com.fasterxml.jackson.databind.JavaType;

import java.util.ArrayList;

/**
 * @author <a href="mailto:khanc.dev@gmail.com">韩超</a>
 * @since 1.0
 */
public class Namespace {

    public static JavaType TYPE = JsonSupport.objectMapper.constructType(Namespace.class);
    public static JavaType LIST_TYPE = JsonSupport.objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Namespace.class);

    private int configCount;
    private String namespace;
    private String namespaceDesc;
    private String namespaceShowName;
    private int quota;
    private int type;

    public int getConfigCount() {
        return configCount;
    }

    public void setConfigCount(int configCount) {
        this.configCount = configCount;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceDesc() {
        return namespaceDesc;
    }

    public void setNamespaceDesc(String namespaceDesc) {
        this.namespaceDesc = namespaceDesc;
    }

    public String getNamespaceShowName() {
        return namespaceShowName;
    }

    public void setNamespaceShowName(String namespaceShowName) {
        this.namespaceShowName = namespaceShowName;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
