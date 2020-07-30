<template>
  <div>
    <playlist-view
      v-for="(playlist, index) in playlists"
      :playlist="playlist"
      :key="index"
    />
  </div>
</template>

<script>
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView
  },
  props: {
    playlists: Array
  }
}
</script>

<style scoped lang="scss">

</style>
