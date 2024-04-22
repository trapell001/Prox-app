package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.ConfigValue;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.auth.*;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class RestTemplateService {
    private final List<String> proxies = new ArrayList<>();
    private final List<RestTemplate> restTemplateList = new ArrayList<>();
    private PoolingHttpClientConnectionManager connectionManager;
    @Autowired
    private ConfigService configService;
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        configService.addConfigActions("proxy list", "149.28.197.42:49381:user49381:fljdDPwMLw, 149.28.249.123:49322:user49322:XLPmQHQiNr, 149.248.4.64:49242:user49242:oVUBdsSsbM, 149.248.4.64:49271:user49271:YeaQglFLSL, 66.42.74.84:49288:user49288:zCvEklrKnw, 144.202.119.30:49017:user49017:fYHUQvTpCV, 207.246.67.78:49346:user49346:rpenHPSQJU, 149.248.4.64:49100:user49100:JWLXcWBbnC, 149.28.126.59:49288:user49288:gdeRzgaWtj, 216.128.143.82:49387:user49387:FFprcOyXGb, 23.157.216.45:49354:user49354:HDGVndA6wY, 23.157.216.49:49363:user49363:8LFosxKNGw, 23.157.216.55:49238:user49238:AGMaPPE5N5, 23.157.216.56:49380:user49380:ySP3q28Tuw, 23.157.216.52:49114:user49114:IRHytgBHSV, 23.157.216.28:49438:user49438:Cvlbrk1A4O, 23.157.216.24:49481:user49481:gyX9Ee3DdX, 23.157.216.56:49468:user49468:NkPcy5Jr3e, 23.157.216.25:49372:user49372:Py3eP87yV8, 23.157.216.51:49308:user49308:lETXrKTDsi, 23.157.216.57:49164:user49164:pxACln38e0, 23.157.216.25:49156:user49156:dLfYe8QIyL, 23.157.216.40:49329:user49329:wdHawaIZi9, 23.157.216.26:49352:user49352:UVPOQSHCBS, 23.157.216.41:49462:user49462:uKh4wtnbFa, 23.157.216.41:49435:user49435:t281RTBv0c, 23.157.216.54:49299:user49299:Ifx86xYtGP, 23.157.216.56:49220:user49220:IqqwzAiWyp, 23.157.216.28:49088:user49088:QJOsxgMvVa, 23.157.216.50:49440:user49440:7PL38Hm4dM,", "list proxy to config", ConfigValue.Type.client, this::createRestTemplate);
    }

    private void createRestTemplate(String s) {
        restTemplateList.clear();
        restTemplateList.add(restTemplate);
        proxies.clear();
        String[] list = s.split(",");
        for(var l : list){
            if(l != null) proxies.add(l.strip());
        }
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(10))
                .build());
        for (String proxy : proxies) {
            String[] p = proxy.split(":");
            if(p.length!=4) continue;
            final String username = p[2];
            final String password = p[3];
            final String proxyUrl = p[0];
            final int port = Integer.parseInt(p[1]);
            final int timeout = 5000;
            RestTemplate restTemplate = new RestTemplate();

            final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxyUrl, port), new UsernamePasswordCredentials(username, password.toCharArray()));
            final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            clientBuilder.setProxy(new HttpHost(proxyUrl, port))
                    .setKeepAliveStrategy(((response, context) -> TimeValue.ofHours(1)))
                    .setDefaultCredentialsProvider(credsProvider)
                    .setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy())
                    .setConnectionManager(connectionManager);
            final CloseableHttpClient client = clientBuilder.build();
            final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(client);
            factory.setConnectTimeout(timeout);
            factory.setConnectionRequestTimeout(timeout);
            restTemplate.setRequestFactory(factory);
            restTemplateList.add(restTemplate);
        }
        log.info("[Proxy create] : " + restTemplateList.size());
    }


    int i = 0;
    public RestTemplate get() {
        if (i >= proxies.size()) i = 0;
        return restTemplateList.get(i++);
    }

    public int getConnectCount() {
        PoolStats poolStats = connectionManager.getTotalStats();
        return poolStats.getLeased() + poolStats.getPending();
    }

    public void closeConnectExpiredAndIdle() {
        connectionManager.closeExpired();
        connectionManager.closeIdle(TimeValue.ofMinutes(1));
    }
}
