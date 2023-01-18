package in.net.sudhir.evernotebatchapp;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import in.net.sudhir.evernotebatchapp.component.EvernoteAppComponent;
import in.net.sudhir.evernotebatchapp.config.EvernoteBatchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@RefreshScope
@EnableEncryptableProperties
public class EvernoteBatchAppApplication {

    @Autowired
    Environment environment;

    @Autowired
    private EvernoteAppComponent evernoteAppController;

    private static final Logger logger = LoggerFactory.getLogger(EvernoteBatchAppApplication.class);

    private EvernoteBatchCommand command;
    private String notebookName;
    private String noteGUID;

    private static final String populateDataFromEvernoteSwitch= "app.operations.populatedatafromevernote";
    private static final String startFileLoadFtp= "app.operations.startfileloadftp";
    private static final String evernoteInformation= "app.operations.evernoteinformation";
    private static final String evernoteUploadTask= "app.operations.evernoteuploadtask";
    private static final String startFtpDownloadTask= "app.operations.startftpdownloadtask";
    private static final String evernoteDownloadTask= "app.operations.evernotedownloadtask";
    private static final String evernoteDownloadNoteTask= "app.operations.evernotedownloadnotetask";
    private static final String evernoteDeleteTask= "app.operations.evernotedeletetask";

    public static void main(String[] args) {
        SpringApplication.run(EvernoteBatchAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            if(validateArgs(args)){
                if(command == EvernoteBatchCommand.POPULATE_DATA_FROM_EVERNOTE){
                    if(Boolean.parseBoolean(environment.getProperty(populateDataFromEvernoteSwitch)))
                        evernoteAppController.populateDatabase();
                    else
                        logger.error("Operation turned off.");
                }else if(command == EvernoteBatchCommand.EVERNOTE_INFORMATION){
                    if(Boolean.parseBoolean(environment.getProperty(evernoteInformation)))
                        evernoteAppController.evernoteInformation();
                    else
                        logger.error("Operation turned off.");
                }else if(command == EvernoteBatchCommand.START_FILE_LOAD_FTP){
                    if(Boolean.parseBoolean(environment.getProperty(startFileLoadFtp)))
                        evernoteAppController.initiateFileLoad();
                    else
                        logger.error("Operation turned off.");
                }else if(command == EvernoteBatchCommand.EVERNOTE_UPLOAD_TASK){
                    if(Boolean.parseBoolean(environment.getProperty(evernoteUploadTask)))
                        evernoteAppController.uploadToEvernote();
                    else
                        logger.error("Operation turned off.");
                }else if(command == EvernoteBatchCommand.START_FTP_DOWNLOAD_TASK){
                    if(Boolean.parseBoolean(environment.getProperty(startFtpDownloadTask)))
                        evernoteAppController.downloadToFTPLocation();
                    else
                        logger.error("Operation turned off.");
                }else if(command == EvernoteBatchCommand.EVERNOTE_DOWNLOAD_TASK){
                    if(Boolean.parseBoolean(environment.getProperty(evernoteDownloadTask))){
                        evernoteAppController.downloadFromEvernote(notebookName);
                    }
                    else
                        logger.error("Operation turned off.");
                } else if(command == EvernoteBatchCommand.EVERNOTE_DOWNLOAD_NOTE_TASK){
                    if(Boolean.parseBoolean(environment.getProperty(evernoteDownloadNoteTask))){
                        evernoteAppController.downloadNoteFromEvernote(noteGUID);
                    }
                    else
                        logger.error("Operation turned off.");
                } else if(command == EvernoteBatchCommand.EVERNOTE_DELETE_TASK){
                    if(Boolean.parseBoolean(environment.getProperty(evernoteDeleteTask))) {
                        evernoteAppController.deleteFromEvernote(notebookName);
                    }
                    else
                        logger.error("Operation turned off.");
                }
            }
        };
    }

    private boolean validateArgs(String[] args) {
        if(args.length > 0){
            command = EvernoteBatchCommand.valueOf(args[0]);
            if(command != null){
                if((command == EvernoteBatchCommand.EVERNOTE_DELETE_TASK) || (command == EvernoteBatchCommand.EVERNOTE_DOWNLOAD_TASK)){
                    notebookName = args[1];
                }else if((command == EvernoteBatchCommand.EVERNOTE_DOWNLOAD_NOTE_TASK)){
                    noteGUID = args[1];
                }
                return true;
            }
        }
        return false;
    }


}
