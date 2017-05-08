package cn.jackq.messenger.network;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

import cn.jackq.messenger.network.protocol.PeerData;
import cn.jackq.messenger.network.protocol.PeerProtocol;

/**
 * Created on: 4/24/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class PeerTransmission implements Runnable {
    private static final String TAG = "PeerTransmission";
    private String sessionId;
    private String connectId;

    public interface PeerTransmissionListener {

        void onPeerAudioFrameReceived(ByteBuffer buffer);


        void onPeerTransmissionError();

        void onPeerAddressReceived();

    }

    public interface PeerConnectionCallback {

        void finish(String errorMessage);

    }

    private int localPort;

    private DatagramSocket socket;

    private Thread thread;
    private final Object runLock = new Object();
    private boolean running = false;

    private PeerTransmissionListener listener;
    private PeerConnectionCallback createCallback;

    private InetAddress peerAddr;
    private int peerPort;

    private InetAddress serverAddr;
    private int serverPort;

    public PeerTransmission(PeerTransmissionListener listener) {
        this.listener = listener;
    }

    public void create(InetAddress serverAddr, int serverPort, String connectId, String sessionId, PeerConnectionCallback createCallback) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;

        this.connectId = connectId;
        this.sessionId = sessionId;

        this.createCallback = createCallback;

        synchronized (runLock) {
            running = true;
        }
        Log.d(TAG, "create: start new thread");
        thread = new Thread(this);
        thread.start();
    }

    public void sendPacket(InetAddress address, int port, byte[] payload, int offset, int length) {
        if (address == null)
            return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(() -> sendPacket(address, port, payload, offset, length));
            return;
        }
        DatagramPacket packet = new DatagramPacket(payload, length);
        packet.setAddress(address);
        packet.setPort(port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            socket = new DatagramSocket();

            this.localPort = socket.getPort();

            ByteBuffer byteBuffer = PeerProtocol.packServerAddr(this.sessionId, this.connectId);

            this.sendPacket(serverAddr, serverPort, byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());

            if (createCallback != null)
                createCallback.finish(null);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG, "run: unable to create UDP socket");
            synchronized (runLock) {
                running = false;
            }
            if (createCallback != null)
                createCallback.finish(e.getMessage());
            return;
        }

        byte[] receiveBuffer = new byte[10240];

        while (running) {
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                socket.receive(receivePacket);
                this.processPacket(receivePacket.getAddress(), receivePacket.getPort(), receivePacket.getData(), receivePacket.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "run: UDP receiving thread closed");
    }

    private void processPacket(InetAddress address, int port, byte[] data, int length) {

        PeerProtocol.PacketType packetType = PeerProtocol.unpackPacketType(ByteBuffer.wrap(data, 0, length));

        if (packetType == null || packetType == PeerProtocol.PacketType.U_SRV_ADDR) {
            Log.d(TAG, "processPacket: unrecognized packet received");
            return;
        }

        switch (packetType) {
            case U_SYN:
                if (checkSessionId(data, length)) {
                    updatePeerAddr(address, port);
                    // syn from peer, send ack back
                    ByteBuffer peerAck = PeerProtocol.packPeerAck(sessionId);
                    sendPacket(this.peerAddr, this.peerPort, peerAck.array(), peerAck.position(), peerAck.limit() - peerAck.position());
                    ;
                }
                break;
            case U_ACK:
                if (checkSessionId(data, length)) {
                    updatePeerAddr(address, port);
                    Log.d(TAG, "processPacket: ack packet received");
                }
                break;
            case U_DAT:
                PeerData peerData = PeerProtocol.unpackPeerData(ByteBuffer.wrap(data, 0, length));
                if (peerData != null && Objects.equals(peerData.getSessionId(), sessionId)) {
                    switch (peerData.getType()) {
                        case AUDIO:
                            listener.onPeerAudioFrameReceived(peerData.getBuffer());
                            break;
                    }
                }
                break;
            case U_END:
                Log.d(TAG, "processPacket: end transmission received");
                break;
        }
    }

    private boolean checkSessionId(byte[] data, int length) {
        String sessionId = PeerProtocol.unpackSessionId(ByteBuffer.wrap(data, 0, length));
        return Objects.equals(sessionId, this.sessionId);
    }

    private void updatePeerAddr(InetAddress address, int port) {
        if (this.peerAddr == null && address != null) {
            this.peerAddr = address;
            this.peerPort = port;
            this.listener.onPeerAddressReceived();
        }
    }

    public void sendSynToPeer(String address, int port) {
        ByteBuffer byteBuffer = PeerProtocol.packPeerSync(this.sessionId);
        try {
            InetAddress name = InetAddress.getByName(address);
            this.sendPacket(name, port, byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendAudioToPeer(ByteBuffer audioFrame) {
        PeerData data = new PeerData(this.sessionId, PeerData.DataType.AUDIO, audioFrame);
        ByteBuffer byteBuffer = PeerProtocol.packPeerData(data);
        this.sendPacket(peerAddr, peerPort, byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
    }
}
