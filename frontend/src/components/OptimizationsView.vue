<template>
  <div class="playlists-view">
    <b-card
      no-body
      class="mt-1 w-100"
      v-for="(playlist, index) in playlists"
      :key="index"
    >
      <b-card-header header-tag="header" class="p-1 playlists-view__header" role="tab">
        <p block v-b-toggle="'playlist'+index.toString()" class="mb-0 p-1 w-100">
          <strong>{{ playlistSummary(playlist) }}</strong>
        </p>
        <b-button size="sm" variant="outline-dark" @click="$emit('optimize', playlist)">
          Optimize
        </b-button>
      </b-card-header>
      <b-collapse
        :id="'playlist'+index.toString()"
        visible
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body>
          <playlist-view :playlist="playlist"/>
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardHeader, BCollapse, BCardBody, BButton } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton
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
    }
  }
}
</script>

<style scoped lang="scss">
.playlists-view {
  width: 800px;
  align-self: center;

  &__header {
    display: flex;
    justify-content: space-between;
    text-align: left;
    line-height: 2;

    *:focus {
      outline: none;
    }
  }
}
</style>
