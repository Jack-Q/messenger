import Recorder from './recorder';
import Player from './player';

export default class Audio {
  constructor() {
    this.audioContext = new AudioContext();
    this.recorder = new Recorder(this.audioContext);
    this.player = new Player(this.audioContext);
    this.recorder.init();
    this.recorder.onData(data => this.player.addData(data));
  }
}
