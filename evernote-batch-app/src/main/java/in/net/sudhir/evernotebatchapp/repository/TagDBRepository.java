package in.net.sudhir.evernotebatchapp.repository;

import in.net.sudhir.evernotebatchapp.model.TagDB;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/***
 Package Name: in.net.sudhir.evernotebatchapp.repository
 User Name: SUDHIR
 Created Date: 03-09-2022 at 09:00
 Description:
 */
@Repository
public interface TagDBRepository extends CrudRepository<TagDB, Long> {
    public TagDB findTagDBByTagGuid(String tagGuid);
}
