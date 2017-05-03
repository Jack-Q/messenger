const minimumBufferFrame = 3;

export default class OutFrameBuffer {
  constructor(sampleRate = 44100, frameRate = 25) {
    this.sampleRate = sampleRate;
    this.frameRate = frameRate;

    this.frameList = [];
    this.next = 0;
    this.onFrameCallback = null;
  }

  reset() {
    this.next = 0;

    delete this.frameList;
    this.frameList = [];
  }

  // add frame with index to buffer list,
  // if the buffer is considered sufficient,
  // a frame will be fed to audio output
  addFrame(frameData) {
    if (frameData.index < this.next) {
      // discard outdated frame
      return;
    }
    this.frameList.push(frameData);
    this.frameList.sort((a, b) => a.index - b.index);

    this.sendConsequentialFrames();

    while (this.frameList.length > minimumBufferFrame && this.frameList[0].index > this.next) {
      // wait for more frame
      this.sendConsequentialFrames();
    }
  }
  sendConsequentialFrames() {
    while (this.frameList.length > 0
      && this.frameList[0].index === this.next) {
      const send = this.frameList.shift();
      this.onFrameCallback(send.frame, send.index);
      this.next = send.index + 1;
    }
  }


  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
