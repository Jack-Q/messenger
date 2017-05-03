const expectedBuffer = 150 / 1000; // expected buffer in millisecond

export default class Player {
  constructor(audioContext, sampleRate = 44100) {
    this.playing = false;

    this.audioContext = audioContext;
    this.sampleRate = sampleRate;
    this.scheduleTime = 0;
    this.currentIndex = -10;
  }

  addData(data, index) {
    if (!this.playing) { return; }
    const duration = data.length / this.sampleRate;
    const currentTime = this.audioContext.currentTime;
    if (index - this.currentIndex > 1) {
      const bufferTime = currentTime > this.scheduleTime ?
        currentTime + expectedBuffer : this.scheduleTime;
      this.addToContext(data, bufferTime, duration);
      this.currentIndex = index;
      this.scheduleTime = bufferTime + duration;
    } else if (index - this.currentIndex === 1) {
      // buffer adjustment
      const idealPosition = currentTime + expectedBuffer;
      this.scheduleTime += 0.125 * (idealPosition - this.scheduleTime);

      this.addToContext(data, this.scheduleTime, duration);
      this.currentIndex = index;
      this.scheduleTime = this.scheduleTime + duration;
    }
  }

  addToContext(data, schedule, duration) {
    console.log(`schedule frame ${schedule}@${schedule - this.audioContext.currentTime}`);
    this.bufferSource = this.audioContext.createBufferSource();
    this.bufferSource.connect(this.audioContext.destination);
    this.bufferSource.buffer = this.audioContext.createBuffer(1, data.length, this.sampleRate);
    this.bufferSource.buffer.copyToChannel(data, 0);
    this.bufferSource.start(schedule, 0, duration);
  }

  start() {
    this.scheduleTime = 0;
    this.currentIndex = -10;
    this.playing = true;
  }

  stop() {
    this.scheduleTime = 0;
    this.currentIndex = -10;
    this.playing = false;
  }
}
