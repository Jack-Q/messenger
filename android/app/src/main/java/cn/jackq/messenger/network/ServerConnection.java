package cn.jackq.messenger.network;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import cn.jackq.messenger.network.protocol.ServerProtocol;

public class ServerConnection {

    private static final String TAG = "ServerConnection";

    public enum ServerStatus {
        NOT_CONNECT, CONNECTING, CONNECTED, DISCONNECTING
    }

    public interface ServerConnectionListener {
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

    public void connect(String serverHost, int serverPort){
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        this.mConnectionThread = new ServerConnectionThread();
        this.mConnectionThread.start();
    }

    private class ServerConnectionThread extends Thread{

        private byte[] readBuffer = new byte[10240];
        private int posLow = 0;
        private int posHigh = 0;

        @Override
        public void run() {
            try {
                socket = new Socket(serverHost, serverPort);
                while(true){
                    int read = socket.getInputStream().read(readBuffer, posHigh, readBuffer.length - posHigh);
                    posHigh += read;
                    // parse all of packet in buffer
                    while (ServerProtocol.isPartialPacket(readBuffer, posLow, posHigh - posLow)) {
                        if (ServerProtocol.isFullPacket(readBuffer, posLow, posHigh - posLow)) {
                            this.handlePacket();
                        }
                    }

                    if(posLow == posHigh){
                        posLow = posHigh = 0;
                    }

                    if(posLow > 2 * readBuffer.length / 3){
                        System.arraycopy(readBuffer, posLow, readBuffer, 0, posHigh - posLow);
                        posHigh -= posLow;
                        posLow = 0;
                    }

                    if(mStatus == ServerStatus.DISCONNECTING){
                        break;
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handlePacket() {

        }
    }

}
