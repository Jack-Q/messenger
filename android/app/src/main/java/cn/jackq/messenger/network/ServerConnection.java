package cn.jackq.messenger.network;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import cn.jackq.messenger.network.protocol.ServerProtocol;
import cn.jackq.messenger.network.protocol.ServerResponse;
import cn.jackq.messenger.network.protocol.User;

public class ServerConnection {

    private static final String TAG = "ServerConnection";

    public enum ServerStatus {
        NOT_CONNECT, CONNECTING, CONNECTED, DISCONNECTING
    }

    public interface ServerConnectionListener {
        /**
         * invoked at the first time the client is connected to the server
         *
         * @param string the message returned from the server
         */
        void onServerConnected(String string);

        void onUserAddResponse(boolean status, String message);

        void onUserLoginResponse(boolean status, String message, String connectId);

        void onServerUpdateBuddyList(List<User> buddyList);

        void onServerMessageFromUser(String user, String connectId, String message);

        void onServerCallInit(boolean status, String message, String sessionId, String user, String address, int port);

        void onServerCallPeerAddress(boolean status, String message, String connectId, String address, int port);

        void onServerCallConnected(boolean status, String message, String sessionId);

        void onServerCallEnd(boolean status, String message, String sessionId);

        void onServerDisconnected(String message);
    }

    private final ServerConnectionListener mListener;
    private ServerStatus mStatus = ServerStatus.NOT_CONNECT;
    private String serverHost;
    private int serverPort;
    private ServerConnectionThread mConnectionThread;
    private Socket socket;


    public ServerConnection(@NonNull() ServerConnectionListener listener) {
        this.mListener = listener;
    }

    public ServerStatus getStatus() {
        return this.mStatus;
    }

    public void connect(String serverHost, int serverPort) {
        if (this.mStatus == ServerStatus.CONNECTING || this.mStatus == ServerStatus.CONNECTED) {
            Log.d(TAG, "connect: already connected to server, please disconnect first");
            return;
        }
        Log.d(TAG, "connect: request start service from binder");
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        this.mStatus = ServerStatus.CONNECTING;
        this.mConnectionThread = new ServerConnectionThread();
        this.mConnectionThread.start();
    }

    public void disconnect() {
        if (this.mStatus == ServerStatus.CONNECTING || this.mStatus == ServerStatus.CONNECTED) {
            this.mStatus = ServerStatus.DISCONNECTING;
            Log.d(TAG, "disconnect: disconnecting: interrupt at first");
            this.mConnectionThread.interrupt();
            try {
                this.mConnectionThread.join(100);
                Log.d(TAG, "disconnect: joined");
            } catch (InterruptedException e) {
                Log.d(TAG, "disconnect: interrupted");
                e.printStackTrace();
            }
            if(this.socket != null && this.socket.isConnected()){
                try {
                    this.socket.close();
                } catch (IOException err) {
                    err.printStackTrace();
                } finally {
                    this.socket = null;
                }
            }
            this.mStatus = ServerStatus.NOT_CONNECT;
        }
    }

    public void sendServerCheck() {
        send(ServerProtocol.packServerTestPacket());
    }

    public void sendUserLogin(String username, String token) {
        send(ServerProtocol.packLoginReqPacket(username, token));
    }

    public void sendUserAdd(String username, String token) {
        send(ServerProtocol.packUserAddReqPacket(username, token));
    }

    public void sendBuddyListRequest() {
        send(ServerProtocol.packBuddyListQueryPacket());
    }

    public void sendMessageToUser(User user, String message) {
        send(ServerProtocol.packMsgSendPacket(user, message));
    }

    public void sendCallRequest(User user, String connectId) {
        send(ServerProtocol.packCallReqPacket(user, connectId));
    }

    public void sendCallPrepared(String sessionId) {
        send(ServerProtocol.packCallPrepPacket(sessionId));
    }

    public void sendCallAnswer(String sessionId) {
        send(ServerProtocol.packCallAnsPacket(sessionId));
    }

    public void sendCallTerminate(String sessionId) {
        send(ServerProtocol.packCallTermPacket(sessionId));
    }


