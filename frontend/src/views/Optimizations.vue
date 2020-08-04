<template>
  <div class="optimizations">
    <optimizations-view
      :optimizations="optimizations"
      @save="savePlaylist"
      @delete="deleteOptimization"
    />
  </div>
</template>

<script>
import OptimizationsView from '@/components/OptimizationsView.vue'

export default {
  name: 'Optimizations',
  components: {
    OptimizationsView
  },
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
    },
    deleteOptimization (id) {
      this.$store.dispatch('deleteOptimization', id)
        .then(() => this.displayNotification({ message: 'Optimization has been deleted' }))
    },
    displayNotification (props) {
      this.$bvToast.toast(props.message, {
        title: 'Success!',
        autoHideDelay: 3000,
        appendToast: true,
        solid: true,
        toaster: 'b-toaster-bottom-right',
        variant: 'success',
        ...props
      })
    }
  }
}
</script>

<style scoped lang="scss">
.optimizations {
  display: flex;
  justify-content: center;
  flex-direction: column;
}
</style>
