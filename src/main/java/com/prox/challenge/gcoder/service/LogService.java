package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.RequestCounter;
import com.prox.challenge.gcoder.model.UserHistory;
import com.prox.challenge.gcoder.repository.UserHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@Log4j2
public class LogService {
    @Autowired
    private UserHistoryRepository userHistoryRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DFileService dFileService;

    //>> History

    /**
     * Get history of user do something when use over them token
     */
    public Page<UserHistory> findAllUserHistories(HttpServletRequest httpServletRequest, Pageable pageable) {
        securityService.checkAdmin(httpServletRequest);
        return userHistoryRepository.findAll(pageable);
    }

    /**
     * request counting, it will count all request send to server, it will save to file at the path 'MonitorService.PATH_WRITE_LOG_MONITOR + "/request/"'
     */
    public RequestCounter[] findAllRequestCounter(HttpServletRequest httpServletRequest, Optional<LocalDate> date) {
        try {
            securityService.checkAdmin(httpServletRequest);
            String path = MonitorService.PATH_WRITE_LOG_MONITOR + "/request/" + date.orElse(LocalDate.now()) + ".json";
            if (!Files.exists(Paths.get(path))) throw new RuntimeException("Path file not exist");
            String content = FileService.readFile(path);
            RequestCounter[] result = ConfigService.GSON.fromJson(content, RequestCounter[].class);
            Arrays.sort(result, Comparator.comparing(RequestCounter::getCount).reversed());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("SecurityService.findAllRequestCounter " + e.getMessage());
        }
    }

    /**
     * Get list file save request counter
     */
    public List<String> findAllFileLogRequestCounter(HttpServletRequest httpServletRequest) {
        securityService.checkAdmin(httpServletRequest);
        List<String> list = new ArrayList<>();
        dFileService.scanDirectory(MonitorService.PATH_WRITE_LOG_MONITOR + "/request/").forEach(fileInfo -> {
            try{
                String[] split = fileInfo.fileName().split("\\.");
                if (split.length == 2) {
                    list.add(split[0]);
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        });
        return list;
    }

}
