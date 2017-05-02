export default class Player {
  constructor(audioContext) {
    this.playing = false;

    this.audioContext = audioContext;
    this.counter = 0;
  }

  addData(data) {
    if (!this.playing) { return; }
    if (this.counter++ % 1000 === 0) {
      console.log(data);
    }
    this.bufferSource = this.audioContext.createBufferSource();
    this.bufferSource.connect(this.audioContext.destination);
    this.bufferSource.buffer = this.audioContext.createBuffer(1, 2048, 44100);
    this.bufferSource.buffer.copyToChannel(data, 0);
    this.bufferSource.start();
  }

  start() {
    this.counter = 0;
    this.playing = true;
  }

  stop() {
    this.counter = 0;
    this.playing = false;
  }
}
