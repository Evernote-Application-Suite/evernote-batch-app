package in.net.sudhir.evernotebatchapp.service;

import in.net.sudhir.evernotebatchapp.model.Batch;
import in.net.sudhir.evernotebatchapp.model.FileDB;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/***
 Package Name: in.net.sudhir.evernotebatchapp.service
 User Name: SUDHIR
 Created Date: 04-09-2022 at 07:03
 Description:
 */
@Service
public class FileXferSvc {

    @Autowired
    Environment environment;

    @Autowired
    DataService dataService;

    private String hostname;
    private String port;
    private String username;
    private String password;
    private String destination;
    private String fileSeperator = "/";;

    private static final Logger logger = LoggerFactory.getLogger(FileXferSvc.class);
    public FileXferSvc(Environment environment) {
        this.environment = environment;
        this.hostname = environment.getProperty("app.target.ftp.hostname");
        this.port = environment.getProperty("app.target.ftp.port");
        this.username = environment.getProperty("app.target.ftp.username");
        this.password = environment.getProperty("app.target.ftp.password");
        this.destination = environment.getProperty("app.staging.directory");
    }


    public void loadFiles() {
        try {
            Batch fileUploadBatch = dataService.startBatch("FTP_FILE_UPLOAD");
            String ftpMainDir = environment.getProperty("app.target.ftp.directory");
            AtomicLong recordsProcessed = new AtomicLong(0L);
            AtomicLong recordsExpected = new AtomicLong(0L);
            AtomicLong recordsFailed = new AtomicLong(0L);
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(hostname, Integer.parseInt(port));
            showServerReply(ftpClient);
            ftpClient.login(username, password);
            showServerReply(ftpClient);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile[] directories = ftpClient.listDirectories(fileSeperator + ftpMainDir);
            directories = Arrays.copyOfRange(directories, 2, directories.length);
            Arrays.asList(directories).forEach(directory -> {
                try {
                    String targetPath = destination + fileSeperator + directory.getName();
                    Path path = Paths.get(targetPath);
                    if(!Files.exists(path)){
                        Files.createDirectory(path);
                    }
                    FTPFile[] filesInDirectory = ftpClient.listFiles(fileSeperator + ftpMainDir + fileSeperator + directory.getName());
                    filesInDirectory = Arrays.copyOfRange(filesInDirectory, 2, filesInDirectory.length);
                    Arrays.asList(filesInDirectory).forEach(file -> {
                        recordsExpected.getAndIncrement();
                        String remoteFile = fileSeperator + ftpMainDir + fileSeperator + directory.getName() + fileSeperator + file.getName();
                        File downloadFile = new File(targetPath + fileSeperator + file.getName());
                        OutputStream outputStream = null;
                        try {
                            outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
                            boolean success = ftpClient.retrieveFile(remoteFile, outputStream);
                            if(success){
                                recordsProcessed.getAndIncrement();
                                FileDB fileDB = new FileDB();
                                fileDB.setFileName(downloadFile.getName());
                                fileDB.setFileSize((downloadFile.getTotalSpace()/1024)/1024);
                                fileDB.setDirectoryName(directory.getName());
                                fileDB.setIsUploaded(false);
                                dataService.addFileToDB(fileDB);
                            }else{
                                recordsFailed.getAndIncrement();
                            }
                            outputStream.close();
                        } catch (FileNotFoundException e) {
                            logger.error("Error Occurred: " + e.getMessage());
                        } catch (IOException e) {
                            logger.error("Error Occurred: " + e.getMessage());
                        }
                    });
                    dataService.updateBatch(fileUploadBatch, recordsProcessed.get(), recordsFailed.get(), recordsExpected.get(), new Date(), "COMPLETED");
                } catch (IOException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }
    }

    public void downloadFiles() {
        try {
            Batch fileDownloadBatch = dataService.startBatch("FILE_DOWNLOAD_BATCH");
            String ftpMainDir = environment.getProperty("app.target.ftp.download.directory");
            AtomicLong recordsProcessed = new AtomicLong(0L);
            AtomicLong recordsExpected = new AtomicLong(0L);
            AtomicLong recordsFailed = new AtomicLong(0L);
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(hostname, Integer.parseInt(port));
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                logger.error("Operation failed. Server reply code: " + replyCode);
                return;
            }
            AtomicBoolean success = new AtomicBoolean(ftpClient.login(username, password));
            showServerReply(ftpClient);
            if (!success.get()) {
                logger.error("Could not login to the server");
                return;
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Path targetLocalDirPath = Paths.get(environment.getProperty("app.staging.download.directory"));
            Files.list(targetLocalDirPath).forEach(localDirectory -> {
                try {
                    String remoteDirName = fileSeperator + ftpMainDir + fileSeperator +localDirectory.getFileName().toString();
                    success.set(ftpClient.makeDirectory(remoteDirName));
                    showServerReply(ftpClient);
                    if (success.get()) {
                        logger.info("Successfully created directory: " + remoteDirName);
                    } else {
                        logger.error("Failed to create directory. See server's reply.");
                    }
                    if(success.get()) {
                        Files.list(localDirectory).forEach(localFile -> {
                            recordsExpected.getAndIncrement();
                            try {
                                InputStream fileInputStream = new FileInputStream(localFile.toAbsolutePath().toString());
                                success.set(ftpClient.storeFile(fileSeperator + remoteDirName + fileSeperator + localFile.getFileName().toString(), fileInputStream));
                                fileInputStream.close();
                                if(success.get()){
                                    recordsProcessed.getAndIncrement();
                                }
                            } catch (FileNotFoundException e) {
                                logger.error("Error Occurred: " + e.getMessage());
                            } catch (IOException e) {
                                logger.error("Error Occurred: " + e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                }
            });
            ftpClient.logout();
            ftpClient.disconnect();
            dataService.updateBatch(fileDownloadBatch, recordsProcessed.get(), recordsExpected.get()-recordsProcessed.get(), recordsExpected.get(), new Date(), "COMPLETED");
        } catch (IOException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }
    }

    private void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                logger.info("SERVER: " + aReply);
            }
        }
    }
}
