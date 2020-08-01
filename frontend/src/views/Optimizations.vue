<template>
  <div class="optimizations">
    <optimizations-view
      :optimizations="optimizations"
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
