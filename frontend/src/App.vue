<template>
  <b-container id="app">
    <div v-if="$store.state.isAuthenticated" id="nav">
      <router-link to="/">Playlists</router-link> |
      <router-link to="/optimizations">Optimizations</router-link> |
      <router-link to="/tracks">Tracks</router-link>
    </div>
    <router-view/>
  </b-container>
</template>

<script>
import { BContainer } from 'bootstrap-vue'

export default {
  name: 'App',
  components: { BContainer }
}

</script>

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
      color: #1DB954;
    }
  }
}
</style>
