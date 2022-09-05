package in.net.sudhir.evernotebatchapp;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import in.net.sudhir.evernotebatchapp.component.EvernoteAppComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEncryptableProperties
public class EvernoteBatchAppApplication {

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
                    evernoteAppController.populateDatabase();
                }else if(args[0].equalsIgnoreCase("EVERNOTE_INFORMATION")){
                    evernoteAppController.evernoteInformation();
                }else if(args[0].equalsIgnoreCase("START_FILE_LOAD_FTP")){
                    evernoteAppController.initiateFileLoad();
                }else if(args[0].equalsIgnoreCase("EVERNOTE_UPLOAD_TASK")){
                    evernoteAppController.uploadToEvernote();
                }
            }
        };
    }



}
