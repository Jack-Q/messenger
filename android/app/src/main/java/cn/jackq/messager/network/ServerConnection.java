package cn.jackq.messager.network;

import java8.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

public class ServerConnection {

    public CompletableFuture<NetworkOperationStatus> connectServer() {
        return null;
    }

    public CompletableFuture<NetworkOperationStatus> testServer() {
        FutureTask<NetworkOperationStatus> task = new FutureTask<>(() -> NetworkOperationStatus.OK);
        return CompletableFuture.completedFuture(NetworkOperationStatus.OK);
    }

    private static ServerConnection connection = new ServerConnection();

    private ServerConnection() {
    }

    public static ServerConnection get() {
        return connection;
    }
}
