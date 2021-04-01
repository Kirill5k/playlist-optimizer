<template>
  <div class="track-view m-2">
    <b-card
      :img-src="track.artwork"
      :img-alt="fullName"
      img-width="180"
      img-left
      bg-variant="dark"
      text-variant="white"
      border-variant="dark"
    >
      <b-card-title class="track-view__title">{{fullName}}</b-card-title>
      <b-card-sub-title class="mb-2">{{release}}</b-card-sub-title>
      <b-card-text class="mb-0 mt-3">Duration {{ duration }}</b-card-text>
      <b-card-text class="mb-0">Key {{ track.key }}, mode {{ track.mode }}</b-card-text>
      <b-card-text>Tempo {{ bpm }}, energy {{ track.energy }}, danceability {{ track.danceability }}</b-card-text>

      <b-link :href="track.url" class="p-1 card-link badge badge-success text-wrap" target="_blank">Spotify</b-link>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardText, BLink, BCardTitle, BCardSubTitle } from 'bootstrap-vue'

export default {
  name: 'TrackView',
  props: {
    track: Object
  },
  components: {
    BCard, BCardText, BLink, BCardTitle, BCardSubTitle
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
      if (this.track.release.name && this.track.release.date) {
        const date = this.track.release.date.split('-').reverse().join('/')
        const uid = this.track.release.uid ? ` - ${this.track.release.uid}` : ''
        return `${this.track.release.name} (${this.track.release.kind}), ${date}${uid}`
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

  &__title {
    color: #ffffff
  }
}
</style>
