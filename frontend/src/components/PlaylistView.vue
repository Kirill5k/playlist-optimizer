<template>
  <div class="playlist-view">
    <b-table
      small
      borderless
      hover
      no-border-collapse
      head-variant="dark"
      :items="tracks"
      :fields="fields"
    />
  </div>
</template>

<script>
import { BTable } from 'bootstrap-vue'

export default {
  name: 'PlaylistView',
  props: {
    playlist: Object
  },
  components: {
    BTable
  },
  data () {
    return {
      fields: ['#', 'artists', 'name', 'key', 'mode']
    }
  },
  computed: {
    tracks () {
      return this.playlist.tracks.map((t, i) => ({
        '#': i + 1,
        artists: t.artists.join(', '),
        name: t.name,
        key: t.key,
        mode: t.mode
      }))
    }
  }
}
</script>

<style scoped lang="scss">
.playlist-view {
  text-align: left;
  display: flex;
  justify-content: center;
  flex-direction: column;
  font-size: 12px;
}
</style>
