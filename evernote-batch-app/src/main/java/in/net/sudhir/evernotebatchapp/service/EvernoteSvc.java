package in.net.sudhir.evernotebatchapp.service;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    private List<Notebook> notebooks;
    private List<Tag> tags;

    private List<String> notebookNames;
    private List<String> tagNames;

    private static final Logger logger = LoggerFactory.getLogger(EvernoteSvc.class);

    public EvernoteSvc(Environment environment) {
        try{
            String env = environment.getProperty("evernote.environment").toUpperCase();
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
            populateCollections();
        }catch(Exception e){
            logger.error("Exception Occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void populateCollections() {
        try {
            notebooks = noteStore.listNotebooks();
            tags = noteStore.listTags();
            notebookNames = new ArrayList<>();
            tagNames = new ArrayList<>();
            notebooks.forEach(notebook -> {
                notebookNames.add(notebook.getName());
            });
            tags.forEach(tag -> {
                tagNames.add(tag.getName());
            });
        } catch (EDAMUserException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (EDAMSystemException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (TException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }

    }

    public Notebook addNotebook(String newNotebookName) {
        try {
            Notebook newNotebook = new Notebook();
            newNotebook.setName(newNotebookName);
            newNotebook = this.getNoteStore().createNotebook(newNotebook);
            return newNotebook;
        } catch (EDAMUserException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (EDAMSystemException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (TException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }
        return null;
    }

    public String getNotebookGuid(String notebookName) {
        populateCollections();
        AtomicReference<String> notebookGuid = new AtomicReference<>();
        notebooks.forEach(notebook -> {
            if(notebook.getName().equals(notebookName)){
                notebookGuid.set(notebook.getGuid());
            }
        });
        return notebookGuid.get();
    }

    public String getTagGuid(String tagName) {
        populateCollections();
        AtomicReference<String> tagGuid = null;
        tags.forEach(tag -> {
            if(tag.getName().equals(tagName)){
                tagGuid.set(tag.getGuid());
            }
        });
        return tagGuid.get();
    }

    public Tag addTag(String tagName) {
        try {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            newTag = this.getNoteStore().createTag(newTag);
            return newTag;
        } catch (EDAMUserException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (EDAMSystemException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (TException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (EDAMNotFoundException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }
        return null;
    }

    public Tag getTag(String tagName) {
        populateCollections();
        for (Tag tag : tags) {
            if(tag.getName().equals(tagName)){
                return tag;
            }
        }
        return null;
    }
}
