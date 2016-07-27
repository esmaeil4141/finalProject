package io.sharif.pavilion.model.contents;

import java.util.ArrayList;

/**
 * Created by EsiJOOn on 2016/07/19.
 */
public class SubjectObj {// element of ContentsObj that create subViews
    String title;
    String text;
    FileObj fileObj;

    ArrayList<SubjectObj> subjectObjsList =new ArrayList<>();

    public SubjectObj(String title, String text, FileObj fileObj, ArrayList<SubjectObj> subjectObjsList) {
        this.title = title;
        this.text=text;
        this.fileObj=fileObj;
        this.subjectObjsList = subjectObjsList;
    }
    public SubjectObj(String title){
        this.title=title;
    }
    public SubjectObj(String title, String text){
        this.title=title;
        this.text=text;
    }
    public SubjectObj(String title, String text, FileObj fileObj){
        this.title=title;
        this.text=text;
        this.fileObj=fileObj;
    }

    public void addSubject(SubjectObj subjectObj){
        this.subjectObjsList.add(subjectObj);
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public FileObj getFileObj() {
        return fileObj;
    }

    public ArrayList<SubjectObj> getSubjectObjsList() {
        return subjectObjsList;
    }
    public boolean deleteInnerSubject(SubjectObj subjectObj){
        return this.subjectObjsList.remove(subjectObj);
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFileObj(FileObj fileObj) {
        this.fileObj = fileObj;
    }
}
