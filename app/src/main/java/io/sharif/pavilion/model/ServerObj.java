package io.sharif.pavilion.model;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.sharif.pavilion.model.contents.ContentsObj;
import io.sharif.pavilion.model.contents.UriDeserializer;
import io.sharif.pavilion.model.contents.UriSerializer;
import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.utility.Statics;



public class ServerObj {//this object have all info of one server in clientSide
    String serverId,serverName,description;//description not used
    int iconId;
    ContentsObj contentsObj;
    long lastTime;
    ApInfo apInfo;
    public ServerObj(ApInfo apInfo, ContentsObj contentsObj){
        this.apInfo=apInfo;
        String name=apInfo.getName();
        serverId=apInfo.getBSSID();
        serverName=name.substring(0,name.length()-1);
        iconId= Statics.convertCharacterToNum(name.substring(name.length()-1));
        lastTime=System.currentTimeMillis();
    }

    public ApInfo getApInfo() {
        return apInfo;
    }

    public ServerObj(String serverId, String serverName, String description, int iconId, ContentsObj contentsObj, long lastTime) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.description = description;
        this.iconId = iconId;
        this.contentsObj = contentsObj;
        this.lastTime=lastTime;
    }
    public String getJson(){
//        Gson gson=new Gson();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        return gson.toJson(this
        );
    }
    public static ServerObj getServerOjbFromJson(String json){
//        Gson gson=new Gson();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        return gson.fromJson(json,ServerObj.class);
    }
    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public int getIconId() {
        return iconId;
    }

    public ContentsObj getContentsObj() {
        return contentsObj;
    }

    public String getDescription() {
        return description;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setContentsObj(ContentsObj contentsObj) {
        this.contentsObj = contentsObj;
    }
}
