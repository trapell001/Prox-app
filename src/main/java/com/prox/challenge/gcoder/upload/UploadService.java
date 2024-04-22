package com.prox.challenge.gcoder.upload;

import com.prox.challenge.gcoder.service.ConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

@Service
public class UploadService {
    public static GCoderSocket.SocketUpload upload = new GCoderSocket.SocketUpload("/upload", "file");
    @PostConstruct
    private void init(){
        upload.uploadResult = (session, fileInfo) -> {
            String path = "path|" + ConfigService.URL_FIRST_FILE + fileInfo.fileName();
            System.out.println(path);
            session.sendMessage(new TextMessage(path));
        };
    }

}
