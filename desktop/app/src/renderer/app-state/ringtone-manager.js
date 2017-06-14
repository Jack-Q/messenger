import '../resource/ringtone.mp3';

export default class RingtoneManager {
  constructor() {
    // load audio file
    const player = new Audio();
    player.src = '../resource/ringtone.mp3';
    player.loop = true;
    player.autoplay = false;
    player.preload = true;

    this.player = player;
  }

  play() {
    if (this.player) {
      this.player.currentTime = 0;
      this.player.play();
    }
  }
  stop() {
    if (this.player) {
      this.player.pause();
    }
  }
}