    private void send(byte[] buffer) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            new Thread(() -> send(buffer)).start();
            return;
        }
        Log.d(TAG, "send: send data to server");
        try {
            this.socket.getOutputStream().write(buffer);
            this.socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerConnectionThread extends Thread {

        private byte[] readBuffer = new byte[10240];
        private int posLow = 0;
        private int posHigh = 0;

        @Override
        public void run() {
            Log.d(TAG, "run: begin server connection thread");
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(serverHost, serverPort);
                socket = new Socket();
                socket.connect(socketAddress, 5000);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: failed to connect to server " + e.getMessage());
                endConnection("failed to connect to server " + e.getMessage());
                return;
            }
            try {
                sendServerCheck();
                while (true) {
                    Log.d(TAG, "run: wait for data from server");
                    int read = socket.getInputStream().read(readBuffer, posHigh, readBuffer.length - posHigh);

                    if (read < 0) {
                        endConnection("Connection error");
                        break;
                    }

                    if(read ==  0){
                        if (mStatus == ServerStatus.DISCONNECTING || mStatus == ServerStatus.NOT_CONNECT) {
                            endConnection("Disconnected");
                            break;
                        }
                    }

                    posHigh += read;
                    Log.d(TAG, "run: receive " + read + " bytes from server");
                    // parse all of packet in buffer
                    while (ServerProtocol.isPartialPacket(readBuffer, posLow, posHigh - posLow)) {
                        if (ServerProtocol.isFullPacket(readBuffer, posLow, posHigh - posLow)) {
                            this.handlePacket();
                            continue;
                        }
                        break;
                    }

                    if (posLow == posHigh) {
                        posLow = posHigh = 0;
                    }

                    if (posLow > 2 * readBuffer.length / 3) {
                        System.arraycopy(readBuffer, posLow, readBuffer, 0, posHigh - posLow);
                        posHigh -= posLow;
                        posLow = 0;
                    }

                }

                Log.d(TAG, "run: finish network thread");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void endConnection(String message) {
            try{
                Log.d(TAG, "endConnection: check and terminate connection");
                if(socket.isConnected()){
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mListener.onServerDisconnected(message);
            socket = null;
            mStatus = ServerStatus.NOT_CONNECT;
        }

        private void handlePacket() {
            ServerProtocol.PacketType packetType = ServerProtocol.getPacketType(readBuffer, posLow);
            if (packetType == null) {
                Log.d(TAG, "handlePacket: unresolvable packet");
                return;
            }
            int length = ServerProtocol.getPacketSize(readBuffer, posLow);
            ServerResponse serverResponse = ServerProtocol.unpackJsonResponse(readBuffer, posLow);
            switch (packetType) {
                case SERVER_STATUS:
                    // Connected to server
                    String string = ServerProtocol.unpackString(readBuffer, posLow);
                    Log.d(TAG, "handlePacket: new connection to server with server feedback " + string);
                    mListener.onServerConnected(string);
                    break;
                case USER_ADD_RESP:
                    Log.d(TAG, "handlePacket: user add response");
                    // user add response
                    if (serverResponse != null) {
                        mListener.onUserAddResponse(serverResponse.isStatus(), serverResponse.getMessage());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case USER_LOGIN_RESP:
                    Log.d(TAG, "handlePacket: login response");
                    if (serverResponse != null) {
                        mListener.onUserLoginResponse(serverResponse.isStatus(), serverResponse.getMessage(), serverResponse.getConnectId());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case INFO_RESP:
                    Log.d(TAG, "handlePacket: information received from server");
                    if (serverResponse != null && serverResponse.isStatus()) {
                        mListener.onServerUpdateBuddyList(serverResponse.getBuddyList());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case MSG_RECV:
                    Log.d(TAG, "handlePacket: receive message from server");
                    if (serverResponse != null) {
                        mListener.onServerMessageFromUser(serverResponse.getUser(), serverResponse.getConnectId(), serverResponse.getMessage());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case CALL_INIT:
                    Log.d(TAG, "handlePacket: call initialized at server");
                    if (serverResponse != null) {
                        mListener.onServerCallInit(serverResponse.isStatus(), serverResponse.getMessage(), serverResponse.getSessionId(), serverResponse.getUser(), serverResponse.getAddress(), serverResponse.getPort());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case CALL_ADDR:
                    Log.d(TAG, "handlePacket: call address acquired from server");
                    if (serverResponse != null) {
                        mListener.onServerCallPeerAddress(serverResponse.isStatus(), serverResponse.getMessage(), serverResponse.getConnectId(), serverResponse.getAddress(), serverResponse.getPort());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case CALL_CONN:
                    Log.d(TAG, "handlePacket: call connect form server");
                    if (serverResponse != null) {
                        mListener.onServerCallConnected(serverResponse.isStatus(), serverResponse.getMessage(), serverResponse.getSessionId());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                case CALL_END:
                    Log.d(TAG, "handlePacket: Call end from server");
                    if (serverResponse != null) {
                        mListener.onServerCallEnd(serverResponse.isStatus(), serverResponse.getMessage(), serverResponse.getSessionId());
                    } else {
                        Log.d(TAG, "handlePacket: no response returned from server");
                    }
                    break;
                default:
                    Log.d(TAG, "handlePacket: unknown packet type received from server" + packetType);
                    // for other packet, just skip the process of the packet content
                    // since all of other packet are sent by client
            }
            this.posLow += length;
        }
    }

}
