export default class InFrameBuffer {
  constructor() {
    this.arrayBuffer = new Int16Array();
    this.frameList = [];
    this.onFrameCallback = null;
  }

  reset() {
    this.arrayBuffer = null;
  }

  addFrame(frame) {
    this.frameList.push(frame);
  }

  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
