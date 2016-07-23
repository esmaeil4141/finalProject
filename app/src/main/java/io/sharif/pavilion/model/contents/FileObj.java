package io.sharif.pavilion.model.contents;

import android.net.Uri;

/**
 * Created by EsiJOOn on 2016/07/19.
 */
public class FileObj {
    String filePath;
    String fileName;
    Uri uri;

    public void setObjForSend(){
        this.filePath=null;
    }

    public FileObj(String filePath, String fileName, Uri uri) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.uri = uri;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public Uri getUri() {
        return uri;
    }
    public String getFullFilePathPlusName(){
        return filePath+"/"+fileName;
    }
}
