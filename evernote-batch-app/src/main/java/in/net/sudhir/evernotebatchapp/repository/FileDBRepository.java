package in.net.sudhir.evernotebatchapp.repository;

import in.net.sudhir.evernotebatchapp.model.FileDB;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.io.File;

/***
 Package Name: in.net.sudhir.evernotebatchapp.repository
 User Name: SUDHIR
 Created Date: 03-09-2022 at 09:00
 Description:
 */
@Repository
public interface FileDBRepository extends CrudRepository<FileDB, Long> {

    public FileDB findFileDBByFileNameAndAndDirectoryName(String fileName, String directoryName);
}
