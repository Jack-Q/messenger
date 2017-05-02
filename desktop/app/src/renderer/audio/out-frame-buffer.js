export default class OutFrameBuffer {
  constructor(sampleRate = 44100, frameRate = 25) {
    this.sampleRate = sampleRate;
    this.frameRate = frameRate;

    this.bufferSize = 44100 / 40 * 10;
    this.bufferPosition = 0;
    this.buffer = new Buffer();
    this.frameList = [];
    this.onFrameCallback = null;
  }

  reset() {
    this.bufferPosition = 0;
  }

  addFrame(frameData) {
    // const index = frameData.index;
    const frame = frameData.frame;
    this.frameList.push(frame);
  }

  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
