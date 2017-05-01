export default class Player {
  constructor(audioContext) {
    this.audioContext = audioContext;
    this.bufferSource = this.audioContext.createBufferSource();
    this.bufferSource.connect(this.audioContext.destination);
    this.bufferSource.buffer = this.audioContext.createBuffer(1, 2048, 44100);
    this.bufferSource.start();
  }

  addData(data) {
    this.bufferSource.buffer.copyToChannel(data, 1);
  }
}
