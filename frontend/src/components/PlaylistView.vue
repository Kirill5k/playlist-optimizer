<template>
  <div>
    {{ playlist.name }}
  </div>
</template>

<script>
export default {
  name: 'PlaylistView',
  props: {
    playlist: Object
  }
}
</script>

<style scoped lang="scss">

</style>
