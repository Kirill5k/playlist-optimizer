<template>
  <div class="tracks">
    <track-search-bar
      @input="findTrack"
    />
    <track-view
      v-if="track"
      :track="track"
    />
  </div>
</template>

<script>
import NotificationsMixin from '@/mixins/NotificationsMixin'
import TrackSearchBar from '@/components/TrackSearchBar.vue'
import TrackView from '@/components/TrackView.vue'

export default {
  name: 'Tracks',
  components: {
    TrackSearchBar, TrackView
  },
  mixins: [NotificationsMixin],
  computed: {
    track () {
      return this.$store.state.currentTrack
    }
  },
  methods: {
    findTrack (name) {
      if (name.length) {
        this.$store
          .dispatch('findTrack', name)
          .then(track => this.$store.commit('setCurrentTrack', track))
          .catch(this.displayError)
      } else {
        this.$store.commit('clearCurrentTrack')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.tracks {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
}
</style>
