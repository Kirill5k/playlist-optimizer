<template>
  <div class="home">
    <playlists-view
      v-if="isAuthenticated"
      :playlists="playlists"
    />
    <a v-else href="/api/spotify/login" aria-label="Left Align">
      <font-awesome-icon :icon="spotifyIcon" size="7x"/>
    </a>
  </div>
</template>

<script>
import PlaylistsView from '@/components/PlaylistsView.vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faSpotify } from '@fortawesome/free-brands-svg-icons'

export default {
  name: 'Home',
  components: {
    PlaylistsView,
    FontAwesomeIcon
  },
  created () {
    this.$store.dispatch('getPlaylists')
  },
  computed: {
    spotifyIcon () {
      return faSpotify
    },
    isAuthenticated () {
      return this.$store.state.isAuthenticated
    },
    playlists () {
      return this.$store.state.playlists
    }
  }
}
</script>

<style scoped lang="scss">
  .home {
    display: flex;
    justify-content: center;
    flex-direction: column;
  }
</style>
