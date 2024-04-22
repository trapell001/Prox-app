package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.ConfigValue;
import com.prox.challenge.gcoder.model.Cover;
import com.prox.challenge.gcoder.model.MonitorRequest;
import com.prox.challenge.gcoder.repository.UserRepository;
import com.prox.challenge.gcoder.security.GCoderSecurity;
import com.prox.challenge.gcoder.upload.GCoderSocket;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class MonitorService {
    public static String PATH_WRITE_LOG_MONITOR;
    private static GCoderSecurity TOKEN_FILTER;
    private static UserRepository USER_REPOSITORY;
    private static MonitorRequest monitorRequestCurrent;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm").withZone(ZoneId.of("GMT+7"));
    private static final Set<WebSocketSession> sessionList = new HashSet<>();
    private static final GCoderSocket.Socket monitorSocket = GCoderSocket.createSocket("/monitor/view");
    private final List<FollowList> listFollow = new ArrayList<>();
    private final List<FollowMap> mapFollow = new ArrayList<>();
    private static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
    @Autowired
    private ConfigService configService;
    @Autowired
    private GCoderSecurity tokenFilter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscordService discordService;

    @PostConstruct
    private void init() {
        TOKEN_FILTER = tokenFilter;
        USER_REPOSITORY = userRepository;
        configService.addConfigActions("path monitor log file", "/home/server/log/", "file log of monitor will here", ConfigValue.Type.monitor, s -> PATH_WRITE_LOG_MONITOR = s);
        scheduled.scheduleWithFixedDelay(this::checkListToSendErrorByDiscord, 5, 5 , TimeUnit.MINUTES);
    }

    static {
        monitorSocket.onConnect = session -> {
            String token = GCoderSocket.getParam(session, "token").orElseThrow(() -> new RuntimeException("Token is null"));
            String email = TOKEN_FILTER.parseJwt(token);
            USER_REPOSITORY.findByEmail(email).orElseThrow(() -> new RuntimeException("Email not found"));
            sessionList.add(session);
        };
        monitorSocket.close = (session, status) -> sessionList.remove(session);
        monitorSocket.error = (session, exception) -> {
            TextMessage message = new TextMessage(ConfigService.GSON.toJson(new Cover(500, exception.getMessage())));
            session.sendMessage(message);
            session.close(CloseStatus.SERVER_ERROR);
        };
    }

    public static void plusSuccess(long time) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String key = now.format(formatter);
        if (monitorRequestCurrent == null) monitorRequestCurrent = new MonitorRequest(key);
        if (monitorRequestCurrent.getTime().equals(key)) {
            monitorRequestCurrent.plusSuccess(time);
        } else {
            sendMessage();
        }
    }
    public static synchronized void plusError(){
        if(monitorRequestCurrent != null) monitorRequestCurrent.plusError();
    }
    public static synchronized void plusCount(){
        if(monitorRequestCurrent != null) monitorRequestCurrent.plusCount();
    }

    private static void sendMessage() throws IOException {
        TextMessage message = new TextMessage(ConfigService.GSON.toJson(new Cover(200, monitorRequestCurrent)));
        sessionList.forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writeFileLog();
        monitorRequestCurrent = null;
    }



    private static synchronized void writeFileLog() throws IOException {
        String path = PATH_WRITE_LOG_MONITOR + LocalDate.now() + ".json";
        Path p = Paths.get(PATH_WRITE_LOG_MONITOR);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
        }
        p = Paths.get(path);
        List<MonitorRequest> monitorRequests = new LinkedList<>();
        if (Files.exists(p)) {
            try {
                String content = FileService.readFile(path);
                MonitorRequest[] all = ConfigService.GSON.fromJson(content, MonitorRequest[].class);
                for(var a : all){
                    if(a != null) monitorRequests.add(a);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        monitorRequests.add(monitorRequestCurrent);
        FileService.writeFile(path, ConfigService.GSON.toJson(monitorRequests));
    }
    public void addListMapToCheck(Collection<?> list, String name, int limit){
        this.listFollow.add(new FollowList(list, name, limit));
    }
    public void addListMapToCheck(Map<?,?> map, String name, int limit){
        this.mapFollow.add(new FollowMap(map, name, limit));
    }
    private void checkListToSendErrorByDiscord(){
        StringBuilder sb = new StringBuilder();
        for(var listFollow : this.listFollow){
            if(listFollow.list.size() > listFollow.limitToSendError){
                sb.append("[").append(listFollow.name).append("]").append(" length too long :").append(listFollow.list.size()).append("\\n");
            }
        }
        for(var mapFollow : this.mapFollow){
            if(mapFollow.map.size() > mapFollow.limitToSendError){
                sb.append("[").append(mapFollow.name).append("]").append(" length too long :").append(mapFollow.map.size()).append("\\n");
            }
        }
        discordService.sendDiscord(sb.toString());
    }
    record FollowList(Collection<?> list, String name, int limitToSendError){}
    record FollowMap(Map<?,?> map, String name, int limitToSendError){}

}
