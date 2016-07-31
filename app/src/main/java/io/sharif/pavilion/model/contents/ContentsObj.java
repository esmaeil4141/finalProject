package io.sharif.pavilion.model.contents;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by EsiJOOn on 2016/07/19.
 */
public class ContentsObj {//this object has all info of server's contents (title and description of files and ...)
    ArrayList<SubjectObj> subjectObjObjsList =new ArrayList<>();

    public ContentsObj(){
    }
    public void addAbstractSubject(SubjectObj subjectObj){
        this.subjectObjObjsList.add(subjectObj);
    }
    public boolean deleteSubjectObjFromList(SubjectObj subjectObj){
        return this.subjectObjObjsList.remove(subjectObj);
    }
    public String getJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
//        Gson gson=new Gson();
        return gson.toJson(this);
    }
    public static ContentsObj getContentsOjbFromJson(String json){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
//        Gson gson=new Gson();
        return gson.fromJson(json,ContentsObj.class);
    }

    public ArrayList<SubjectObj> getSubjectObjObjsList() {
        return subjectObjObjsList;
    }

    public ArrayList<Uri> getAllUris(){
        ArrayList<Uri> uris=new ArrayList<>();
        for (SubjectObj subjectObj:subjectObjObjsList){
            if(subjectObj.getFileObj()!=null && subjectObj.getFileObj().getUri()!=null){
                uris.add(subjectObj.getFileObj().getUri());
            }
        }
        return uris;
    }
}




