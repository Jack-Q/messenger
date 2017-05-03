const minimumBufferFrame = 5;

export default class OutFrameBuffer {
  constructor(sampleRate = 44100, frameRate = 25) {
    this.sampleRate = sampleRate;
    this.frameRate = frameRate;

    this.frameList = [];
    this.timer = 0;
    this.current = {
      index: -1,
      length: 0, // expect a constant length for all frame
      finish: 0, // timestamp when the slice will finish
    };
    this.onFrameCallback = null;
  }

  reset() {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = 0;
    }
    this.currentIndex = 0;

    delete this.frameList;
    this.frameList = [];
  }

  // add frame with index to buffer list,
  // if the buffer is considered sufficient,
  // a frame will be fed to audio output
  addFrame(frameData) {
    if (frameData.index <= this.current.index) {
      // discard outdated frame
      return;
    }
    this.frameList.push(frameData);
    this.frameList.sort((a, b) => a.index - b.index);

    if (this.frameList.length < minimumBufferFrame) {
      // wait for more frame
      return;
    }

    this.playNextFrame();
  }

  playNextFrame() {
    const now = Date.now();
    if (now < this.current.finish) {
      // timer pending
      console.log('Skip play');
      return;
    }

    if (this.frameList.length === 0) {
      // no audio frame
      console.log('empty buffer');
      return;
    }

    while (this.frameList[0].index <= this.current.index) {
      console.log(`drop buffer frame with index${this.frameList[0].index}`);
      this.frameList.shift();
    }

    if (this.frameList[0].index === this.current.index + 1
      || this.frameList.length > minimumBufferFrame) {
      // expected frame, send to audio device
      // or, the buffer is sufficient (long lag), skip to next available frame
      const nxtSeq = this.frameList.shift();
      this.current.index = nxtSeq.index;
      this.current.length = Math.floor(nxtSeq.frame.length / this.sampleRate * 1000);
      console.log(`feed frame of ${this.current.length}@${now - this.now}@${now} with id ${nxtSeq.index}`);
      this.onFrameCallback(nxtSeq.frame);
    } else {
      // place a space with the same length as the current one
      console.log(`insert empty frame of ${this.current.length}@${now - this.now}@${now}`);
      this.current.index = this.current.index + 1;
    }

    this.current.finish = this.current.length + now;
    this.timer = setTimeout(this.playNextFrame.bind(this), this.current.length);
    console.log(this.frameList.map(p => `${p.index}, ${p.frame.length}`));
    this.now = now;
  }

  onFrame(callback) {
    this.onFrameCallback = callback;
  }
}
