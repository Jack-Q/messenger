package cn.jackq.messenger.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import cn.jackq.messenger.service.MainService;

/**
 * Created on: 4/24/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class PeerTransmission implements Runnable {
    private static final String TAG = "PeerTransmission";

    public interface PeerTransmissionListener {
        void onPackageReceived(byte[] data, int size);

        void onError();
    }

    public interface PeerConnectionCallback {
        void finish(String errorMessage);
    }

    private int localPort = 42001;
    private DatagramSocket socket;
    private Thread thread;

    private final Object runLock = new Object();
    private boolean running = false;

    private PeerTransmissionListener listener;
    private PeerConnectionCallback createCallback;

    private String peerHost;
    private int peerPort;
    private InetAddress inetAddress;

    public PeerTransmission(PeerTransmissionListener listener) {
        this.listener = listener;
    }

    public void create(String peerHost, int peerPort, PeerConnectionCallback createCallback) {
        this.peerHost = peerHost;
        this.peerPort = peerPort == 0 ? 42001 : peerPort;
        this.createCallback = createCallback;

        synchronized (runLock) {
            running = true;
        }
        Log.d(TAG, "create: start new thread");
        thread = new Thread(this);
        thread.start();
    }

    public void sendPacket(byte[] payload, int length) throws IOException {
        DatagramPacket packet = new DatagramPacket(payload, length);
        socket.send(packet);
    }

    public void terminate() {
        if (thread == null)
            return;

        synchronized (runLock) {
            running = false;
        }
        thread.interrupt();
        try {
            thread.join(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        socket.close();
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, "run: Creating new UDP socket on port " + localPort);
            socket = new DatagramSocket(localPort);

            Log.d(TAG, "run: start UDP transmission");
            inetAddress = InetAddress.getByName(peerHost);


            socket.connect(inetAddress, peerPort);

            if (createCallback != null)
                createCallback.finish(null);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG, "run: unable to create UDP socket");
            synchronized (runLock) {
                running = false;
            }
            if (createCallback != null)
                createCallback.finish(e.getMessage());
            return;
        }

        byte[] receiveBuffer = new byte[1024];

        while (running) {
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (this.listener != null)
                listener.onPackageReceived(receivePacket.getData(), receivePacket.getLength());
        }

        Log.d(TAG, "run: UDP receiving thread closed");
    }
}
