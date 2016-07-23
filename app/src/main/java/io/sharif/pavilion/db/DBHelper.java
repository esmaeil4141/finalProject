package io.sharif.pavilion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.model.contents.ContentsObj;

/**
 * Created by EsiJOOn on 2016/07/19.
 */
public class DBHelper {
    private Context context;
    private SQLiteOpenHelper sqLiteOpenHelper;
    final int serverIdIndex=0;
    final int serverNameIndex=1;
    final int iconIdIndex=2;
    final int contentsIndex=3;
    final int descriptionIndex=4;
    final int lastTimeIndex=5;
    public DBHelper(Context context) {
        this.context = context;
        sqLiteOpenHelper = new SQLiteOpenHelper(context, "mydb", null, 1) {//"mybox.db"

            @Override
            public void onCreate(SQLiteDatabase db) {
                String sql1 = "create table if not exists  servers (serverId text ,serverName text,iconId integer default 1,contents text, description text, lastTime long, primary key(serverId , serverName))";
                db.execSQL(sql1);//all the cards from all boxes will be saved in the bax table!
                Log.d("end db oncreateee", "succccccccccc");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };
    }

    public void saveNewServerInfo(ServerObj serverObj){
        String serverName=serverObj.getServerName();
        String serverId=serverObj.getServerId();
        int iconId=serverObj.getIconId();
        String contents=serverObj.getContentsObj().getJson();
        long lastTime=serverObj.getLastTime();

        SQLiteDatabase database = null;
        try {
            ContentValues values = new ContentValues();
            values.put("serverId", serverId);
            values.put("serverName", serverName);
            values.put("iconId", iconId);
            values.put("contents", contents);
            values.put("lastTime", lastTime);


            database = sqLiteOpenHelper.getWritableDatabase();
            database.insert("servers", null, values);

        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
            Toast.makeText(context, "problem in db!", Toast.LENGTH_LONG).show();
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }


    }

    public ArrayList<ServerObj> getAllSavedServers(){
        ArrayList<ServerObj> resultServerObjs=new ArrayList<>();
        SQLiteDatabase database = null;
        try {
            database = sqLiteOpenHelper.getWritableDatabase();
            String query = "select * from servers ";
            Cursor cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String serverId=cursor.getString(serverIdIndex);
                    String serverName=cursor.getString(serverNameIndex);
                    int iconId=cursor.getInt(iconIdIndex);
                    String contents=cursor.getString(contentsIndex);
                    String description=cursor.getString(descriptionIndex);
                    long lastTime=cursor.getLong(lastTimeIndex);

                   ServerObj serverObj=new ServerObj(serverId,serverName,description,iconId, ContentsObj.getContentsOjbFromJson(contents),lastTime);
                   resultServerObjs.add(serverObj);

                } while (cursor.moveToNext());

            }
        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

        return resultServerObjs;
    }

    public ServerObj getSavedServer(String serverId,String serverName){//TODO no need to this method becouse list of servers' title will be maintain from getServersList() method and all data of server will be send from there
        ServerObj serverObj=null;
        SQLiteDatabase database = null;
        try {
            database = sqLiteOpenHelper.getWritableDatabase();
            String query = "select * from servers where serverId="+serverId+" and serverName="+serverName;
            Cursor cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
//                    String serverId=cursor.getString(serverIdIndex);
//                    String serverName=cursor.getString(serverNameIndex);
                    int iconId=cursor.getInt(iconIdIndex);
                    String contents=cursor.getString(contentsIndex);
                    String description=cursor.getString(descriptionIndex);
                    long lastTime=cursor.getLong(lastTimeIndex);

                     serverObj=new ServerObj(serverId,serverName,description,iconId, ContentsObj.getContentsOjbFromJson(contents),lastTime);
            }
        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
        return serverObj;
    }
    public boolean isServerSaved(String serverId,String serverName){
        SQLiteDatabase database = null;
        boolean result=false;
        try {
            database = sqLiteOpenHelper.getWritableDatabase();
            String query = "select * from servers where serverId="+serverId+" and serverName="+serverName;
            Cursor cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                result= true;
            }else{
                result= false;
            }
        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
        return result;
    }

    public int deleteSavedServer(String serverId,String serverName){
        String whereClause = "serverId=? AND serverName=?";
        String[] whereArgs = new String[]{serverId,serverName};
        int noOfDeletedRecords=0;
        SQLiteDatabase database = null;
        try {
            database = sqLiteOpenHelper.getWritableDatabase();
            noOfDeletedRecords=database.delete("servers", whereClause, whereArgs);
        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    return noOfDeletedRecords;
    }

    public void updateSavedServer(ServerObj serverObj){
        String whereClause = "serverId=? AND serverName=?";
        String[] whereArgs = new String[]{serverObj.getServerId(),serverObj.getServerName()};

        SQLiteDatabase database = null;

        try {
            ContentValues values = new ContentValues();
            values.put("serverId", serverObj.getServerId());
            values.put("serverName", serverObj.getServerName());
            values.put("iconId", serverObj.getIconId());
            values.put("contents", serverObj.getContentsObj().getJson());
            values.put("description", serverObj.getDescription());
            values.put("lastTime",serverObj.getLastTime());

            database = sqLiteOpenHelper.getWritableDatabase();
            database.update("servers", values, whereClause, whereArgs);
        } catch (Exception ex) {
            Log.d("Database", "Exception:" + ex.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

    }



}
