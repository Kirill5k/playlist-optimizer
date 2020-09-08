<template>
  <b-container>
    <b-row class="justify-content-center">
      <b-col>
        <playlists-view
          v-if="isAuthenticated"
          :playlists="playlists"
          @optimize="optimizePlaylist"
        />
        <a v-else href="/api/spotify/login" aria-label="Left Align">
          <font-awesome-icon :icon="spotifyIcon" size="7x"/>
        </a>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import { BContainer, BCol, BRow } from 'bootstrap-vue'
import PlaylistsView from '@/components/PlaylistsView.vue'
import NotificationsMixin from '@/mixins/NotificationsMixin'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faSpotify } from '@fortawesome/free-brands-svg-icons'

export default {
  name: 'Playlists',
  components: {
    PlaylistsView, FontAwesomeIcon, BContainer, BCol, BRow
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
