<template>
  <div class="playlists-view">
    <b-card
      no-body
      class="mt-1 w-100"
      v-for="(playlist, index) in playlists"
      :key="index"
    >
      <b-card-header
        header-bg-variant="dark"
        header-text-variant="white"
        header-tag="header"
        class="p-1 playlists-view__header"
        role="tab"
      >
        <p v-b-toggle="'playlist'+index.toString()" class="mb-0 p-1 w-100">
          <strong>{{ playlist.name }}</strong>
        </p>
      </b-card-header>
      <b-collapse
        :id="'playlist'+index.toString()"
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body>
          <b-card-text class="mb-0">
            {{ playlist.tracks.length }} tracks
          </b-card-text>
          <b-card-text>
            Total duration {{ duration(playlist) }} min
          </b-card-text>
          <playlist-view :playlist="playlist"/>
          <b-button size="sm" variant="info" @click="$emit('optimize', playlist)">
            Optimize
          </b-button>
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText
  },
  props: {
    playlists: Array
  },
  methods: {
    playlistSummary (playlist) {
      const name = playlist.name
      const tracks = playlist.tracks.length
      const duration = playlist.tracks.map(t => t.duration).reduce((s, d) => s + d, 0) / 60
      return `${name} (${tracks} tracks / total duration ${duration.toFixed(2)} min)`
    },
    duration (playlist) {
      const duration = playlist.tracks.map(t => t.duration).reduce((s, d) => s + d, 0) / 60
      return duration.toFixed(2)
    }
  }
}
</script>

<style scoped lang="scss">
.playlists-view {
  width: 800px;
  align-self: center;
  text-align: left;

  &__header {
    display: flex;
    justify-content: space-between;
    line-height: 2;

    *:focus {
      outline: none;
    }
  }
}
</style>
