package io.github.kurok1.nacos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author <a href="mailto:khanc.dev@gmail.com">韩超</a>
 * @since 1.0
 */
public final class JsonSupport {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void output(Object object, OutputStream out) {
        try {
            objectMapper.writeValue(out, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(InputStream in, Class<T> clazz) {
        if (in == null)
            return new ArrayList<>();
        try {
            return objectMapper.readValue(in, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read json value for special path<br/>
     * for json example:
     * <pre>
     *     {
     *         "name": "nameValue",
     *         "data": {
     *             "key": "value"
     *         }
     *     }
     * </pre>
     * for paths:
     * <ul>
     *     <li>if paths is {"name"}, return "nameValue"</li>
     *     <li>if paths is {"data", "key"}, return "value"</li>
     * </ul>
     *
     * @param root json root
     * @param paths paths
     * @return value
     */
    public static String readStringJsonValue(JsonNode root, String... paths) {
        if (paths == null || paths.length == 0)
            return null;

        if (root == null)
            return "";

        int level = 0;
        JsonNode node = root;
        while (level < paths.length) {
            String path = paths[level];
            node = node.get(path);
            if (node == null)
                return "";
            level++;
        }

        return node.asText("");
    }

}
