# messenger

> Desktop client for Messenger

## Build Setup


### Prep
Before performing the following build commands, the environment ought to be configured with the following components:
`wine-1.8`, `mono`

```bash
# Basic
sudo apt-get install --no-install-recommends -y icnsutils graphicsmagick xz-utils

# rpm (optional)
sudo apt-get install --no-install-recommends -y rpm

# pacman (Arch based, optional)
sudo apt-get install --no-install-recommends -y bsdtar

# Wine
sudo add-apt-repository ppa:ubuntu-wine/ppa -y
sudo apt-get update
sudo apt-get install --no-install-recommends -y wine1.8

# Mono 
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 3FA7E0328081BFF6A14DA29AA6A19B38D3D831EF
echo "deb http://download.mono-project.com/repo/debian wheezy main" | sudo tee /etc/apt/sources.list.d/mono-xamarin.list
sudo apt-get update
sudo apt-get install --no-install-recommends -y mono-devel ca-certificates-mono
```
Check out document of [Electron Build](https://github.com/electron-userland/electron-builder/wiki/Multi-Platform-Build) for more details)

### build commands

``` bash
# install dependencies
npm install

# serve with hot reload at localhost:9080
npm run dev

# build electron app for production
npm run build

# build for both linux and windows
npm run build:all
# if this step failed due to network issues, a mirror server may solve the problem
ELECTRON_MIRROR="https://npm.taobao.org/mirrors/electron/" npm run build:all

# lint all JS/Vue component files in `app/src`
npm run lint

# run webpack in production
npm run pack
```
More information can be found [here](https://simulatedgreg.gitbooks.io/electron-vue/content/en/npm_scripts.html).

---

This project was generated from [electron-vue](https://github.com/SimulatedGREG/electron-vue) using [vue-cli](https://github.com/vuejs/vue-cli). Documentation about this project can be found [here](https://simulatedgreg.gitbooks.io/electron-vue/content/index.html).
