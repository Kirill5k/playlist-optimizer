<template>
  <div class="optimizations">
    <optimizations-view
      :optimizations="optimizations"
      @save="savePlaylist"
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
  methods: {
    savePlaylist (playlist) {
      this.$store.dispatch('savePlaylist', playlist)
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
