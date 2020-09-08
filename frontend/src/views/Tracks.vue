<template>
  <div class="tracks">
  </div>
</template>

<script>
import NotificationsMixin from '@/mixins/NotificationsMixin'
import {faSpotify} from "@fortawesome/free-brands-svg-icons";

export default {
  name: 'Tracks',
  components: {
    OptimizationsView
  },
  mixins: [NotificationsMixin],
  computed: {
    track () {
      return this.$store.state.currentTrack
    }
  },
  methods: {
    findTrack (name) {
      this.$store.dispatch('findTrack', name)
        .catch(this.displayError)
    }
  }
}
</script>

<style scoped lang="scss">
.tracks {
  display: flex;
  justify-content: center;
  flex-direction: column;
}
</style>
