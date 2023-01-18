package in.net.sudhir.evernotebatchapp.config;

/***
 Package Name: in.net.sudhir.evernotebatchapp.config
 User Name: SUDHIR
 Created Date: 17-10-2022 at 16:54
 Description:
 */
public enum EvernoteBatchCommand {


    POPULATE_DATA_FROM_EVERNOTE("POPULATE_DATA_FROM_EVERNOTE"),
    EVERNOTE_INFORMATION("EVERNOTE_INFORMATION"),
    START_FILE_LOAD_FTP("START_FILE_LOAD_FTP"),
    EVERNOTE_UPLOAD_TASK("EVERNOTE_UPLOAD_TASK"),
    START_FTP_DOWNLOAD_TASK("START_FTP_DOWNLOAD_TASK"),
    EVERNOTE_DOWNLOAD_TASK("EVERNOTE_DOWNLOAD_TASK"),
    EVERNOTE_DOWNLOAD_NOTE_TASK("EVERNOTE_DOWNLOAD_NOTE_TASK"),
    EVERNOTE_DELETE_TASK("EVERNOTE_DELETE_TASK")
    ;

    private final String command;

    EvernoteBatchCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}
