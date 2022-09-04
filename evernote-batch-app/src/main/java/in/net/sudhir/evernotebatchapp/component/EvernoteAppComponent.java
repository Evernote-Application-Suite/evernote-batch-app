package in.net.sudhir.evernotebatchapp.component;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.thrift.TException;
import in.net.sudhir.evernotebatchapp.model.NoteDB;
import in.net.sudhir.evernotebatchapp.model.NotebookDB;
import in.net.sudhir.evernotebatchapp.model.TagDB;
import in.net.sudhir.evernotebatchapp.service.DataService;
import in.net.sudhir.evernotebatchapp.service.EmailSvc;
import in.net.sudhir.evernotebatchapp.service.EvernoteSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

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

    private static final Logger logger = LoggerFactory.getLogger(EvernoteAppComponent.class);
    public void populateDatabase() {
        try {
            // Load All Notebooks to Database
            AtomicInteger notebookCounter = new AtomicInteger();
            evernoteSvc.getNoteStore().listNotebooks().forEach(notebook -> {
                NotebookDB newNotebook = new NotebookDB(notebook.getGuid(), notebook.getName());
                dataService.addNotebookToDB(newNotebook);
                logger.info("Notebooks Added to DB: " + notebookCounter.incrementAndGet());
            });
            AtomicInteger tagCounter = new AtomicInteger();
            evernoteSvc.getNoteStore().listTags().forEach(tag -> {
                TagDB newTag = new TagDB(tag.getGuid(), tag.getName());
                dataService.addTagToDB(newTag);
                logger.info("Tags Added to DB: " + tagCounter.incrementAndGet());
            });
            AtomicInteger totalNoteCount = new AtomicInteger();
            evernoteSvc.getNoteStore().listNotebooks().forEach(notebook -> {
                int offset = 0;
                int pageSize = 50;
                int noteCount = 0;
                NoteFilter filter = new NoteFilter();
                NoteList notes;
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
                        throw new RuntimeException(e);
                    } catch (EDAMSystemException e) {
                        throw new RuntimeException(e);
                    } catch (EDAMNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }while(notes.getTotalNotes() > offset);
            });
            logger.info("Total Notes: " + totalNoteCount);
        } catch (EDAMUserException e) {
            throw new RuntimeException(e);
        } catch (EDAMSystemException e) {
            throw new RuntimeException(e);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public void evernoteInformation() {
        try {
            StringBuilder mailContent = new StringBuilder("");
            mailContent.append("<html><body><table><tr><th>Notebook Name</th><th>Notebook GUID</th></tr>\n");
            evernoteSvc.getNoteStore().listNotebooks().forEach(notebook -> {
                mailContent.append("<tr><td>" + notebook.getName() + "</td><td>" + notebook.getGuid() + "</td></tr>");
            });
            mailContent.append("</table><br><br><table><tr><th>Tag Name</th><th>Tag GUID</th></tr>");
            evernoteSvc.getNoteStore().listTags().forEach(tag -> {
                mailContent.append("<tr><td>" + tag.getName() + "</td><td>" + tag.getGuid() + "</td></tr>");
            });
            mailContent.append("</table><br><br><table><tr><th>Note Name</th><th>Note GUID</th><th>Notebook Name</th><th>Notebook GUID</th></tr>");
            evernoteSvc.getNoteStore().listNotebooks().forEach(notebook -> {
                int offset = 0;
                int pageSize = 50;
                int noteCount = 0;
                NoteFilter filter = new NoteFilter();
                NoteList notes;
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
                        throw new RuntimeException(e);
                    } catch (EDAMSystemException e) {
                        throw new RuntimeException(e);
                    } catch (EDAMNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }while(notes.getTotalNotes() > offset);
            });
            mailContent.append("</table><br><br></body></html>");
            emailSvc.sendEvernoteInformationToEmail(mailContent.toString());
        } catch (EDAMUserException e) {
            throw new RuntimeException(e);
        } catch (EDAMSystemException e) {
            throw new RuntimeException(e);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }
}
