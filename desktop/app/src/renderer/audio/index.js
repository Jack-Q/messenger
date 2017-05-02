import InFrameBuffer from './in-frame-buffer';
import OutFrameBuffer from './out-frame-buffer';
import AudioPacker from './audio-packer';
import OpusCodec from './opus-codec';
import PcmCodec from './pcm-codec';
import Recorder from './recorder';
import Player from './player';

export default class Audio {
  constructor() {
    // state
    this.recording = false;
    this.audioContext = new AudioContext();

    // declare component
    this.recorder = new Recorder(this.audioContext);
    this.player = new Player(this.audioContext);
    this.inFrameBuffer = new InFrameBuffer();
    this.outFrameBuffer = new OutFrameBuffer();
    this.opusCodec = new OpusCodec();
    this.sendFrameCallback = null;

    // init
    this.recorder.init();

    // bind items
    this.recorder.onData(data => this.onRecordFrame(data));
    this.inFrameBuffer.onFrame(frame => this.onSendFrame(frame));
    this.outFrameBuffer.onFrame(frame => this.onPlayFrame(frame));
  }

  startSession() {
    this.recording = true;
    this.inFrameBuffer.reset();
    this.outFrameBuffer.reset();
    this.recorder.start();
    this.player.start();
  }

  endSession() {
    this.recording = false;
    this.recorder.stop();
    this.player.stop();
  }

  receivePacket(packet) {
    const frameData = AudioPacker.unpack(packet);
    const pcmFrame = this.opusCodec.decode(frameData.frame);
    this.outFrameBuffer.addFrame({ index: frameData.index, frame: pcmFrame });
  }

  onPlayFrame(pcmFrame) {
    const floatPcmFrame = this.pcmCodec.decode(pcmFrame);
    this.player.addData(floatPcmFrame);
  }

  onRecordFrame(floatPcmFrame) {
    const pcmFrame = PcmCodec.encode(floatPcmFrame);
    this.inFrameBuffer.addFrame(pcmFrame);
  }

  onSendFrame(pcmFrame) {
    const encodedData = this.opusCodec.encode(pcmFrame.frame);
    const packet = AudioPacker.pack({ index: pcmFrame.index, frame: encodedData });
    if (this.sendFrameCallback) {
      this.sendFrameCallback(packet);
    }
  }

  setOnPacket(callback) {
    this.sendFrameCallback = callback;
  }
}
