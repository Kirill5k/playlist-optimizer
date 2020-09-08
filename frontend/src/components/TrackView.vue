<template>
  <div class="track-view">
    {{ track }}
  </div>
</template>

<script>
export default {
  name: 'TrackView',
  props: {
    track: Object
  },
  computed: {
    duration () {
      const totalSeconds = Math.floor(this.track.duration)
      if (totalSeconds < 60) {
        return `${totalSeconds}s`
      } else {
        const totalMinutes = Math.floor(totalSeconds / 60)
        const remSeconds = totalSeconds - totalMinutes * 60
        return `${totalMinutes}m ${remSeconds}s`
      }
    }
  }
}
</script>

<style lang="scss">
.track-view {
  text-align: left;
  display: flex;
  justify-content: center;
  flex-direction: column;
  font-size: 12px;
}
</style>
