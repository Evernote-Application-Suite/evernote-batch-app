package in.net.sudhir.evernotebatchapp.service;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/***
 Package Name: in.net.sudhir.evernotebatchapp.service
 User Name: SUDHIR
 Created Date: 03-09-2022 at 08:03
 Description:
 */
@Service
@Getter
@Setter
public class EvernoteSvc {

    @Autowired
    private Environment environment;

    private UserStoreClient userStore;
    private NoteStoreClient noteStore;
    private String shardId;
    private String userId;

    private static final Logger logger = LoggerFactory.getLogger(EvernoteSvc.class);

    public EvernoteSvc(Environment environment) {
        try{
            String env = environment.getProperty("evernote.environment");
            String developerToken = environment.getProperty("evernote.remote." + env.toLowerCase() + ".developertoken");
            EvernoteService evernoteEnv = EvernoteService.valueOf(env);
            EvernoteAuth evernoteAuth = new EvernoteAuth(evernoteEnv, developerToken);
            ClientFactory clientFactory = new ClientFactory(evernoteAuth);
            userStore = clientFactory.createUserStoreClient();
            boolean versionOk = userStore.checkVersion("Evernote App Suite Service(Java)", com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR, com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR );

            if(!versionOk){
                logger.error("Invalid Version identified. Check with Evernote Dev Team.");
                logger.error("Version check failed. Exiting program.");
                System.exit(1);
            }
            noteStore = clientFactory.createNoteStoreClient();
            shardId = userStore.getUser().getShardId();
            userId = String.valueOf(userStore.getUser().getId());
        }catch(Exception e){
            logger.error("Exception Occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
