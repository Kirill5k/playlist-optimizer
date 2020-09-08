<template>
  <b-container>
    <b-row clas="justify-content-md-center">
      <b-col>
        <track-search-bar
          @input="findTrack"
        />
      </b-col>
    </b-row>
    <b-row v-if="track">
      <b-col>
        <track-view
          :track="track"
        />
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import { BContainer, BCol, BRow } from 'bootstrap-vue'
import NotificationsMixin from '@/mixins/NotificationsMixin'
import TrackSearchBar from '@/components/TrackSearchBar.vue'
import TrackView from '@/components/TrackView.vue'

export default {
  name: 'Tracks',
  components: {
    TrackSearchBar, TrackView, BContainer, BCol, BRow
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
        this.$store.dispatch('findTrack', name).catch(this.displayError)
      } else {
        this.$store.commit('clearCurrentTrack')
      }
    }
  }
}
</script>
