package cn.jackq.messenger.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    private Map<String, MessageList> store = new HashMap<>();

    public void addNewMessage(String user, Message message) {
    }

    public List<Message> getMessages(String id) {
        return getMessageList(id).getList();
    }

    public int getUnread(String id) {
        return getMessageList(id).getUnread();
    }

    public void readAll(String id) {
        getMessageList(id).readAll();
    }

    public void addMessage(String id, Message message) {
        getMessageList(id).addMessage(message);
    }

    private MessageList getMessageList(String id) {
        if (!this.store.containsKey(id)) {
            this.store.put(id, new MessageList());
        }
        return this.store.get(id);
    }

    private MessageManager() {
    }

    private static MessageManager list = new MessageManager();

    public static MessageManager get() {
        return list;
    }

}
