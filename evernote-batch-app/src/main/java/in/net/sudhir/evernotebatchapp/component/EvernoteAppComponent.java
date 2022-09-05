package in.net.sudhir.evernotebatchapp.component;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.*;
import com.evernote.thrift.TException;
import in.net.sudhir.evernotebatchapp.model.*;
import in.net.sudhir.evernotebatchapp.service.DataService;
import in.net.sudhir.evernotebatchapp.service.EmailSvc;
import in.net.sudhir.evernotebatchapp.service.EvernoteSvc;
import in.net.sudhir.evernotebatchapp.service.FileXferSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/***
 Package Name: in.net.sudhir.evernotebatchapp.component
 User Name: SUDHIR
 Created Date: 03-09-2022 at 11:24
 Description:
 */
@Component
public class EvernoteAppComponent {

    @Autowired
    private Environment environment;
    @Autowired
    private EvernoteSvc evernoteSvc;

    @Autowired
    private DataService dataService;

    @Autowired
    private EmailSvc emailSvc;

    @Autowired
    private FileXferSvc fileXferSvc;

    private static final Logger logger = LoggerFactory.getLogger(EvernoteAppComponent.class);
    public void populateDatabase() {
        // Load All Notebooks to Database
        AtomicInteger notebookCounter = new AtomicInteger();
        evernoteSvc.getNotebooks().forEach(notebook -> {
            NotebookDB newNotebook = new NotebookDB(notebook.getGuid(), notebook.getName());
            dataService.addNotebookToDB(newNotebook);
            logger.info("Notebooks Added to DB: " + notebookCounter.incrementAndGet());
        });
        AtomicInteger tagCounter = new AtomicInteger();
        evernoteSvc.getTags().forEach(tag -> {
            TagDB newTag = new TagDB(tag.getGuid(), tag.getName());
            dataService.addTagToDB(newTag);
            logger.info("Tags Added to DB: " + tagCounter.incrementAndGet());
        });
        AtomicInteger totalNoteCount = new AtomicInteger();
        evernoteSvc.getNotebooks().forEach(notebook -> {
            logger.info("Current Notebook - " + notebook.getName());
            int offset = 0;
            int pageSize = 50;
            int noteCount = 0;
            NoteFilter filter = new NoteFilter();
            NoteList notes = null;
            filter.setNotebookGuid(notebook.getGuid());
            offset = 0;
            noteCount = 0;
            do{
                try {
                    notes = evernoteSvc.getNoteStore().findNotes(filter, offset, pageSize);
                    notes.getNotesIterator().forEachRemaining(note -> {
                        NoteDB newNote = new NoteDB();
                        newNote.setNoteGuid(note.getGuid());
                        newNote.setNoteName(note.getTitle());
                        newNote.setNotebookGUID(notebook.getGuid());
                        dataService.addNoteToDB(newNote);
                        totalNoteCount.getAndIncrement();
                        logger.info("Notes added to DB " + totalNoteCount.get());
                    });
                    noteCount += notes.getTotalNotes();
                    offset += noteCount;
                } catch (EDAMUserException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (EDAMSystemException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (EDAMNotFoundException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (TException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                }
            }while(notes.getTotalNotes() > offset);
        });
        logger.info("Total Notes: " + totalNoteCount);
    }

    public void evernoteInformation() {
        StringBuilder mailContent = new StringBuilder("");
        mailContent.append("<html><body><table><tr><th>Notebook Name</th><th>Notebook GUID</th></tr>\n");
        evernoteSvc.getNotebooks().forEach(notebook -> {
            mailContent.append("<tr><td>" + notebook.getName() + "</td><td>" + notebook.getGuid() + "</td></tr>");
        });
        mailContent.append("</table><br><br><table><tr><th>Tag Name</th><th>Tag GUID</th></tr>");
        evernoteSvc.getTags().forEach(tag -> {
            mailContent.append("<tr><td>" + tag.getName() + "</td><td>" + tag.getGuid() + "</td></tr>");
        });
        mailContent.append("</table><br><br><table><tr><th>Note Name</th><th>Note GUID</th><th>Notebook Name</th><th>Notebook GUID</th></tr>");
        evernoteSvc.getNotebooks().forEach(notebook -> {
            int offset = 0;
            int pageSize = 50;
            int noteCount = 0;
            NoteFilter filter = new NoteFilter();
            NoteList notes = null;
            filter.setNotebookGuid(notebook.getGuid());
            offset = 0;
            noteCount = 0;
            do{
                try {
                    notes = evernoteSvc.getNoteStore().findNotes(filter, offset, pageSize);
                    notes.getNotesIterator().forEachRemaining(note -> {
                        mailContent.append("<tr><td>" + note.getTitle() + "</td><td>" + note.getGuid() + "</td>" + "<td>" + notebook.getName() + "</td><td>"+ notebook.getGuid() + "</td></tr>");
                    });
                    noteCount += notes.getTotalNotes();
                    offset += noteCount;
                } catch (EDAMUserException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (EDAMSystemException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (EDAMNotFoundException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                } catch (TException e) {
                    logger.error("Error Occurred: " + e.getMessage());
                }
            }while(notes.getTotalNotes() > offset);
        });
        mailContent.append("</table><br><br></body></html>");
        emailSvc.sendEvernoteInformationToEmail(mailContent.toString());
    }

    public void initiateFileLoad() {
        fileXferSvc.loadFiles();
    }

    public void uploadToEvernote() {
        Batch uploadToEvernoteBatch = dataService.startBatch("UPLAOD_TO_EVERNOTE");
        AtomicLong recordsProcessed = new AtomicLong(0L);
        AtomicLong recordsExpected = new AtomicLong(0L);
        AtomicLong recordsFailed = new AtomicLong(0L);
        try{
            Path targetPath = Paths.get(environment.getProperty("app.staging.directory"));
            logger.info("Target Directory: " + targetPath);
            if(Files.exists(targetPath)){
                Files.list(targetPath).forEach(directory -> {
                    logger.info("Currently processing directory: " + directory.getFileName());
                    if(Files.isDirectory(directory)){
                        Path subDir = Paths.get(directory.toUri());
                        String notebookGuid = createNotebook(directory.getFileName());
                        try {
                            Files.list(directory).forEach(file -> {
                                logger.info("Currently processing file " + file.getFileName().toString());
                                if(Files.isRegularFile(file)){
                                    recordsExpected.getAndIncrement();
                                    Calendar calendar = Calendar.getInstance();
                                    String pattern = "MM-dd-yyyy";
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                                    String date = simpleDateFormat.format(calendar.getTime());
                                    List<String> defaultTags = Arrays.asList(environment.getProperty("app.default.tags").split(";"));
                                    List<String> tagsToUplaod = new ArrayList<>();
                                    tagsToUplaod.addAll(defaultTags);
                                    tagsToUplaod.add(date);
                                    List<String> tagGuids = createOrGetTagsFromEN(tagsToUplaod);
                                    Note note = new Note();
                                    note.setTagGuids(tagGuids);
                                    note.setNotebookGuid(notebookGuid);
                                    note.setTitle(file.getFileName().toString() + " - Uploaded on - " + date);
                                    String mimeType = null;
                                    try {
                                        mimeType = Files.probeContentType(file);
                                    } catch (IOException e) {
                                        logger.error("Error Occurred: " + e.getMessage());
                                    }
                                    Resource resource = new Resource();
                                    resource.setData(readFileAsData(file));
                                    resource.setMime(mimeType);
                                    ResourceAttributes attributes = new ResourceAttributes();
                                    attributes.setFileName(file.getFileName().toString());
                                    resource.setAttributes(attributes);
                                    note.addToResources(resource);
                                    String hashHex = bytesToHex(resource.getData().getBodyHash());
                                    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                            + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                                            + "<en-note>"
                                            + "<span style=\"color:green;\">This note is uploaded with batch process - </span><br/>"
                                            + "<en-media type=\"" + mimeType + "\" hash=\"" + hashHex + "\"/>"
                                            + "</en-note>";
                                    note.setContent(content);
                                    try {
                                        Note newNote = evernoteSvc.getNoteStore().createNote(note);
                                        NoteDB newNoteDB = new NoteDB();
                                        newNoteDB.setNoteGuid(newNote.getGuid());
                                        newNoteDB.setNoteName(newNote.getTitle());
                                        newNoteDB.setNotebookGUID(newNote.getNotebookGuid());
                                        FileDB fileDB = dataService.getFileDBFromDB(file.getFileName().toString(), directory.getFileName().toString());
                                        fileDB.setIsUploaded(true);
                                        String noteUrl = environment.getProperty("evernote.remote.urlpattern");
                                        noteUrl = noteUrl.replace("[url]", environment.getProperty("evernote.remote." + environment.getProperty("evernote.environment") + ".url"));
                                        noteUrl = noteUrl.replace("[shardId]", evernoteSvc.getShardId());
                                        noteUrl = noteUrl.replace("[userId]", evernoteSvc.getUserId());
                                        noteUrl = noteUrl.replace("[noteGuid]", newNote.getGuid());
                                        fileDB.setNoteUrl(noteUrl);
                                        dataService.updateFileDB(fileDB);
                                        dataService.addNoteToDB(newNoteDB);
                                        logger.info("Note added to Evernote" + newNote.getTitle());
                                        recordsProcessed.getAndIncrement();
                                    } catch (EDAMUserException e) {
                                        logger.error("Error Occurred: " + e.getMessage());
                                    } catch (EDAMSystemException e) {
                                        logger.error("Error Occurred: " + e.getMessage());
                                    } catch (EDAMNotFoundException e) {
                                        logger.error("Error Occurred: " + e.getMessage());
                                    } catch (TException e) {
                                        logger.error("Error Occurred: " + e.getMessage());
                                    }
                                }
                            });
                        } catch (IOException e) {
                            logger.error("Error Occurred: " + e.getMessage());
                        }
                    }
                });
            }else{
                throw new Exception("Target Directory Does not exist.");
            }
        }catch(Exception e){
            logger.error("Exception Occurred: " + e.getMessage());
            e.printStackTrace();
        }
        recordsFailed.set(recordsExpected.get() - recordsProcessed.get());
        dataService.updateBatch(uploadToEvernoteBatch, recordsProcessed.get(), recordsFailed.get(), recordsExpected.get(), new Date(), "COMPLETED");
        logger.info("Batch Process complete.");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
        }
        return sb.toString();
    }

    private Data readFileAsData(Path file) {
        InputStream in = null;
        Data data = null;
        try {
            in = Files.newInputStream(file);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] block = new byte[10240];
            int len;
            while ((len = in.read(block)) >= 0) {
                byteOut.write(block, 0, len);
            }
            in.close();
            byte[] body = byteOut.toByteArray();

            // Create a new Data object to contain the file contents
            data = new Data();
            data.setSize(body.length);
            data.setBodyHash(MessageDigest.getInstance("MD5").digest(body));
            data.setBody(body);
        } catch (IOException e) {
            logger.error("Error Occurred: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error Occurred: " + e.getMessage());
        }
        return data;
    }

    private List<String> createOrGetTagsFromEN(List<String> tagNamesList) {
        List<String> tagsList = new ArrayList<>();
        evernoteSvc.populateCollections();
        tagNamesList.forEach(tagName -> {
            if(!evernoteSvc.getTagNames().contains(tagName)){
                Tag newTag = evernoteSvc.addTag(tagName);
                TagDB newTagDB = new TagDB(newTag.getGuid(), newTag.getName());
                dataService.addTagToDB(newTagDB);
                tagsList.add(newTag.getGuid());
                logger.info("New Tag Created: " + newTag.getName());
            }else{
                logger.info("Tag Already Exists: " + tagName);
                tagsList.add(evernoteSvc.getTag(tagName).getGuid());
            }
        });
        return tagsList;
    }


    private String createNotebook(Path dirName) {
        evernoteSvc.populateCollections();
        if(!evernoteSvc.getNotebookNames().contains(dirName.toString())){
            Notebook newNotebook = evernoteSvc.addNotebook(dirName.toString());
            NotebookDB newNotebookDB = new NotebookDB(newNotebook.getGuid(), newNotebook.getName());
            dataService.addNotebookToDB(newNotebookDB);
            logger.info("New Notebook Created " + newNotebook.getName());
            return newNotebook.getGuid();
        }else{
            logger.info("Notebook Already Exists " + dirName.toString());
            return evernoteSvc.getNotebookGuid(dirName.toString());
        }
    }


}
