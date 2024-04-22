package com.prox.challenge.gcoder.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.io.IOException;
import java.security.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

//implement
//implementation group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.11.5'
//runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.11.5'
//runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.11.5'

//Example
//
//@Configuration
//public class ProjectConfig {
//@Bean
//public GCoderSecurity gCoderSecurity (){
//        String secretKey = "faaefa9dfasdfa09sd8fa0s9e8f0a9sndfoijsdoniagaklsdhkcadhkahdkasdnkfchakdhha9sd8gasdg7a9sd8gad87gada98dsg79a8d7gnca8dsg7";
//        long tokenExpirationTime = 3000;
//        return new GCoderSecurity(name,secretKey, tokenExpirationTime);
//        }
//@Bean
//public FilterRegistrationBean<Filter> registrationBean(GCoderSecurity gCoderSecurity){
//        List<GCoderSecurity.URL> urlFilter = List.of(new GCoderSecurity.URL("/pain/*", "POST", "DELETE"));
//        return gCoderSecurity.createJWTFilter(urlFilter,2);
//        }
//}


@Log4j2
public class GCoderSecurity {
    private final JwtService JWT_SERVICE;

    /**
     * this property will fill all request filter by token
     */
    public static RequestDetailAction requestDetailAction;

    /**
     * make security with random key
     */
    public GCoderSecurity() {
        this.JWT_SERVICE = new JwtService("APP", null, null);
    }

    /**
     * make security with all info
     */
    public GCoderSecurity(String issuer, String secretKey, Long tokenExpirationTime) {
        this.JWT_SERVICE = new JwtService(issuer, secretKey, tokenExpirationTime);
    }

