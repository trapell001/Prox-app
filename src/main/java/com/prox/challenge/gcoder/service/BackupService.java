package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.ConfigValue;
import com.prox.challenge.gcoder.model.FileInfo;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
@Log4j2
public class BackupService {
    @Autowired
    private DFileService dFileService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${spring.datasource.url}")
    private String database;
    @Value("${spring.datasource.username}")
    private String usernameDatabase;
    @Value("${spring.datasource.password}")
    private String passwordDatabase;
    private boolean runFirst = false;
    public static String PATH_BACKUP;
    public static int TIME_DAILY_HOUR = 24;
    public static String URL_DOWNLOAD_BACKUP_FILE;
    public static String URL_DOWNLOAD_BACKUP_MYSQL;
    public static String PASS_TARGET_SERVER_BACKUP;
    public static String PASS_BACKUP;
    public static int DAY_SAVE_BACKUP;

    @PostConstruct
    private void init() {
        configService.addConfigActions("Path Backup", "/home/server/backup/", "file backup will save in path", ConfigValue.Type.backup, s -> {
            Path path = Paths.get(s);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            PATH_BACKUP = s;
        });
        configService.addConfigActions("url download backup file from target server (string)", "", "Url will download file backup file, if set empty or none will ignore update", ConfigValue.Type.backup, s -> {
            URL_DOWNLOAD_BACKUP_FILE = s;
            if (runFirst) {
                downloadFileBackup();
            }
        });
        configService.addConfigActions("url download backup mysql from target server (string)", "", "Url will download file backup mysql, if set empty or none will ignore update", ConfigValue.Type.backup, s -> {
            URL_DOWNLOAD_BACKUP_MYSQL = s;
            if (runFirst) {
                downloadMysqlBackup();
            }
        });
        configService.addConfigActions("Daily Backup (Hours:int)", "24", "Time between (House dai) to start backup, this backup about : download file backup (file *.zip, mysql *.sql) form target server and restore main server, after make file backup(zip and sql) in day , path save file backup is value of 'Path Backup'", ConfigValue.Type.backup, s -> {
            TIME_DAILY_HOUR = Integer.parseInt(s);
        });
        configService.addConfigActions("password backup of target server (string)", "12345678", "Url will download file backup file", ConfigValue.Type.backup, s -> {
            PASS_TARGET_SERVER_BACKUP = s;
        });
        configService.addConfigActions("password backup of main backup (string)", "12345678", "Url will download file backup file", ConfigValue.Type.backup, s -> {
            PASS_BACKUP = s;
        });
        configService.addConfigActions("backup save around (day : int)", "5", "days will save backup, if over day config will delete", ConfigValue.Type.backup, s -> {
            DAY_SAVE_BACKUP = Integer.parseInt(s);
        });
        runFirst = true;
//        ConfigService.SCHEDULED.scheduleAtFixedRate(this::backup, 0, TIME_DAILY_HOUR, TimeUnit.HOURS);
    }

    private void backup() {
        downloadFileBackup();
        downloadMysqlBackup();
        // Daily make file backup
        try {
            dFileService.zip(ConfigService.PATH_PUBLIC_FILE, PATH_BACKUP + "backup-file.zip");
        } catch (Exception e) {
            log.error("BackupService.backup().fileBackup : " + e.getMessage());
        }
        try {
            createBackupMysql(PATH_BACKUP + LocalDate.now() + ".backup-mysql.sql");
        } catch (Exception e) {
            log.error("BackupService.backup().mysqlBackup : " + e.getMessage());
        }
        createBackupH2();
        deleteFileOverDay();
    }


    public void downloadFileBackup() {
        ConfigService.SCHEDULED.submit(() -> {
            try {
                if (URL_DOWNLOAD_BACKUP_FILE.isEmpty() || URL_DOWNLOAD_BACKUP_FILE.equals("none")) return;
                String path = PATH_BACKUP + "backup-file.zip";
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("password", PASS_TARGET_SERVER_BACKUP);
                dFileService.downloadFileFromUrl(URL_DOWNLOAD_BACKUP_FILE, path, httpHeaders);
                dFileService.extractZipFile(path, ConfigService.PATH_PUBLIC_FILE);
                Files.delete(Paths.get(path));
            } catch (Exception e) {
                log.error("BackupService.downloadFileBackup() : " + e.getMessage());
            }
        });
    }

