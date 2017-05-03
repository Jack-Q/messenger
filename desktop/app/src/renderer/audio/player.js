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
    console.log(`prep add data ${index} after ${this.currentIndex}`);
    const duration = data.length / this.sampleRate;
    const currentTime = this.audioContext.currentTime;
    if (index - this.currentIndex > 1) {
      const bufferTime = currentTime > this.scheduleTime ?
        currentTime + 0.15 : this.scheduleTime + 0.03;
      this.addToContext(data, bufferTime, duration);
      this.currentIndex = index;
      this.scheduleTime = bufferTime + duration;
    } else if (index - this.currentIndex === 1) {
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
