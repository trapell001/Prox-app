package com.prox.challenge.gcoder.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;
import com.prox.challenge.gcoder.model.ConfigValue;
import com.prox.challenge.gcoder.repository.ConfigValueRepository;
import com.prox.challenge.gcoder.upload.UploadService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Transactional
@Log4j2
public class ConfigService {
    public final static Random RANDOM = new Random();
    public final static ScheduledExecutorService SCHEDULED = Executors.newScheduledThreadPool(50);
    /**
     * the first url will connect filename to become the complete url
     */
    public static String URL_FIRST_FILE;
    /**
     * request get file by url '<img src="http://localhost/public/file/">' don't need token to get file
     */
    public static String PATH_PUBLIC_FILE;
    /**
     * path will save all config file , endpoint upload file
     */
    public static String PATH_CONFIG_FILE;
    /**
     * custom config action
     */
    public final static Gson GSON = new Gson();
    private final List<RunnableException> configAction = new ArrayList<>();

    @Autowired
    private ConfigValueRepository configValueRepository;
    @Autowired
    private SecurityService securityService;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public List<ConfigValue> findAll(HttpServletRequest httpServletRequest) {
        securityService.checkAdmin(httpServletRequest);
        return configValueRepository.findAll();
    }

    @PostConstruct
    private void init() {
        try {
            runFirstConfig();
            configFirebase();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public List<ConfigValue> saveAll(HttpServletRequest httpServletRequest, List<ConfigValue> values) {
        securityService.checkAdmin(httpServletRequest);
        return configValueRepository.saveAll(values);
    }

    private void runFirstConfig() {
        // config url file
        addConfigActions("URL_FIRST_FILE", "https://data.nowtechai.com/assets/", "the first url will connect filename to become the complete url", ConfigValue.Type.upload, s ->
                URL_FIRST_FILE = s
        );
        // config path save when up file
        addConfigActions("PATH_UPLOAD", "/home/server/html/assets/", "When up load by socket 'ws:/localhost/upload, file will upload on path on server'", ConfigValue.Type.upload, s ->
                UploadService.upload.setFolderSave(s)
        );
        // config path source file
        addConfigActions("PATH_PUBLIC_FILE", "/home/server/html/assets/", "request get file by url http://localhost/public/file dont need token to get file", ConfigValue.Type.upload,
                s -> PATH_PUBLIC_FILE = s
        );
        // config path file config example [firebase config]
        addConfigActions("PATH_FILE_CONFIG", "/home/server/config/", "path will save all config file , endpoint upload file ", ConfigValue.Type.upload, s ->
                PATH_CONFIG_FILE = s
        );
    }

    public Set<String> applyConfig() {
        Set<String> error = new HashSet<>();
        for (var action : this.configAction) {
            try {
                action.run();
            } catch (Exception e) {
                error.add(e.getMessage());
            }
        }
        return error;
    }

    public Set<String> run(HttpServletRequest httpServletRequest) {
        securityService.checkAdmin(httpServletRequest);
        return applyConfig();
    }

    public void configFirebase(HttpServletRequest httpServletRequest, MultipartFile file) {
        securityService.checkAdmin(httpServletRequest);
        uploadFileConfig(file, "firebase_config.json");
        configFirebase();
    }

    /**
     * make config value for service, if any change, it will call back to start method
     *
     * @param key          : name of value
     * @param defaultValue : if key is not exist, this value will set first time
     * @param description  : to say about to key, cant update
     * @param callBack     : is current value of key, if any change, call will be start
     */
    public void addConfigActions(String key, String defaultValue, String description, ConfigValue.Type type, ConsumerString callBack) {
        RunnableException action = () -> {
            try {
                ConfigValue pathFileConfig = configValueRepository.findById(key).orElseGet(() -> {
                    ConfigValue d = new ConfigValue();
                    d.setKey(key);
                    d.setValue(defaultValue);
                    d.setDescription(description);
                    return configValueRepository.save(d);
                });
                // check old version set type yet
                if (pathFileConfig.getType() == null) {
                    pathFileConfig.setType(type);
                    configValueRepository.save(pathFileConfig);
                }
                callBack.accept(pathFileConfig.getValue());
            } catch (Exception e) {
                throw new RuntimeException(key + ": " + e.getMessage());
            }
        };
        try {
            action.run();
        } catch (Exception e) {
            log.error(e);
        }
        configAction.add(action);
    }

    public void configFirebase() {
        String path = PATH_CONFIG_FILE + "firebase_config.json";
        if (Files.exists(Paths.get(path))) {
            try {
                FileInputStream serviceAccount =
                        new FileInputStream(path);
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                throw new RuntimeException("Config firebase service Error");
            }
        } else {
            log.warn("[config]: firebase_config.json not found");
        }
    }

    public void configFirebaseClient(HttpServletRequest httpServletRequest, MultipartFile file) {
        securityService.checkAdmin(httpServletRequest);
        uploadFileConfig(file, "firebase_client.json");
    }

    /**
     * any file config, save in folder PATH_CONFIG_FILE
     */
    private void uploadFileConfig(MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload.");
        }
        try {
            Path uploadPath = Paths.get(PATH_CONFIG_FILE);
            if (!Files.exists(uploadPath)) {
                Files.createDirectory(uploadPath);
            }
            File uploadedFile = new File(PATH_CONFIG_FILE + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(uploadedFile);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file.");
        }
    }

    @FunctionalInterface
    public interface RunnableException {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ConsumerString {
        void accept(String value) throws Exception;
    }
}
