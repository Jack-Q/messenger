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
        void onConnected(String string);
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
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        this.mConnectionThread = new ServerConnectionThread();
        this.mConnectionThread.start();
    }

    private class ServerConnectionThread extends Thread {

        private byte[] readBuffer = new byte[10240];
        private int posLow = 0;
        private int posHigh = 0;

        @Override
        public void run() {
            try {
                socket = new Socket(serverHost, serverPort);
                while (true) {
                    int read = socket.getInputStream().read(readBuffer, posHigh, readBuffer.length - posHigh);
                    posHigh += read;
                    // parse all of packet in buffer
                    while (ServerProtocol.isPartialPacket(readBuffer, posLow, posHigh - posLow)) {
                        if (ServerProtocol.isFullPacket(readBuffer, posLow, posHigh - posLow)) {
                            this.handlePacket();
                        }
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
                        mListener.onConnected(string);
                    } else {
                        Log.d(TAG, "handlePacket: server status report " + string);
                    }
                    break;
                case USER_ADD_RESP:
                    // user add response

                    break;
                case USER_LOGIN_RESP:
                    break;
                case INFO_RESP:
                    break;
                case MSG_RECV:
                    break;
                case CALL_INIT:
                    break;
                case CALL_ADDR:
                    break;
                case CALL_CONN:
                    break;
                case CALL_END:
                    break;
                default:
                    // for other packet, just skip the process of the packet content
                    // since all of other packet are sent by client
            }
            this.posLow += length;
        }
    }

}
