package com.app.voicechat;

public class Chat {

    private String senderId;
    private String receiverId;
    private String url;
    private String time;
    private String id;

    public Chat(){
        //Varsayılan constructor Firebaseden veri çekmek için gerekir.

    }



    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getUrl() {
        return url;
    }

    public String getTime() {
        return time;
    }
}