    /**
     * make bean in config
     * @Bean
     * public FilterRegistrationBean<Filter> registrationBean(GCoderSecurity gCoderSecurity){
     *         List<GCoderSecurity.URL> urlFilter = List.of(new GCoderSecurity.URL("/pain/*", "POST", "DELETE"));
     *         return gCoderSecurity.createJWTFilter(urlFilter,2);
     *         }
     * }
     *
     * @param urlFilter list url will be filtered
     * @param order order in filter
     * @param socket if api is socket connect
     */
    private FilterRegistrationBean<Filter> createJWTFilter(List<URL> urlFilter, int order, boolean socket) {
        String[] url = new String[urlFilter.size()];
        for (int i = 0; i < urlFilter.size(); i++) {
            url[i] = urlFilter.get(i).url;
        }
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FilterClass(urlFilter, JWT_SERVICE, socket));
        registrationBean.addUrlPatterns(url);
        registrationBean.setOrder(order); //set precedence
        return registrationBean;
    }

    // Dùng để đăng ký bean filter cho spring boot
    // urlFilter : những đường dẫn sẽ được chọn để filter
    // urlPass : những đường dẫn sẽ được loại loại bỏ trong đường dẫn trên, ví dụ 1 đường dẫn chi tiết hơn của đường dẫn filter
    // order : thứ tự ưu tiên của filter
    public FilterRegistrationBean<Filter> createJWTFilter(List<URL> urlFilter, int order) {
        return createJWTFilter(urlFilter, order, false);
    }

    // Dùng để đăng ký bean filter socket cho spring boot
    // urlFilter : những đường dẫn sẽ được chọn để filter
    // urlPass : những đường dẫn sẽ được loại loại bỏ trong đường dẫn trên, ví dụ 1 đường dẫn chi tiết hơn của đường dẫn filter
    // order : thứ tự ưu tiên của filter
    // quy định token : ws://host/uri?token={token}
    public FilterRegistrationBean<Filter> createJWTFilterSocket(List<URL> urlFilter, int order) {
        return createJWTFilter(urlFilter, order, true);
    }

    public String createJwt(String subject) {
        return JWT_SERVICE.createJwt(subject);
    }

    public String parseJwt(String token) {
        return JWT_SERVICE.parseJwt(token);
    }

    public String parseJwt(HttpServletRequest httpServletRequest) {
        return JWT_SERVICE.parseJwt(httpServletRequest);
    }

    public String createJws(String subject) {
        return JWT_SERVICE.createJws(subject);
    }

    public String parseJws(String token) {
        return JWT_SERVICE.parseJws(token);
    }

    public String parseJws(HttpServletRequest httpServletRequest) {
        return JWT_SERVICE.parseJws(httpServletRequest);
    }

    private static class JwtService {
        private final Long EXPIRATION_TIME;
        private PrivateKey PRIVATE_KEY;
        private PublicKey PUBLIC_KEY;
        private Key KEY;
        private final String ISSUER;

        public JwtService(String issuer, String secretKey, Long tokenExpirationTime) {
            EXPIRATION_TIME = tokenExpirationTime;
            this.ISSUER = issuer;
            try {
                if (secretKey != null) KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
                else KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                // Thiết lập độ dài khóa là 2048 bit
                keyPairGenerator.initialize(2048);
                // Tạo cặp khóa công khai / riêng tư RSA
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                // Lấy riêng tư khóa
                PRIVATE_KEY = keyPair.getPrivate();
                // Lấy khóa công khai
                PUBLIC_KEY = keyPair.getPublic();
                startTest();
            } catch (Exception e) {
                log.error(e);
            }
        }

        public String createJwt(String subject) {
            JwtBuilder jwt = Jwts.builder()
                    .setSubject(subject)
                    .setIssuer(ISSUER)
                    .signWith(KEY);
            if (EXPIRATION_TIME != null) jwt.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));
            return jwt.compact();
        }

        public String parseJwt(String token) throws JwtException {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseClaimsJws(token).getBody().getSubject();
        }

        public String createJws(String subject) {
            return Jwts.builder()
                    .setSubject(subject)
                    .setIssuer(ISSUER)
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(PRIVATE_KEY, SignatureAlgorithm.RS256)
                    .compact();
        }

        public String parseJws(String token) {
            return Jwts.parserBuilder()
                    .requireIssuer(ISSUER)
                    .setSigningKey(PUBLIC_KEY)
                    .build()
                    .parseClaimsJws(token).getBody().getSubject();
        }

        public void startTest() {
            String token = createJwt("admin");
            String parseResult = "OK";
            try {
                parseJwt(token);
            } catch (JwtException e) {
                parseResult = e.getMessage();
            }
            log.info("\n[Security Test]\n + Token : " + token + "\n + Result : " + parseResult);
        }

        private String parseJwt(HttpServletRequest httpServletRequest) throws JwtException {
            String token = httpServletRequest.getHeader("Authorization");
            if (token == null || token.isEmpty()) throw new JwtException("Token is null");
            return parseJwt(token);
        }

        private String parseJws(HttpServletRequest httpServletRequest) throws JwtException {
            String token = httpServletRequest.getHeader("Authorization");
            if (token == null || token.isEmpty()) throw new JwtException("Token is null");
            return parseJws(token);
        }
    }

    public static class FilterClass implements Filter {
        public final List<URL> START_URI_PASS;
        private final JwtService JWT_SERVICE;
        private final boolean SOCKET;

        public FilterClass(@NonNull List<URL> startUrlPass, @NonNull JwtService jwtService, boolean socket) {
            this.START_URI_PASS = startUrlPass;
            this.JWT_SERVICE = jwtService;
            this.SOCKET = socket;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            String uri = httpServletRequest.getRequestURI();
            String method = httpServletRequest.getMethod();
            String token;
            String dataParse;

            // ignore uri
            if (!checkFilter(uri, method)) {
                try{
                    chain.doFilter(request, response);
                    return;
                }catch (Exception e){
                    log.error(e);
                }
            }
            if (SOCKET) {
                token = httpServletRequest.getParameter("token");
            } else {
                token = httpServletRequest.getHeader("Authorization");
            }
            if (token == null) {
                sendAuthorException(httpServletResponse, "token is null");
                return;
            }
            // parse token
            try {
                dataParse = JWT_SERVICE.parseJwt(httpServletRequest);
            } catch (JwtException jwtException) {
                sendAuthorException(httpServletResponse, jwtException.getMessage());
                return;
            }
            RequestDetail requestDetail = new RequestDetail(httpServletRequest, httpServletResponse, uri, method, token, dataParse);
            // one filter
            try{
                if(GCoderSecurity.requestDetailAction != null) requestDetailAction.run(requestDetail);
            } catch (Exception e){
                sendAuthorException(httpServletResponse, e.getMessage());
            }
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
        // trường hợp nếu trùng với url phải lọc thì > true
        private boolean checkFilter(String url, String method) {
            AtomicBoolean filter = new AtomicBoolean(false);
            Optional<URL> urlMapping = this.START_URI_PASS.stream()
                    .filter(t -> url.startsWith(t.url.replace("*", ""))).findFirst();
            urlMapping.ifPresent(
                    url1 -> {
                        if(Arrays.stream(url1.methods).anyMatch(method1 -> Method.valueOf(method1) == Method.valueOf(method))) filter.set(true);
                    });
            return filter.get();
        }

        private void sendAuthorException(HttpServletResponse httpServletResponse, String detail) throws IOException {
            try{
                httpServletResponse.setContentType("application/json");
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                String errorJson = """
                    {
                        "title" : "Authorization",
                        "detail" : "%s"
                    }
                    """.replace("%s", detail);
                //Fix lỗi CORS
                httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
                httpServletResponse.getWriter().write(errorJson);
            }catch (Exception e){
                log.error(e);
            }
        }
    }

    public enum Method {
        GET("GET"), PUT("PUT"), POST("POST"), DELETE("DELETE"), OPTIONS("OPTIONS");

        Method(String value) {
        }
    }
    public record RequestDetail(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String uri, String method, String token, String dataParseToken){}
    public record URL(String url, String... methods) {
    }
    @FunctionalInterface
    public interface RequestDetailAction {
        void run(RequestDetail requestDetail) throws Exception;
    }
}
