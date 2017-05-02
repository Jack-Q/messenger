export default class Player {
  constructor(audioContext) {
    this.audioContext = audioContext;
    this.counter = 0;
  }

  addData(data) {
    this.counter++;
    if (this.counter % 100 === 0) {
      console.log(JSON.stringify(data));
    }
    this.bufferSource = this.audioContext.createBufferSource();
    this.bufferSource.connect(this.audioContext.destination);
    this.bufferSource.buffer = this.audioContext.createBuffer(1, 2048, 44100);
    this.bufferSource.buffer.copyToChannel(data, 0);
    this.bufferSource.start();
  }
}
