<template>
  <b-container>
    <b-row>
      <b-col>
        <optimizations-view
          :optimizations="optimizations"
          @save="savePlaylist"
          @delete="deleteOptimization"
        />
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import { BContainer, BCol, BRow } from 'bootstrap-vue'
import OptimizationsView from '@/components/OptimizationsView.vue'
import NotificationsMixin from '@/mixins/NotificationsMixin'

export default {
  name: 'Optimizations',
  components: {
    OptimizationsView, BContainer, BCol, BRow
  },
  mixins: [NotificationsMixin],
  created () {
    this.$store.dispatch('getOptimizations')
  },
  computed: {
    optimizations () {
      return this.$store.state.optimizations
    }
  },
  watch: {
    optimizations (newOpts) {
      const inProgress = newOpts.some(opt => opt.status === 'in progress')
      if (inProgress) {
        setTimeout(() => this.$store.dispatch('getOptimizations'), 2000)
      }
    }
  },
  methods: {
    savePlaylist (playlist) {
      this.$store.dispatch('savePlaylist', playlist)
        .then(() => this.displayNotification({ message: `Playlist ${playlist.name} has been added to the library` }))
        .catch(this.displayError)
    },
    deleteOptimization (id) {
      this.$store.dispatch('deleteOptimization', id)
        .then(() => this.displayNotification({ message: 'Optimization has been deleted' }))
        .catch(this.displayError)
    }
  }
}
</script>
