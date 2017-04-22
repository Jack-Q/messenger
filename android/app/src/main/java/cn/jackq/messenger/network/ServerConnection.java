package cn.jackq.messenger.network;

import android.util.Log;

import java8.util.concurrent.CompletableFuture;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.FutureTask;

public class ServerConnection {
    private static final String TAG = "ServerConnection";
    private Protocol serverProtocol = new Protocol();

    public CompletableFuture<NetworkOperationStatus> connectServer() {
        return null;
    }

    public CompletableFuture<NetworkOperationStatus> testServer() {
        FutureTask<NetworkOperationStatus> task = new FutureTask<>(() -> NetworkOperationStatus.OK);
        return CompletableFuture.supplyAsync(() -> {
            try {
                InetAddress serverAddress = InetAddress.getByName("10.0.6.1");
                Socket socket = new Socket(serverAddress, 12121);

                socket.getOutputStream().write(serverProtocol.packServerTestPackage());

                byte[] readBuffer = new byte[10240];
                int read = 0;
                while (serverProtocol.isPartialPackage(readBuffer, read)) {
                    if (serverProtocol.isFullPackage(readBuffer, read)) {
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
