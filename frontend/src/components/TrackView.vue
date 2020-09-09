<template>
  <div class="track-view m-2">
    <b-card :title="fullName" :sub-title="release">
      <b-card-text class="mb-0 mt-3">Duration {{ duration }}</b-card-text>
      <b-card-text class="mb-0">Key {{ track.key }}, mode {{ track.mode }}</b-card-text>
      <b-card-text>Tempo {{ bpm }}, energy {{ track.energy }}, danceability {{ track.danceability }}</b-card-text>

      <b-link :href="track.url" class="p-1 card-link badge badge-primary text-wrap" target="_blank">Spotify</b-link>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardText, BLink } from 'bootstrap-vue'

export default {
  name: 'TrackView',
  props: {
    track: Object
  },
  components: {
    BCard, BCardText, BLink
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
    },
    artists () {
      return this.track.artists.join(', ')
    },
    fullName () {
      return `${this.artists} - ${this.track.name}`
    },
    release () {
      if (this.track.releaseName && this.track.releaseDate) {
        const date = this.track.releaseDate.split('-').reverse().join('/')
        return `${this.track.releaseName} (${this.track.releaseType}), ${date}`
      } else {
        return ''
      }
    },
    bpm () {
      return Math.round(this.track.tempo)
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
