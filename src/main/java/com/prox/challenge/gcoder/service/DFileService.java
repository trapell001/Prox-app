package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.DFile;
import com.prox.challenge.gcoder.model.DFileFolder;
import com.prox.challenge.gcoder.model.FileInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
public class DFileService {
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SecurityService securityService;

    public List<FileInfo> scanDirectorySecurityAdmin(HttpServletRequest httpServletRequest, String path) {
        securityService.checkAdmin(httpServletRequest);
        return scanDirectory(path);
    }

    /**
     * just get file list of current folder, info of file about : name, path, size, type
     */
    public List<FileInfo> scanDirectory(String path) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        File folder = new File(path);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) throw new RuntimeException("files is null");
            for (var file : files) {
                fileInfoList.add(new FileInfo(
                                file.getName(),
                                file.getPath().replaceAll("\\\\", "/"),
                                file.length(), file.isFile(), file.lastModified(),
                                (file.isFile()) ? file.getName().substring(file.getName().lastIndexOf(".") + 1) : "Folder"
                        )
                );
            }
        } else throw new RuntimeException("path is not directory");
        return fileInfoList;
    }

    /**
     * get list file in current folder and children folder
     */
    public List<DFile> scanDirectory(File directory, boolean scatter) {
        List<DFile> folder = new ArrayList<>();
        // Lấy danh sách tất cả các tệp tin và thư mục con trong thư mục hiện tại
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // Nếu là một tệp tin, tạo một đối tượng FileData và thêm vào danh sách
                    String name = file.getName().strip();
                    String path = file.getPath().replace("\\\\", "/");
                    String type = getFileType(name);
                    folder.add(new DFile(name, type, path));
                } else if (file.isDirectory()) {
                    if (scatter) {
                        folder.addAll(scanDirectory(file, true));
                    } else {
                        // Nếu là một thư mục, tiếp tục quét thư mục đó
                        folder.add(new DFileFolder(file.getName().strip(), file.getPath().replace("\\\\", "/"), scanDirectory(file, false)));
                    }
                }
            }
        }
        return folder;
    }

    public String getFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "NO TYPE";
    }

    // --------------------------- Action file ----------------------------------
    public void actionCopySecurityAdmin(HttpServletRequest httpServletRequest, String sourcePath, String destinationPath) {
        securityService.checkAdmin(httpServletRequest);
        actionCopy(sourcePath, destinationPath);
    }

    public void actionCopy(String sourcePath, String destinationPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(destinationPath + sourcePath.substring(sourcePath.lastIndexOf("/")));
            if (Files.isDirectory(source)) {
                // Nếu nguồn là thư mục, thực hiện sao chép toàn bộ thư mục và các tập tin con
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = destination.resolve(source.relativize(file));
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetDir = destination.resolve(source.relativize(dir));
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // Nếu nguồn là tập tin, thực hiện sao chép tập tin đơn lẻ
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void actionCutSecurityAdmin(HttpServletRequest httpServletRequest, String sourcePath, String destinationPath) {
        securityService.checkAdmin(httpServletRequest);
        actionCut(sourcePath, destinationPath);
    }

    public void actionCut(String sourcePath, String destinationPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(destinationPath + sourcePath.substring(sourcePath.lastIndexOf("/")));
            // Sao chép tệp/thư mục đến nơi đích
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean actionNewFolderSecurityAdmin(HttpServletRequest httpServletRequest, String sourcePath, String folderName) {
        securityService.checkAdmin(httpServletRequest);
        return actionNewFolder(sourcePath, folderName);
    }

    public boolean actionNewFolder(String sourcePath, String folderName) {
        File file = new File(sourcePath + folderName);
        return file.mkdirs();
    }

    public void actionRenameSecurityAdmin(HttpServletRequest httpServletRequest, String path, String newName) {
        securityService.checkAdmin(httpServletRequest);
        actionRename(path, newName);
    }

    public void actionRename(String path, String newName) {
        File fileOrDirectory = new File(path);

        // Kiểm tra xem đường dẫn tồn tại và có thể đổi tên hay không
        if (!fileOrDirectory.exists()) {
            throw new RuntimeException("[action rename] >>> File or directory does not exist.");
        }

        // Lấy đường dẫn cha của file hoặc thư mục để tạo tên mới
        String parentPath = fileOrDirectory.getParent();
        String separator = System.getProperty("file.separator");
        String newPath = parentPath + separator + newName.strip();

        // Kiểm tra xem đường dẫn mới đã tồn tại hay chưa
        File newFileOrDirectory = new File(newPath);
        if (newFileOrDirectory.exists()) {
            throw new RuntimeException("[action rename] >>> A file or directory with the new name already exists.");
        }

        // Thực hiện việc đổi tên
        if (fileOrDirectory.renameTo(newFileOrDirectory)) {
            log.info("[action rename] >>> path" + path + ">>>" + newName);
            log.info("[action rename] >>> successful.");
        } else {
            throw new RuntimeException("[action rename] >>> Rename failed.");
        }
    }

    public void actionDeleteSecurityAdmin(HttpServletRequest httpServletRequest, String path) {
        securityService.checkAdmin(httpServletRequest);
        actionDelete(path);
    }

    public void actionDelete(String path) {
        File file = new File(path);
        // Kiểm tra xem tệp tin hoặc thư mục tồn tại hay không
        if (!file.exists()) {
            throw new RuntimeException("file / folder not found");
        }
        // Nếu là một tệp tin
        if (file.isFile()) {
            if (!file.delete()) throw new RuntimeException("Cant delete file");
        } else
            // Nếu là một thư mục
            deleteFolder(file);
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // Gọi đệ quy để xóa các thư mục con
                } else {
                    if (!file.delete()) throw new RuntimeException("Cant delete file"); // Xóa tệp tin
                }
            }
        }
        if (!folder.delete()) throw new RuntimeException("Cant delete folder"); // Xóa thư mục
    }

    // --------------------------- Response ----------------------------------
    public Resource getFile(HttpServletRequest httpServletRequest, String filename) {
        securityService.checkAdmin(httpServletRequest);
        return getFile(filename);
    }

    public Resource getFile(String filename) {
        // Xây dựng đường dẫn đến tệp trong thư mục bạn muốn truy cập
        String fullPath = "file:" + filename;

        // Sử dụng ResourceLoader để tạo một Resource từ đường dẫn
        Resource resource = resourceLoader.getResource(fullPath);

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not Found");
        }
    }

    // --------------------------- Zip ----------------------------------
    public void zip(String sourceFolderPath, String zipFilePath) throws IOException {
        File sourceFolder = new File(sourceFolderPath);
        File zipFile = new File(zipFilePath);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            compressFolderToZip(sourceFolder, zos);
        }
    }

    private void compressFolderToZip(File sourceFolder, ZipOutputStream zos) throws IOException {
        File[] files = sourceFolder.listFiles();
        byte[] buffer = new byte[1024];

        for (File file : files) {
            if (file.isDirectory()) {
                compressFolderToZip(file, zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String entryName = file.getName();
                    ZipEntry ze = new ZipEntry(entryName);
                    zos.putNextEntry(ze);

                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }

                    zos.closeEntry();
                }
            }
        }
    }

    public void extractZipFile(String zipFilePath, String targetFolder) throws IOException {
        File targetDirectory = new File(targetFolder);
        targetDirectory.mkdirs();

        try (FileInputStream fis = new FileInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                String entryName = ze.getName();
                File newFile = new File(targetFolder + File.separator + entryName);

                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    //------------------------------- Download file ---------------------------------------
    public void downloadFileFromUrl(String fileUrl, String destinationPath) {
        try {
            // Sử dụng RestTemplate để tải file từ URL
            byte[] fileContent = restTemplate.getForObject(fileUrl, byte[].class);

            if (fileContent != null) {
                // Lưu nội dung file vào đường dẫn đích
                try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                    fos.write(fileContent);
                }
                System.out.println("File đã được tải về thành công tại: " + destinationPath);
            } else {
                System.out.println("Không tìm thấy file hoặc có lỗi trong quá trình tải.");
            }
        } catch (IOException e) {
            log.error(e);
            System.err.println("Lỗi trong quá trình tải file từ URL: " + e.getMessage());
        }
    }

    public void downloadFileFromUrl(String fileUrl, String destinationPath, HttpHeaders headers) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                byte[] fileContent = response.getBody();
                if (fileContent != null) {
                    try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                        fos.write(fileContent);
                    }
                    System.out.println("File đã được tải về thành công tại: " + destinationPath);
                } else {
                    System.out.println("Không tìm thấy file hoặc có lỗi trong quá trình tải.");
                }
            } else {
                System.out.println("Lỗi trong quá trình tải file, mã trạng thái: " + response.getStatusCodeValue());
            }
        } catch (IOException e) {
            log.error(e);
            System.err.println("Lỗi trong quá trình tải file từ URL: " + e.getMessage());
        }
    }


}
