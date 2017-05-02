export default class Recorder {

  constructor(audioContext) {
    this.recording = false;
    this.onDataCallback = null;

    this.stream = null;
    this.buffer = null;
    this.sampleRate = 8000;
    this.frameFrequency = 20;
    this.audioContext = audioContext;
  }

  init() {
    navigator.mediaDevices.getUserMedia({
      audio: { sampleRate: 8000 },
    }).then((stream) => {
      console.log(stream);
      this.stream = stream;
      const gainNode = this.audioContext.createGain();
      const recorder = this.audioContext.createScriptProcessor(
        2048, 1, 1); // this.sampleRate / this.frameFrequency
      recorder.onaudioprocess = (e) => {
        const data = e.inputBuffer.getChannelData(0);
        if (this.recording && this.onDataCallback) { this.onDataCallback(data); }
      };

      this.audioContext.createMediaStreamSource(stream).connect(gainNode);
      gainNode.connect(recorder);
      recorder.connect(this.audioContext.destination);
    });
  }

  start() {
    this.recording = true;
  }

  stop() {
    this.recording = false;
    this.onDataCallback = null;
  }

  onData(callback) {
    this.onDataCallback = callback;
  }
}
