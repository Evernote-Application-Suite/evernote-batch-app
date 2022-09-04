package in.net.sudhir.evernotebatchapp.service;

import in.net.sudhir.evernotebatchapp.model.NoteDB;
import in.net.sudhir.evernotebatchapp.model.NotebookDB;
import in.net.sudhir.evernotebatchapp.model.TagDB;
import in.net.sudhir.evernotebatchapp.repository.NotbookDBRepository;
import in.net.sudhir.evernotebatchapp.repository.NoteDBRepository;
import in.net.sudhir.evernotebatchapp.repository.TagDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/***
 Package Name: in.net.sudhir.evernotebatchapp.service
 User Name: SUDHIR
 Created Date: 03-09-2022 at 08:02
 Description:
 */
@Service
public class DataService {

    @Autowired
    private NotbookDBRepository notebookDBRepository;

    @Autowired
    private TagDBRepository tagDBRepository;

    @Autowired
    private NoteDBRepository noteDBRepository;

    private Iterable<NotebookDB> notebooksFromDB;
    private Iterable<TagDB> tagsFromDB;
    private Iterable<NoteDB> notesFromDB;


    public DataService() {

    }

    @PostConstruct
    public void postConstruct(){
        notebooksFromDB = notebookDBRepository.findAll();
        tagsFromDB = tagDBRepository.findAll();
        notesFromDB = noteDBRepository.findAll();
    }
    public void addNotebookToDB(NotebookDB newNotebook) {
        boolean found = false;
        if(notebooksFromDB != null){
            for(NotebookDB notebook: notebooksFromDB){
                if(notebook.getNotebookGuid().equalsIgnoreCase(newNotebook.getNotebookGuid())){
                    found = true;
                    break;
                }
            }
        }
        if(!found)
            notebookDBRepository.save(newNotebook);
    }

    public void addTagToDB(TagDB newTag) {
        boolean found = false;
        if(tagsFromDB != null){
            for(TagDB tag: tagsFromDB){
                if(tag.getTagGuid().equalsIgnoreCase(newTag.getTagGuid())){
                    found = true;
                    break;
                }
            }
        }
        if(!found)
            tagDBRepository.save(newTag);
    }

    public TagDB getTagFromDB(String tagGUID) {
        return tagDBRepository.findTagDBByTagGuid(tagGUID);
    }

    public void addNoteToDB(NoteDB newNote) {
        boolean found = false;
        if(notesFromDB != null){
            for(NoteDB note: notesFromDB){
                if(note.getNoteGuid().equalsIgnoreCase(newNote.getNoteGuid())){
                    found = true;
                    break;
                }
            }
        }
        if(!found)
            noteDBRepository.save(newNote);
    }

    public void truncateTables() {
        noteDBRepository.deleteAll();
        notebookDBRepository.deleteAll();
        tagDBRepository.deleteAll();
    }
}