    public void downloadMysqlBackup() {
        if (database == null || !database.contains("mysql")) return;
        ConfigService.SCHEDULED.submit(() -> {
            try {
                if (URL_DOWNLOAD_BACKUP_MYSQL.isEmpty() || URL_DOWNLOAD_BACKUP_MYSQL.equals("none")) return;
                String path = PATH_BACKUP + "backup-mysql.sql";
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("password", PASS_TARGET_SERVER_BACKUP);
                dFileService.downloadFileFromUrl(URL_DOWNLOAD_BACKUP_MYSQL, path, httpHeaders);
                restoreMysqlBackup(path);
                Files.delete(Paths.get(path));
            } catch (Exception e) {
                log.error("BackupService.downloadMysqlBackup() : " + e.getMessage());
            }
        });
    }

    public Resource createFileBackupApi(HttpServletRequest httpServletRequest) throws IOException {
        if (database == null || !database.contains("mysql")) return null;
        String pass = httpServletRequest.getHeader("password");
        if (PASS_BACKUP.equals(pass)) {
            String path = PATH_BACKUP + "backup-file.zip";
            dFileService.zip(ConfigService.PATH_PUBLIC_FILE, path);
            return dFileService.getFile(path);
        } else throw new RuntimeException("Password wrong");
    }

    public Resource createMysqlBackupApi(HttpServletRequest httpServletRequest) {
        if (database == null || !database.contains("mysql")) return null;
        String pass = httpServletRequest.getHeader("password");
        if (PASS_BACKUP.equals(pass)) {
            String path = PATH_BACKUP + LocalDate.now() + ".backup-mysql.sql";
            createBackupMysql(path);
            return dFileService.getFile(path);
        } else throw new RuntimeException("Password wrong");
    }

    public void createBackupMysql(String pathFileBackup) {
        if (database == null || !database.contains("mysql")) return;
        try {
            String[] parts = database.split("/");
            String database = parts[parts.length - 1];
            /*NOTE: Used to create a cmd command*/
            String executeCmd = "mysqldump -u" + usernameDatabase + " -p" + passwordDatabase + " " + database + " -r " + pathFileBackup;
            /*NOTE: Executing the command here*/
            Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            /*NOTE: processComplete=0 if correctly executed, will contain other values if not*/
            if (processComplete == 0) {
                log.info("Backup Mysql Complete");
            } else {
                log.error("Backup Mysql Failure");
            }
        } catch (Exception e) {
            throw new RuntimeException("Backup Mysql Failure : " + e.getMessage());
        }
    }

    private void restoreMysqlBackup(String pathFileBackup) {
        if (database == null || !database.contains("mysql")) return;
        try {
            String[] parts = database.split("/");
            String database = parts[parts.length - 1];
            /*NOTE: Used to create a cmd command*/
            String[] restoreCmd = new String[]{"mysql", "-u" + usernameDatabase, "-p" + passwordDatabase, database, "-e", "source " + pathFileBackup};
            Runtime rt = Runtime.getRuntime();
            rt.exec(restoreCmd);
            log.info("Restored mysql successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Restored mysql error! : " + e.getMessage());
        }
    }

    /**
     * check file over too long with 'DAY_SAVE_BACKUP' to delete
     */
    public void deleteFileOverDay() {
        long dayOver = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * DAY_SAVE_BACKUP);
        try {
            List<FileInfo> infoList = dFileService.scanDirectory(PATH_BACKUP);
            infoList.forEach(fileInfo -> {
                if (fileInfo.lastModified() <= dayOver) {
                    Path path = Paths.get(fileInfo.path());
                    if (Files.exists(path)) {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            log.error("deleteFileOverDay [" + fileInfo.fileName() + "]: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("deleteFileOverDay : " + e.getMessage());
        }
    }

    /**
     * Get list file sql backup
     */
    public List<String> getListFileBackupSql(HttpServletRequest httpServletRequest) {
        securityService.checkAdmin(httpServletRequest);
        List<FileInfo> listFile = dFileService.scanDirectory(PATH_BACKUP);
        List<String> result = new ArrayList<>();
        listFile.forEach(fileInfo -> {
            String[] slit = fileInfo.fileName().split("\\.");
            if (slit.length == 3 && slit[2].equals("sql")) {
                result.add(slit[0]);
            }
        });
        return result;
    }

    public void restoreMysql(HttpServletRequest httpServletRequest, @NonNull Optional<String> date) {
        securityService.checkAdmin(httpServletRequest);
        String path = PATH_BACKUP + date.orElseThrow(() -> new RuntimeException("date requirement")) + ".backup-mysql.sql";
        restoreMysqlBackup(path);
    }

    public void createBackupH2(){
        try{
            if(database == null || !database.contains("jdbc:h2:file:")) return;
            String pathBackup = PATH_BACKUP + LocalDate.now() + ".backup-h2.zip";
            String backupQuery = "BACKUP TO '" + pathBackup + "'";
            jdbcTemplate.execute(backupQuery);
            log.info("Create backup complete : " + pathBackup);
        }catch (Exception e){
            log.error("createBackupH2 : " + e.getMessage());
        }
    }

}
