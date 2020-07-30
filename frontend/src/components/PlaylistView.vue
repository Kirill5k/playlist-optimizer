<template>
  <div>
    {{ playlist.name }}
    <b-table striped hover :items="tracks" :fields="fields"></b-table>
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
      fields: ['artists', 'name', 'key', 'mode']
    }
  },
  computed: {
    tracks () {
      return this.playlist.tracks.map(t => ({
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

</style>
