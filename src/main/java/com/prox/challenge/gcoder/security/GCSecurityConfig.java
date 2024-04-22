package com.prox.challenge.gcoder.security;

import com.prox.challenge.gcoder.model.*;
import com.prox.challenge.gcoder.repository.UserHistoryRepository;
import com.prox.challenge.gcoder.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@Log4j2
public class GCSecurityConfig {
    @Autowired
    private UserHistoryRepository userHistoryRepository;
    @Autowired
    private UserRepository userRepository;
    private static final List<GCoderSecurity.URL> filter = new ArrayList<>();
    private RequestCounterCover requestCounterCover;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        GCoderSecurity.requestDetailAction = requestDetail -> {
            requestDetail.httpServletRequest().setAttribute("email", requestDetail.dataParseToken());
            UserHistory userHistory = new UserHistory();
            userHistory.setUri(requestDetail.uri());
            userHistory.setEmail(requestDetail.dataParseToken());
            userHistory.setMethod(requestDetail.method());
            userHistoryRepository.save(userHistory);
        };
        makeAdmin();
        requestCounterCover = new RequestCounterCover();
        scheduler.scheduleAtFixedRate(this::writeFileRequestCounter, 0, 10, TimeUnit.SECONDS);
    }

    private void writeFileRequestCounter() {
        try {
            if (requestCounterCover != null) requestCounterCover.writeFile();
        } catch (Exception e) {
            log.error("GCSecurityConfig.writeFileRequestCounter " + e.getMessage());
        }
    }

    private void makeAdmin() {
        userRepository.findByEmail("admin").orElseGet(() -> {
            User user = new User();
            user.setEmail("admin");
            user.setPassword("Proxglobal@123$");
            user.setRole(Role.admin);
            return userRepository.save(user);
        });
    }

    public static void addUriSecurity(String uri, String... method) {
        filter.add(new GCoderSecurity.URL(uri, method));
    }

    @Bean
    public GCoderSecurity tokenFilter() {
        return new GCoderSecurity(
                "Pain_2023",
                "faaefa9dfasdfa09sd8fa0s9e8f0a9sndfoijsdoniagaklsdhkcadhkahdkasdnkfchakdhha9sd8gasdg7a9sd8gad87gada98dsg79a8d7gnca8dsg7",
                1000L * 60 * 60 * 5);
    }

    @Bean
    public FilterRegistrationBean<Filter> registrationBean(GCoderSecurity tokenFilter) {
        filter.add(new GCoderSecurity.URL("/security", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/security/*", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/config", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/config/*", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/config/*", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/file-manager", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/file-manager/*", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/log/*", "GET", "POST", "PUT", "DELETE"));
        filter.add(new GCoderSecurity.URL("/backup/*", "GET", "POST", "PUT", "DELETE"));
        return tokenFilter.createJWTFilter(filter, 2);
    }

    /**
     * This filter to gather quantity request
     */
    @Bean
    @Transactional
    public FilterRegistrationBean<Filter> registrationFilterAll() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.addUrlPatterns("/*");
        filter.setOrder(1);
        filter.setFilter((request, response, chain) -> {
            try {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                String uri = httpServletRequest.getRequestURI();
                if (!requestCounterCover.getDate().equals(LocalDate.now().toString())) {
                    requestCounterCover.writeFile();
                    requestCounterCover = new RequestCounterCover().add(uri);
                } else {
                    requestCounterCover.add(uri);
                }
            } catch (Exception e) {
                log.error(e);
            } finally {
                try {
                    chain.doFilter(request, response);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        });
        return filter;
    }
}
