<template>
  <div class="playlists">
    <b-card
      no-body
      class="mt-1"
      v-for="(playlist, index) in playlists"
      :key="index"
    >
      <b-card-header header-tag="header" class="p-1" role="tab">
        <b-button block v-b-toggle="'playlist'+index.toString()" variant="info">{{ playlist.name }}</b-button>
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
import { BCard, BCardHeader, BButton, BCollapse, BCardBody } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView, BCard, BCardHeader, BButton, BCollapse, BCardBody
  },
  props: {
    playlists: Array
  }
}
</script>

<style scoped lang="scss">
.playlists {
  width: 800px;
  display: flex;
  justify-content: center;
  align-self: center;
}
</style>
