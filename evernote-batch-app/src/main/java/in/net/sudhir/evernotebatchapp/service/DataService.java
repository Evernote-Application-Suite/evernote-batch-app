package in.net.sudhir.evernotebatchapp.service;

import in.net.sudhir.evernotebatchapp.model.*;
import in.net.sudhir.evernotebatchapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

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

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private FileDBRepository fileDBRepository;

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

    public Batch startBatch(String batchName){
        Batch newBatch = new Batch();
        newBatch.setBatchName(batchName);
        newBatch.setRecordsExpected(0L);
        newBatch.setRecordsProcessed(0L);
        newBatch.setRecordsFailed(0L);
        newBatch.setStartDate(new Date());
        newBatch.setStatus("STARTED");
        newBatch = batchRepository.save(newBatch);
        return newBatch;
    }

    public Batch updateBatch(Batch batch, Long recordsProcessed, String status){
        batch.setRecordsProcessed(recordsProcessed);
        batch.setStatus(status);
        batch = batchRepository.save(batch);
        return batch;
    }

    public Batch updateBatch(Batch batch, Long recordsProcessed,Long recordsFailed, String status){
        batch.setRecordsProcessed(recordsProcessed);
        batch.setRecordsFailed(recordsFailed);
        batch.setStatus(status);
        batch = batchRepository.save(batch);
        return batch;
    }

    public Batch updateBatch(Batch batch, Long recordsProcessed,Long recordsFailed, Long recordsExpected, Date endTime, String status){
        batch.setRecordsProcessed(recordsProcessed);
        batch.setRecordsFailed(recordsFailed);
        batch.setRecordsExpected(recordsExpected);
        batch.setStatus(status);
        batch.setEndDate(endTime);
        batch = batchRepository.save(batch);
        return batch;
    }

    public Batch updateBatch(Batch batch, Long recordsProcessed,Long recordsFailed, Long recordsExpected, String status){
        batch.setRecordsProcessed(recordsProcessed);
        batch.setRecordsFailed(recordsFailed);
        batch.setRecordsExpected(recordsExpected);
        batch.setStatus(status);
        batch = batchRepository.save(batch);
        return batch;
    }

    public void addFileToDB(FileDB fileDB) {
        fileDBRepository.save(fileDB);
    }

    public FileDB getFileDBFromDB(String fileName, String directoryName) {
        return fileDBRepository.findFileDBByFileNameAndAndDirectoryName(fileName, directoryName);
    }

    public void updateFileDB(FileDB fileDB) {
        fileDBRepository.save(fileDB);
    }
}
