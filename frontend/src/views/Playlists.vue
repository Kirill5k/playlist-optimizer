<template>
  <div v-if="isAuthenticated && !isLoading" class="playlists">
    <playlists-view
      :playlists="playlists"
      @optimize="optimizePlaylist"
    />
    <playlist-import
      @import="importPlaylist"
    />
  </div>
  <div v-else-if="isLoading">
    <div class="d-flex justify-content-center mb-3">
      <b-spinner
        class="m-5"
        style="width: 4rem; height: 4rem;"
        variant="dark"
        label="Loading..."
      />
    </div>
  </div>
  <div v-else class="playlists">
    <a href="/api/spotify/login" aria-label="Left Align" class="mt-5">
      <font-awesome-icon :icon="spotifyIcon" size="9x"/>
    </a>
  </div>
</template>

<script>
import PlaylistsView from '@/components/PlaylistsView'
import PlaylistImport from '@/components/PlaylistImport'
import NotificationsMixin from '@/mixins/NotificationsMixin'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faSpotify } from '@fortawesome/free-brands-svg-icons'
import { BSpinner } from 'bootstrap-vue'

export default {
  name: 'Playlists',
  components: {
    PlaylistsView,
    PlaylistImport,
    FontAwesomeIcon,
    BSpinner
  },
  mixins: [NotificationsMixin],
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
    isLoading () {
      return this.$store.state.isLoading
    },
    playlists () {
      return this.$store.state.playlists
    }
  },
  methods: {
    optimizePlaylist ({ playlist, optimizationParameters }) {
      this.$store.dispatch('optimizePlaylist', { playlist, optimizationParameters })
        .then(() => this.displayNotification({ message: `Optimization of ${playlist.name} has been initiated` }))
        .catch(this.displayError)
    },
    importPlaylist (playlist) {
      this.$store.dispatch('importPlaylist', playlist)
        .then(() => this.displayNotification({ message: `${playlist.name} has been added to the library` }))
        .catch(this.displayError)
    }
  }
}
</script>

<style scoped lang="scss">
.playlists {
  display: flex;
  justify-content: center;
  flex-direction: column;
}
</style>
