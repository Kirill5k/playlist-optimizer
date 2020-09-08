<template>
  <div id="app">
    <div id="nav">
      <router-link to="/">Playlists</router-link> |
      <router-link to="/optimizations">Optimizations</router-link> |
      <router-link to="/tracks">Tracks</router-link>
    </div>
    <router-view/>
  </div>
</template>

<style lang="scss">
@import 'assets/bootstrap-overrides.scss';
@import '~bootstrap/scss/bootstrap.scss';
@import '~bootstrap-vue/src/index.scss';

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

#nav {
  padding: 30px;

  a {
    font-weight: bold;

    &.router-link-exact-active {
      color: #1F9BCF;
    }
  }
}
</style>
