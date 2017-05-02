export default class OutFrameBuffer {
  constructor() {
    this.bufferSize = 44100 / 40 * 10;
    this.bufferPosition = 0;
    this.buffer = new Buffer();
    this.frameList = [];
    this.onFrameCallback = null;
  }

  reset() {
    this.bufferPosition = 0;
  }

  addFrame(frame) {
    this.frameList.push(frame);
  }

  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
