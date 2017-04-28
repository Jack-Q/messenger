import Vue from 'vue';
import Electron from 'vue-electron';
import Resource from 'vue-resource';
import Router from 'vue-router';
import KeenUI from 'keen-ui';
import 'keen-ui/dist/keen-ui.css';

import App from './App';
import routes from './routes';

import { createSock } from './server/network';

Vue.use(Electron);
Vue.use(Resource);
Vue.use(Router);
Vue.use(KeenUI);
Vue.config.debug = true;

const router = new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes,
});

/* eslint-disable no-new */
new Vue({
  router,
  ...App,
}).$mount('#app');

createSock('0.0.0.0', 12121, serverSock => {
  console.log('connected', serverSock);
  // serverSock.register()
});
