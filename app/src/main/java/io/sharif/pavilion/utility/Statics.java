package io.sharif.pavilion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Random;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.model.contents.SubjectObj;

/**
 * Created by EsiJOOn on 2016/07/19.
 */
public class Statics {
    static String dady="0123456789abcdefghijklmnopqrstuvwxyz";

    public static String convertNumToCharacter(int num){
        return dady.charAt(num)+"";
    }
    public static int convertCharacterToNum(String c){
        return dady.indexOf(c);
    }
    public static int getIdOfIcon( int num){
        int[] icons={
                R.drawable.icon0,
                R.drawable.icon1,
                R.drawable.icon2,
                R.drawable.icon3

        };
        return icons[num];
    }
    public static void setSavedLongData(String name, long value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(name, value);
        editor.commit();
    }
    public static long getSavedLongData(String name,long initial,Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(name, initial);
    }
    public static void setSavedStringData(String name, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, value);
        editor.commit();
    }
    public static String getSavedStringData(String name,String initial,Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(name, initial);
    }

    public static void setSavedBooleanData(String name, boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }
    public static boolean getSavedBooleanData(String name,boolean initial,Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(name, initial);
    }
    public static void saveServerContents(ContentsObj contentsObj,Context context){
        setSavedStringData("server_contents",contentsObj.getJson(),context);
    }
    public static ContentsObj getServerContents(Context context){
        String initial;
        String saved=getSavedStringData("server_contents",null,context);
        if(saved!=null) {
            return ContentsObj.getContentsOjbFromJson(saved);
        }else{
            return getFakeSavedServers(context).get(0).getContentsObj();
        }

    }
    public static int getRandomDiologColor(){
        Random random=new Random();
        int rnd= random.nextInt(5)+1;
        switch (rnd){
            case 1:
                return R.color.dialog1;
            case 2:
                return R.color.dialog2;
            case 3:
                return R.color.dialog3;
            case 4:
                return R.color.dialog4;
            case 5:
                return R.color.dialog5;
            case 6:
                return R.color.dialog6;

        }
        return R.color.dialog6;
    }
    public static ArrayList<ServerObj> getFakeSavedServers(Context context){

        SubjectObj smallSubjectObj=new SubjectObj("فایل بروشور","این فایل می توانید تمام فعالیت ها و خدمات ما را ببینید");
        SubjectObj smallSubjectObj2=new SubjectObj("2فایل بروشور","این فایل می توانید تمام فعالیت ها و خدمات ما را ببینید");
//        SubjectObj subjectObj=new SubjectObj("بروشور") ;
//        subjectObj.addSubject(smallSubjectObj);

        SubjectObj subjectObj =new SubjectObj("سوالات متداول");
        SubjectObj q1=new SubjectObj("چگونه کتاب بخرم؟","با مراجعه به غرفه میتوانید کتابهای ما را خریداری کنید",null);
        SubjectObj q2=new SubjectObj("آدرس شما کجاست؟","با مراجعه به غرفه میتوانید کتابهای ما را خریداری کنید",null);
        SubjectObj q3=new SubjectObj("چگونه به صورت اینترنتی کتاب بخرم","با مراجعه به غرفه میتوانید کتابهای ما را خریداری کنید",null);
        subjectObj.addSubject(q1);
        subjectObj.addSubject(q2);
        subjectObj.addSubject(q3);

        ContentsObj contentsObj=new ContentsObj();
        contentsObj.addAbstractSubject(smallSubjectObj);
        contentsObj.addAbstractSubject(smallSubjectObj2);
        contentsObj.addAbstractSubject(subjectObj);

        ArrayList<ServerObj> servers=new ArrayList<>();
        ServerObj s=new ServerObj("123123","ghalamchi","قلمچی بهترین کتابهای کنکور را برای شما آماده میکند",1,contentsObj,System.currentTimeMillis());
        ServerObj s2=new ServerObj("444444","modarresane sharif","همه مدرسان شریف میخوانند....!",2,contentsObj,System.currentTimeMillis());
        ServerObj s3=new ServerObj("55555","mahan","همه مدرسان شریف میخوانند....!",2,contentsObj,System.currentTimeMillis());

        if(Statics.getSavedStringData("server_contents",null,context)!=null) {
            s.setContentsObj(getServerContents(context));
        }
        servers.add(s);
        servers.add(s2);
        servers.add(s3);

        return servers;
    }
    public static  void saveServerName(Context context,String serverName){
        setSavedStringData("serverName",serverName,context);
    }
    public static  String getServerName(Context context){
        return getSavedStringData("serverName","no name",context);
    }

    public  static void setServerState(boolean state,Context context){
        setSavedBooleanData("serverState",state,context);
    }
    public static  boolean getServerState(Context context){
        return getSavedBooleanData("serverState",false,context);
    }
    public  static void setServerIconId(int iconId,Context context){
        setSavedLongData("serverIconId",iconId,context);
    }
    public static int getServerIconId(Context context){
        return (int)getSavedLongData("serverIconId",0,context);
    }


}
