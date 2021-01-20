package com.benjaminfaal.ps4remotepkginstaller.service.impl;

import com.benjaminfaal.ps4remotepkginstaller.Settings;
import com.benjaminfaal.ps4remotepkginstaller.event.TaskUpdateEvent;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallManifestJSONUrlRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallPKGUrlRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallPackagesRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.TaskRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.request.TitleRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.ExistsResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.InstallResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.ModifyTaskResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.TaskProgress;
import com.benjaminfaal.ps4remotepkginstaller.service.RemotePKGInstallerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommonsLog
@Service
public class RemotePKGInstallerServiceImpl implements RemotePKGInstallerService, InitializingBean {

    public static final int PORT = 12800;

    @Autowired(required = false)
    private ServerProperties serverProperties;

    @Autowired
    private Settings settings;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private final ObjectMapper tasksObjectMapper = new ObjectMapper()
            .activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(InstallRequest.class).build());

    private final Map<Integer, InstallRequest> installRequests = new HashMap<>();

    private Map<Integer, TaskProgress> tasks = new HashMap<>();

    // API sometimes corrupts when doing multiple requests async https://github.com/flatz/ps4_remote_pkg_installer/issues/3
    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(1000))
            .setReadTimeout(Duration.ofMillis(1000))
            .errorHandler(new DefaultResponseErrorHandler() {
                @Override
                protected boolean hasError(HttpStatus statusCode) {
                    // API returns 500 but also a response which we need to parse
                    if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return false;
                    }
                    return super.hasError(statusCode);
                }
            })
            .interceptors((request, body, execution) -> {
                ClientHttpResponse response = execution.execute(request, body);
                // API returns hexadecimal values (like error codes) without qoutes which is invalid JSON
                final String json = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())
                        .replaceAll("0[xX][0-9a-fA-F]+", "\"$0\"");
                return new AbstractClientHttpResponse() {
                    @Override
                    public int getRawStatusCode() throws IOException {
                        return response.getRawStatusCode();
                    }

                    @Override
                    public String getStatusText() throws IOException {
                        return response.getStatusText();
                    }

                    @Override
                    public void close() {
                        response.close();
                    }

                    @Override
                    public InputStream getBody() throws IOException {
                        return new ByteArrayInputStream(json.getBytes());
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return response.getHeaders();
                    }
                };
            })
            .build();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (settings.containsKey("installRequests")) {
            try {
                TypeReference<HashMap<Integer, InstallRequest>> tasksTypeReference = new TypeReference<HashMap<Integer, InstallRequest>>() {};
                HashMap<Integer, InstallRequest> previousInstallRequests = tasksObjectMapper.readValue(settings.getProperty("installRequests"), tasksTypeReference);
                installRequests.putAll(previousInstallRequests);
            } catch (JsonProcessingException e) {
                log.error("Error loading previous install requests", e);
            }
        }
    }

    @Override
    public void setHost(String host) {
        String baseUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(PORT)
                .path("/api")
                .toUriString();
        restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(baseUri));
    }

    @Override
    public ExistsResponse exists(String titleId) {
        return restTemplate.postForObject("/is_exists", new TitleRequest(titleId), ExistsResponse.class);
    }

    @Override
    public InstallResponse installFiles(File[] files) {
        List<String> packages = new ArrayList<>();
        for (File file : files) {
            String fileBase64 = Base64.getUrlEncoder().encodeToString(file.getAbsolutePath().getBytes());

            Map<String, Object> variables = new HashMap<>();
            variables.put("file", fileBase64);
            String pkgUrl = buildUri()
                    .path("pkg/{file}")
                    .uriVariables(variables)
                    .toUriString();
            packages.add(pkgUrl);
        }

        InstallPackagesRequest request = new InstallPackagesRequest();
        request.setPackages(packages.toArray(new String[0]));
        request.setLocalFiles(Arrays.stream(files).map(File::getAbsolutePath).toArray(String[]::new));
        return install(request);
    }

    @Override
    public InstallResponse installManifestJSONUrl(String manifestJsonUrl) {
        InstallManifestJSONUrlRequest request = new InstallManifestJSONUrlRequest();
        request.setUrl(manifestJsonUrl);
        return install(request);
    }

    @Override
    public InstallResponse installPKGUrl(String pkgUrl) {
        InstallPKGUrlRequest request = new InstallPKGUrlRequest();
        request.setPackages(new String[]{pkgUrl});
        return install(request);
    }

    @Override
    public ModifyTaskResponse stopTask(Integer id) {
        ModifyTaskResponse stopTaskResponse = modifyTask(id, "stop");
        applicationEventPublisher.publishEvent(new TaskUpdateEvent(id));
        return stopTaskResponse;
    }

    @Override
    public ModifyTaskResponse pauseTask(Integer id) {
        ModifyTaskResponse pauseTaskResponse = modifyTask(id, "pause");
        applicationEventPublisher.publishEvent(new TaskUpdateEvent(id));
        return pauseTaskResponse;
    }

    @Override
    public ModifyTaskResponse resumeTask(Integer id) {
        ModifyTaskResponse resumeTaskResponse = modifyTask(id, "resume");
        applicationEventPublisher.publishEvent(new TaskUpdateEvent(id));
        return resumeTaskResponse;
    }

    @Override
    public ModifyTaskResponse removeTask(Integer taskId) {
        installRequests.remove(taskId);
        saveTasks();
        return modifyTask(taskId, "unregister");
    }

    @Override
    public List<TaskProgress> getTasks() {
        return installRequests.keySet().stream()
                .sorted()
                .map(this::getTask)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getTaskIds() {
        return installRequests.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public InstallRequest getInstallRequest(Integer taskId) {
        return installRequests.get(taskId);
    }

    @Override
    public TaskProgress getTask(Integer id) {
        TaskProgress response = restTemplate.postForObject("/get_task_progress", new TaskRequest(id), TaskProgress.class);
        response.setId(id);
        tasks.put(id, response);
        return response;
    }

    @Override
    public TaskProgress getCachedTask(Integer id) {
        return tasks.get(id);
    }

    @Override
    public int countTasks() {
        return installRequests.size();
    }

    private ModifyTaskResponse modifyTask(Integer id, String action) {
        return restTemplate.postForObject("/" + action + "_task", new TaskRequest(id), ModifyTaskResponse.class);
    }

    private InstallResponse install(InstallRequest request) {
        InstallResponse response = restTemplate.postForObject("/install", request, InstallResponse.class);
        if (response.isSuccess() && response.getTaskId() != null) {
            installRequests.put(response.getTaskId(), request);
            saveTasks();
            applicationEventPublisher.publishEvent(new TaskUpdateEvent(response.getTaskId()));
        }
        return response;
    }

    private void saveTasks() {
        try {
            settings.setProperty("installRequests", tasksObjectMapper.writeValueAsString(installRequests));
        } catch (JsonProcessingException e) {
            log.error("Error saving install requests: ", e);
        }
    }

    private UriComponentsBuilder buildUri() {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(serverProperties.getAddress().getHostAddress())
                .port(serverProperties.getPort());
    }

}