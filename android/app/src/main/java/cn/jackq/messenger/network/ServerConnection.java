package cn.jackq.messenger.network;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.FutureTask;

import java.util.concurrent.CompletableFuture;

import cn.jackq.messenger.network.protocol.ServerProtocol;

public class ServerConnection {
    private static final String TAG = "ServerConnection";
    private ServerProtocol serverProtocol = new ServerProtocol();

    public CompletableFuture<NetworkOperationStatus> connectServer() {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<NetworkOperationStatus> testServer() {
        FutureTask<NetworkOperationStatus> task = new FutureTask<>(() -> NetworkOperationStatus.OK);
        return CompletableFuture.supplyAsync(() -> {
            try {
                InetAddress serverAddress = InetAddress.getByName("10.0.6.1");
                Socket socket = new Socket(serverAddress, 12121);

                socket.getOutputStream().write(serverProtocol.packServerTestPacket());

                byte[] readBuffer = new byte[10240];
                int read = 0;
                while (serverProtocol.isPartialPacket(readBuffer, read)) {
                    if (serverProtocol.isFullPacket(readBuffer, read)) {
                        String message = serverProtocol.unpackString(readBuffer);
                        socket.close();
                        return new NetworkOperationStatus(message, true);
                    }
                    read += socket.getInputStream().read(readBuffer);
                }
                socket.close();
                return new NetworkOperationStatus("Invalid response from server", false);
            } catch (UnknownHostException e) {
                Log.d(TAG, "testServer: failed to resolve text");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "Network IO Exception");
                e.printStackTrace();
            }
            return new NetworkOperationStatus("Failed to connect to server", false);
        });
    }

    private static ServerConnection connection = new ServerConnection();

    private ServerConnection() {
    }

    public static ServerConnection get() {
        return connection;
    }
}
