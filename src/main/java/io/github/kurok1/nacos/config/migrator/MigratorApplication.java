package io.github.kurok1.nacos.config.migrator;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.kurok1.RequestTemplate;
import io.github.kurok1.nacos.JsonSupport;
import io.github.kurok1.nacos.Namespace;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author <a href="mailto:khanc.dev@gmail.com">韩超</a>
 * @since 1.0
 */
public class MigratorApplication implements Runnable {

    private final MigratorConfiguration migratorConfiguration;
    private final RequestTemplate sourceRequestTemplate;
    private final RequestTemplate targetRequestTemplate;

    public MigratorApplication(MigratorConfiguration migratorConfiguration) {
        this.migratorConfiguration = migratorConfiguration;
        this.sourceRequestTemplate = new RequestTemplate(migratorConfiguration.getSourceHost(), 8848, false);
        this.targetRequestTemplate = new RequestTemplate(migratorConfiguration.getTargetHost(), 443, true);
    }

    @Override
    public void run() {
        try {
            Path dir = Paths.get(System.getProperty("user.dir"), "nacos-output");
            List<Namespace> keys = sinkSource(dir);
            //登录到目标服务器
            String token = "";
            if (migratorConfiguration.isTargetSecure()) {
                String json = getAccessToken(targetRequestTemplate, migratorConfiguration.getTargetUserName(), migratorConfiguration.getTargetPassword());
                targetRequestTemplate.addHeader("Authorization", json);
                JsonNode jsonNode = JsonSupport.objectMapper.readTree(json);
                if (jsonNode.has("accessToken")) {
                    token = jsonNode.get("accessToken").asText();
                    targetRequestTemplate.addHeader("accessToken", token);
                } else {
                    throw new RuntimeException("获取目标服务器访问令牌失败");
                }
            }

            //获取所有namespace
            List<Namespace> namespaces = getNamesapceList(targetRequestTemplate);
            Set<String> namespaceSet = new HashSet<>();
            for (Namespace namespace : namespaces) {
                namespaceSet.add(namespace.getNamespace());
            }

            for (Namespace namespace : keys) {
                if (namespace.getConfigCount() == 0) {
                    System.out.println("namespace没有需要导入的配置数据: " + namespace.getNamespace());
                    continue;
                }

                if (!namespaceSet.contains(namespace.getNamespace())) {
                    //创建namespace
                    if (migratorConfiguration.isCreateNamespaceIfNotExists()) {
                        System.out.println("创建namespace: " + namespace.getNamespace());
                        createNamespace(targetRequestTemplate, namespace);
                    } else {
                        System.out.println("目标服务器不存在namespace: " + namespace);
                    }

                }
                System.out.println("导入配置文件, namespace: " + namespace.getNamespace());
                doImport(targetRequestTemplate, namespace, token, dir.resolve(namespace.getNamespace() + ".zip"));
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void doImport(RequestTemplate targetRequestTemplate, Namespace namespace, String token, Path resolve) throws IOException {
        final String path = "nacos/v1/cs/configs";
        Map<String, String> params = new HashMap<>();

        params.put("tenant", namespace.getNamespace());
        params.put("namespace", namespace.getNamespace());
        params.put("accessToken", token);
        params.put("import", "true");
        params.put("username", migratorConfiguration.getTargetUserName());
        targetRequestTemplate.postFormAndFile(path, params, resolve);
    }

    private void createNamespace(RequestTemplate requestTemplate, Namespace namespace) throws IOException {
        final String path = "nacos/v1/console/namespaces";
        Map<String, String> params = new HashMap<>();
        params.put("namespaceName", namespace.getNamespaceShowName());
        params.put("namespaceDesc", namespace.getNamespaceDesc());
        params.put("customNamespaceId", namespace.getNamespace());
        params.put("namespaceId", "");
        requestTemplate.postForm(path, params);
    }

    private List<Namespace> sinkSource(Path dir) throws Exception {
        //login to source
        String token = "";
        if (migratorConfiguration.isSourceSecure()) {
            String json = getAccessToken(sourceRequestTemplate, migratorConfiguration.getSourceUserName(), migratorConfiguration.getSourcePassword());
            sourceRequestTemplate.addHeader("Authorization", json);
            JsonNode jsonNode = JsonSupport.objectMapper.readTree(json);
            if (jsonNode.has("accessToken")) {
                token = jsonNode.get("accessToken").asText();
                sourceRequestTemplate.addHeader("accessToken", token);
            } else {
                throw new RuntimeException("获取源服务器访问令牌失败");
            }
        }


        //获取所有namespace
        List<Namespace> namespaces = getNamesapceList(sourceRequestTemplate);

        Map<String, InputStream> namespaceMap = new HashMap<>();

        for (Namespace namespace : namespaces) {
            //导出namespace配置数据
            if (namespace.getConfigCount() > 0) {
                System.out.println("导出namespace配置数据: " + namespace.getNamespace());
                InputStream inputStream = exportConfig(sourceRequestTemplate, migratorConfiguration.getSourceUserName(), token, namespace.getNamespace());
                namespaceMap.put(namespace.getNamespace(), inputStream);
            } else {
                System.out.println("namespace没有配置数据: " + namespace.getNamespace());
            }
        }

        //输出数据到指定文件夹
        if (Files.exists(dir)) {
            System.out.println("删除旧的输出目录: " + dir.toAbsolutePath());
            Files.list(dir).forEach(subPath -> {
                try {
                    Files.delete(subPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        Files.deleteIfExists(dir);
        Files.createDirectories(dir);

        for (Map.Entry<String, InputStream> entry : namespaceMap.entrySet()) {
            String namespace = entry.getKey();
            InputStream inputStream = entry.getValue();
            Path outputFile = dir.resolve(namespace + ".zip");
            Files.copy(inputStream, outputFile);
            System.out.println("导出配置数据到文件: " + outputFile.toAbsolutePath());
        }

        return namespaces;
    }

    private String getAccessToken(RequestTemplate requestTemplate, String username, String password) throws IOException {
        final String path = "nacos/v1/auth/users/login";
        return requestTemplate.postForm(path, Map.of("username", username, "password", password));
    }

    private List<Namespace> getNamesapceList(RequestTemplate requestTemplate) throws IOException {
        final String path = "nacos/v1/console/namespaces";
        Response response = requestTemplate.get(path, Map.of("namespaceId", ""));
        JsonNode jsonNode = JsonSupport.objectMapper.readTree(response.body().byteStream());
        response.close();
        return JsonSupport.objectMapper.convertValue(jsonNode.get("data"), Namespace.LIST_TYPE);
    }

    private InputStream exportConfig(RequestTemplate requestTemplate, String username, String accessToken, String namespace) throws IOException {
        final String path = "nacos/v1/cs/configs";
        Map<String, String> params = new HashMap<>();
        params.put("exportV2", "true");
        params.put("tenant", namespace);
        params.put("group", "");
        params.put("appName", "");
        params.put("dataId", "");
        params.put("ids", "");
        params.put("accessToken", accessToken);
        params.put("username", username);

        Response response = requestTemplate.get(path, params);
        return response.body().byteStream();
    }

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(MigratorApplication.class.getClassLoader().getResourceAsStream("migrator.properties"));
        MigratorConfiguration configuration = MigratorConfiguration.readFromProperties(properties);
        MigratorApplication migratorApplication = new MigratorApplication(configuration);
        migratorApplication.run();
    }



}
