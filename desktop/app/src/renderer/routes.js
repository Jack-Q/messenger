export default [
  {
    path: '/',
    name: 'main-page',
    component: require('./components/MainPageView'),
  },
  {
    path: '*',
    redirect: '/',
  },
];
