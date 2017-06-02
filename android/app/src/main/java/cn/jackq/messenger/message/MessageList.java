package cn.jackq.messenger.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on: 5/7/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

class MessageList {
    private List<Message> list = new ArrayList<>();
    private int unread = 0;

    int getUnread() {
        return this.unread;
    }

    void readAll(){
        if(this.unread > 0){
            this.unread = 0;
        }
    }

    void addMessage(Message message){
        this.list.add(message);
        this.unread++;
    }

    List<Message> getList() {
        return list;
    }
}
