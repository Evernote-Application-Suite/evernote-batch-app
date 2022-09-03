package in.net.sudhir.evernotebatchapp.service;

import in.net.sudhir.evernotebatchapp.model.NoteDB;
import in.net.sudhir.evernotebatchapp.model.NotebookDB;
import in.net.sudhir.evernotebatchapp.model.TagDB;
import in.net.sudhir.evernotebatchapp.repository.NotbookDBRepository;
import in.net.sudhir.evernotebatchapp.repository.NoteDBRepository;
import in.net.sudhir.evernotebatchapp.repository.TagDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

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


    public void addNotebookToDB(NotebookDB newNotebook) {
        notebookDBRepository.save(newNotebook);
    }

    public void addTagToDB(TagDB newTag) {
        tagDBRepository.save(newTag);
    }

    public TagDB getTagFromDB(String tagGUID) {
        return tagDBRepository.findTagDBByTagGuid(tagGUID);
    }

    public void addNoteToDB(NoteDB newNote) {
        noteDBRepository.save(newNote);
    }

    public void truncateTables() {
        noteDBRepository.deleteAll();
        notebookDBRepository.deleteAll();
        tagDBRepository.deleteAll();
    }
}
