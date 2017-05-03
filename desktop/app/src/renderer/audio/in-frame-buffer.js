

export default class InFrameBuffer {
  constructor(sampleRate = 48000, frameRate = 25) {
    // config
    this.frameRate = frameRate;
    this.sampleRate = sampleRate;
    this.onFrameCallback = null;
    // state
    this.frameList = [];
    this.initialPosition = 0;
    this.sampleLength = 0;
    this.frameIndex = 0;
  }

  reset() {
    delete this.frameList;
    this.frameList = [];
    this.initialPosition = 0;
    this.sampleLength = 0;
    this.frameIndex = 0;
  }

  addFrame(frame) {
    console.log('receive buffer');

    this.frameList.push(frame);
    this.sampleLength += frame.length;

    const frameLength = Math.floor(this.sampleRate / this.frameRate);
    if (this.sampleLength >= frameLength) {
      // encode packet
      const buffer = Buffer.allocUnsafe(2 * frameLength);
      let pos = 0; let leftLength = frameLength;
      while (leftLength > 0) {
        const low = this.initialPosition;
        const frame = this.frameList[0];
        let high;
        if (frame.length - low <= leftLength) {
          leftLength = leftLength - frame.length - low;
          high = frame.length;
          this.frameList.shift();
          this.initialPosition = 0;
        } else {
          high = low + leftLength;
          leftLength = 0;
          this.initialPosition = high;
        }

        for (let i = low; i < high; i++) {
          pos = buffer.writeInt16LE(frame[i], pos);
        }
      }
      this.sampleLength -= frameLength;
      this.onFrameCallback({ index: this.frameIndex++, frame: buffer });
    }
  }

  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
