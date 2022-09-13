package in.net.sudhir.evernotebatchapp;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import in.net.sudhir.evernotebatchapp.component.EvernoteAppComponent;
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

    public static void main(String[] args) {
        SpringApplication.run(EvernoteBatchAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("POPULATE_DATA_FROM_EVERNOTE")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.populatedatafromevernote")))
                        evernoteAppController.populateDatabase();
                    else
                        logger.error("Operation turned off.");
                }else if(args[0].equalsIgnoreCase("EVERNOTE_INFORMATION")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.evernoteinformation")))
                        evernoteAppController.evernoteInformation();
                    else
                        logger.error("Operation turned off.");
                }else if(args[0].equalsIgnoreCase("START_FILE_LOAD_FTP")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.startfileloadftp")))
                        evernoteAppController.initiateFileLoad();
                    else
                        logger.error("Operation turned off.");
                }else if(args[0].equalsIgnoreCase("EVERNOTE_UPLOAD_TASK")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.evernoteuploadtask")))
                        evernoteAppController.uploadToEvernote();
                    else
                        logger.error("Operation turned off.");
                }else if(args[0].equalsIgnoreCase("START_FTP_DOWNLOAD_TASK")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.startftpdownloadtask")))
                        evernoteAppController.downloadToFTPLocation();
                    else
                        logger.error("Operation turned off.");
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("EVERNOTE_DOWNLOAD_TASK")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.evernotedownloadtask")))
                        evernoteAppController.downloadFromEvernote(args[1]);
                    else
                        logger.error("Operation turned off.");
                } else if(args[0].equalsIgnoreCase("EVERNOTE_DELETE_TASK")){
                    if(Boolean.parseBoolean(environment.getProperty("app.operations.evernotedeletetask")))
                        evernoteAppController.deleteFromEvernote(args[1]);
                    else
                        logger.error("Operation turned off.");
                }
            }
        };
    }



}
