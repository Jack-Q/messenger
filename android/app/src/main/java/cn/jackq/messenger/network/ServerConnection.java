package cn.jackq.messenger.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import cn.jackq.messenger.network.protocol.ServerProtocol;

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
        // void onUserLoginResponse();
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mStatus = ServerStatus.NOT_CONNECT;
        }
    }

    public void sendServerCheck() {
        byte[] bytes = ServerProtocol.packServerTestPacket();
        try {
            Log.d(TAG, "sendServerCheck: sending server check packet to server");
            this.socket.getOutputStream().write(bytes);
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
                socket = new Socket(serverHost, serverPort);
                sendServerCheck();
                mStatus = ServerStatus.CONNECTED;
                while (true) {
                    Log.d(TAG, "run: wait for data from server");
                    int read = socket.getInputStream().read(readBuffer, posHigh, readBuffer.length - posHigh);
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

                    if (mStatus == ServerStatus.DISCONNECTING) {
                        break;
                    }
                }

                Log.d(TAG, "run: finish network thread");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handlePacket() {
            ServerProtocol.PacketType packetType = ServerProtocol.getPacketType(readBuffer, posLow);
            if (packetType == null) {
                Log.d(TAG, "handlePacket: unresolvable packet");
                return;
            }
            int length = ServerProtocol.getPacketSize(readBuffer, posLow);
            switch (packetType) {
                case SERVER_STATUS:
                    // Connected to server
                    String string = ServerProtocol.unpackString(readBuffer, posLow);
                    if (mStatus == ServerStatus.CONNECTING) {
                        Log.d(TAG, "handlePacket: new connection to server with server feedback " + string);
                        mStatus = ServerStatus.CONNECTED;
                        mListener.onServerConnected(string);
                    } else {
                        Log.d(TAG, "handlePacket: server status report " + string);
                    }
                    break;
                case USER_ADD_RESP:
                    Log.d(TAG, "handlePacket: user add response");
                    // user add response

                    break;
                case USER_LOGIN_RESP:
                    Log.d(TAG, "handlePacket: login response");
                    break;
                case INFO_RESP:
                    Log.d(TAG, "handlePacket: information received from server");
                    break;
                case MSG_RECV:
                    Log.d(TAG, "handlePacket: receive message from server");
                    break;
                case CALL_INIT:
                    Log.d(TAG, "handlePacket: call initialized at server");
                    break;
                case CALL_ADDR:
                    Log.d(TAG, "handlePacket: call address acquired from server");
                    break;
                case CALL_CONN:
                    Log.d(TAG, "handlePacket: call connect form server");
                    break;
                case CALL_END:
                    Log.d(TAG, "handlePacket: Call end from server");
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
