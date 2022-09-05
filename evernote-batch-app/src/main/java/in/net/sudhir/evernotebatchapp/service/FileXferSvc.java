package in.net.sudhir.evernotebatchapp.service;

import in.net.sudhir.evernotebatchapp.component.EvernoteAppComponent;
import in.net.sudhir.evernotebatchapp.model.Batch;
import in.net.sudhir.evernotebatchapp.model.FileDB;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
            Batch fileDownloadBatch = dataService.startBatch("FTP_FILE_DOWNLOAD");
            String ftpMainDir = environment.getProperty("app.target.ftp.directory");
            AtomicLong recordsProcessed = new AtomicLong(0L);
            AtomicLong recordsExpected = new AtomicLong(0L);
            AtomicLong recordsFailed = new AtomicLong(0L);
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(hostname, Integer.parseInt(port));
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile[] directories = ftpClient.listDirectories("/" + ftpMainDir);
            directories = Arrays.copyOfRange(directories, 2, directories.length);
            Arrays.asList(directories).forEach(directory -> {
                try {
                    String targetPath = destination + "/" + directory.getName();
                    Path path = Paths.get(targetPath);
                    if(!Files.exists(path)){
                        Files.createDirectory(path);
                    }
                    FTPFile[] filesInDirectory = ftpClient.listFiles("/" + ftpMainDir + "/" + directory.getName());
                    filesInDirectory = Arrays.copyOfRange(filesInDirectory, 2, filesInDirectory.length);
                    Arrays.asList(filesInDirectory).forEach(file -> {
                        recordsExpected.getAndIncrement();
                        String remoteFile = "/" + ftpMainDir + "/" + directory.getName() + "/" + file.getName();
                        File downloadFile = new File(targetPath + "/" + file.getName());
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
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    dataService.updateBatch(fileDownloadBatch, recordsProcessed.get(), recordsFailed.get(), recordsExpected.get(), new Date(), "COMPLETED");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
