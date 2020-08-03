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
    },
    deleteOptimization (id) {
      this.$store.dispatch('deleteOptimization', id)
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
