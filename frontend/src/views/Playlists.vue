<template>
  <div v-if="isAuthenticated" class="playlists">
    <playlists-view
      :playlists="playlists"
      @optimize="optimizePlaylist"
    />
    <playlist-import />
  </div>
  <div v-else class="playlists">
    <a href="/api/spotify/login" aria-label="Left Align">
      <font-awesome-icon :icon="spotifyIcon" size="7x"/>
    </a>
  </div>
</template>

<script>
import PlaylistsView from '@/components/PlaylistsView'
import PlaylistImport from '@/components/PlaylistImport'
import NotificationsMixin from '@/mixins/NotificationsMixin'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faSpotify } from '@fortawesome/free-brands-svg-icons'

export default {
  name: 'Playlists',
  components: {
    PlaylistsView,
    PlaylistImport,
    FontAwesomeIcon
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
    playlists () {
      return this.$store.state.playlists
    }
  },
  methods: {
    optimizePlaylist ({ playlist, optimizationParameters }) {
      this.$store.dispatch('optimizePlaylist', { playlist, optimizationParameters })
        .then(() => this.displayNotification({ message: `Optimization of ${playlist.name} has been initiated` }))
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
