package com.prox.challenge.gcoder.upload;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/*
implement: 	implementation 'org.springframework.boot:spring-boot-starter-websocket'
Should create in static block
Example:
static {
            SocketService.Socket socket = SocketService.createSocket("/abc1");
            socket.onConnect = (session)->{
                System.out.println(session.getId());
            };
            socket.onMessage = (session, message) -> {
                System.out.println(message.getPayload());
                if(message.getPayload().equals("dm")) throw new Exception("Cái gì cơ");
            };
            socket.error = (session, exception) -> {
                System.out.println(exception);
                session.sendMessage(new TextMessage(exception.getMessage()));
                session.close();
            };
        }
 */

@Log4j2
@Configuration
@EnableWebSocket
public class GCoderSocket implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        Set<Socket> sockets = Socket.SOCKET_INSTANCE;
        sockets.forEach(socket -> {
            System.out.println("Socket Register: " + socket.enPoint);
            socket.register(registry);
        });
    }

    public static Socket createSocket(String endPoint) {
        return new AbstractWebSocketHandler() {
            public final Socket SOCKET = new Socket(this, endPoint);

            @Override
            public void afterConnectionEstablished(@NonNull  WebSocketSession session) throws Exception {
                try {
                    if (SOCKET.onConnect == null) return;
                    SOCKET.onConnect.run(session);
                } catch (Exception e) {
                    handleTransportError(session, e);
                }
            }

            @Override
            protected void handleTextMessage(@NonNull  WebSocketSession session, @NonNull TextMessage message) throws Exception {
                try {
                    if (SOCKET.onMessage == null) return;
                    SOCKET.onMessage.run(session, message);
                } catch (Exception e) {
                    handleTransportError(session, e);
                }
            }

            @Override
            protected void handleBinaryMessage(@NonNull  WebSocketSession session, @NonNull BinaryMessage message) throws Exception {
                try {
                    if (SOCKET.onBinary == null) return;
                    SOCKET.onBinary.run(session, message);
                } catch (Exception e) {
                    handleTransportError(session, e);
                }
            }

            @Override
            public void handleTransportError(@NonNull  WebSocketSession session, @NonNull Throwable exception) throws Exception {
                if (SOCKET.error == null) return;
                SOCKET.error.run(session, exception);
            }

            @Override
            public void afterConnectionClosed(@NonNull  WebSocketSession session, @NonNull CloseStatus status) throws Exception {
                if (SOCKET.close == null) return;
                SOCKET.close.run(session, status);
            }
        }.SOCKET;
    }

    public static Optional<String> getParam(@NonNull  WebSocketSession session, String paramKey) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] queryParams = uri.split("\\?");
        if (queryParams.length > 1) {
            String[] paramPairs = queryParams[1].split("&");
            for (String paramPair : paramPairs) {
                String[] keyValue = paramPair.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(paramKey)) {
                    return Optional.of(keyValue[1]);
                }
            }
        }
        return Optional.empty(); // Trả về null nếu không tìm thấy giá trị tham số
    }

    public static Optional<String> getHeader(@NonNull  WebSocketSession session, String key) {
        List<String> value = session.getHandshakeHeaders().get(key);
        if (value != null && !value.isEmpty()) return Optional.of(value.get(0));
        return Optional.empty();
    }
    public static ResponseEntity<Resource> getFileResponse(String path) throws IOException {
        Path file = Paths.get(path);
        return ResponseEntity.ok().header("Content-Type", Files.probeContentType(file))
                .body(new FileSystemResource(file));
    }

    public static class Socket {
        private Socket(AbstractWebSocketHandler abstractWebSocketHandler, String endPoint) {
            this.SOCKET_HANDLER = abstractWebSocketHandler;
            this.enPoint = endPoint;
            SOCKET_INSTANCE.add(this);
        }

        private static final Set<Socket> SOCKET_INSTANCE = new HashSet<>();
        public FIAfterConnectionClosed close;
        public FIAfterConnectionEstablished onConnect;
        public FIHandleTextMessage onMessage;
        public FIHandleBinaryMessage onBinary;
        public FIHandleTransportError error;
        public final String enPoint;
        private final AbstractWebSocketHandler SOCKET_HANDLER;

        private void register(WebSocketHandlerRegistry registry) {
            registry.addHandler(SOCKET_HANDLER, enPoint).setAllowedOrigins("*");
        }
    }

    public static class SocketUpload {
        public static final String CODE_JS_UPLOAD = """
                export function uploadFile(file: File, url: string, path: string, action: (url: string) => any, percentAction?: (data: number) => any) {
                  const chunkSize = 1024 * 7; // Size of each piece of data (1 MB)
                  const totalChunks = Math.ceil(file.size / chunkSize); // data total
                  let fileType = file.name.split('.').pop()
                  let currentChunk = 0; // number percent downloaded
                  let sockets = new WebSocket(url + `?path=${path}&type=${fileType}&size=${file.size}`);
                  sockets.onopen = () => {
                    console.log("connected")
                    const reader = new FileReader();
                    reader.onload = (event: any) => {
                      const data: ArrayBuffer = event.target.result;
                      sockets.send(data);
                      currentChunk++;
                      if (currentChunk < totalChunks) {
                        // If you haven't uploaded all the data yet, continue to upload the next part
                        const start = currentChunk * chunkSize;
                        const end = Math.min(start + chunkSize, file.size);
                        const nextChunk = file.slice(start, end);
                        reader.readAsArrayBuffer(nextChunk);
                      }
                    };
                    // Start uploading the first piece of data
                    const firstChunk = file.slice(0, chunkSize);
                    reader.readAsArrayBuffer(firstChunk);
                  }
                  // finish upload, return url file
                  sockets.onmessage = function (event) {
                    let load: string = event.data;
                    let arr: string[] = load.split("|")
                    switch (arr[0]) {
                      case "path" :
                        action(arr[1]);
                        break;
                      case "error":
                        console.log(arr[1])
                        break;
                      case "size":
                        if (percentAction) percentAction(parseFloat(arr[1]))
                    }
                  };
                }
                """;
        private final Socket SOCKET;
        private String FOLDER_SAVE;
        public FISocketUploadProcess uploadProcess;
        public FISocketUploadResult uploadResult;
        public FIHandleTransportError error;

        public SocketUpload(@NonNull String endPoint, @NonNull String folderSave) {
            if (endPoint.isEmpty() || folderSave.isEmpty())
                throw new RuntimeException("endPoint and folderSave required!");
            this.FOLDER_SAVE = folderSave;
            try {
                Files.createDirectories(Paths.get(this.FOLDER_SAVE));
            } catch (IOException e) {
                throw new RuntimeException("cant create Folder save");
            }
            this.SOCKET = createSocket(endPoint);

            // default config
            error = (session, exception) -> session.sendMessage(new TextMessage("error|" + exception.getMessage()));
            uploadProcess = (session, percent) -> session.sendMessage(new TextMessage("size|" + percent));
            uploadResult = (session, fileInfo) -> session.sendMessage(new TextMessage("path|" + fileInfo.path));

            SOCKET.onConnect = session -> {
                Map<String, Object> attribute = session.getAttributes();
                FileInfo fileInfo = generatePath(session, FOLDER_SAVE);
                Files.createDirectories(Paths.get(fileInfo.direction));
                attribute.put("path_file", fileInfo);
                attribute.put("size", Float.parseFloat(getParam(session, "size").orElseThrow(() -> new RuntimeException("size param required"))));
            };
            SOCKET.onBinary = (session, message) -> {
                FileInfo fileInfo = ((FileInfo) session.getAttributes().get("path_file"));
                Path pathFile = Paths.get(fileInfo.path);
                ByteBuffer buffer = message.getPayload();
                Files.write(pathFile, buffer.array(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                float size = Files.size(pathFile);
                float sizeTotal = (float) session.getAttributes().get("size");
                if (size >= sizeTotal) {
                    if (uploadProcess != null) uploadProcess.run(session, 100f);
                    if (uploadResult != null) uploadResult.run(session, fileInfo);
                    session.close();
                } else {
                    if (uploadProcess != null) uploadProcess.run(session, (size / sizeTotal) * 100);
                }
            };
            SOCKET.error = (session, exception) -> {
                if (error != null) error.run(session, exception);
                session.close(CloseStatus.BAD_DATA);
            };
            SOCKET.close = (session, status) -> {
                if (!(status.getCode() == 1000)) {
                    String pathFile = ((FileInfo) session.getAttributes().get("path_file")).path;
                    Files.delete(Paths.get(pathFile));
                }
            };
        }

        public void setFolderSave(String path) {
            try{
                Path p = Paths.get(path);
                if(!Files.exists(p)){
                    Files.createDirectories(p);
                }
                FOLDER_SAVE = path;
            }catch (IOException e){
                throw new RuntimeException(e.getMessage());
            }

        }

        public String getEndPoint() {
            return SOCKET.enPoint;
        }

        public String folderSever() {
            return this.FOLDER_SAVE;
        }

        private FileInfo generatePath(@NonNull  WebSocketSession session, String folderSave) {
            String direction = folderSave + getParam(session, "path").orElse("");
            String type = getParam(session, "type").orElse("file");
            String fileName = UUID.randomUUID() + "." + type;
            String path = direction + fileName;
            return new FileInfo(path, fileName, type, direction);
        }
    }

    public record FileInfo(String path, String fileName, String type, String direction) {
    }

    @FunctionalInterface
    public interface FIHandleTextMessage {
        void run(@NonNull  WebSocketSession session,@NonNull TextMessage message) throws Exception;
    }

    @FunctionalInterface
    public interface FIHandleBinaryMessage {
        void run(@NonNull  WebSocketSession session, @NonNull BinaryMessage message) throws Exception;
    }

    @FunctionalInterface
    public interface FIHandleTransportError {
        void run(@NonNull  WebSocketSession session,@NonNull Throwable exception) throws Exception;
    }

    @FunctionalInterface
    public interface FIAfterConnectionEstablished {
        void run(@NonNull  WebSocketSession session) throws Exception;
    }

    @FunctionalInterface
    public interface FIAfterConnectionClosed {
        void run(@NonNull  WebSocketSession session,@NonNull CloseStatus status) throws Exception;
    }

    @FunctionalInterface
    public interface FISocketUploadProcess {
        void run(@NonNull  WebSocketSession session,float percent) throws Exception;
    }

    @FunctionalInterface
    public interface FISocketUploadResult {
        void run(@NonNull  WebSocketSession session,@NonNull FileInfo fileInfo) throws Exception;
    }
}
