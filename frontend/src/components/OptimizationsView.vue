<template>
  <div class="optimizations-view">
    <b-card
      no-body
      class="mt-1 w-100"
      v-for="(optimization, index) in optimizations"
      :key="index"
    >
      <b-card-header header-tag="header" class="p-1 optimizations-view__header" role="tab">
        <p v-b-toggle="'optimization'+index.toString()" class="mb-0 p-1 w-100">
          <strong>{{ optimization.original.name }}</strong> playlist optimization
          <b-badge :variant="optimizationStatusVariant(optimization.status)" class="ml-2">{{ optimization.status }}</b-badge>
        </p>
      </b-card-header>
      <b-collapse
        :id="'optimization'+index.toString()"
        visible
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body class="optimizations-view__body">
          <b-card-text class="mb-0">
            Initiated on {{ optimization.dateInitiated.slice(0, 10) }} at {{ optimization.dateInitiated.slice(11, 19) }}
          </b-card-text>
          <b-card-text v-if="optimization.durationMs" class="small mb-0">
            Total duration {{ optimization.durationMs / 1000 }}s
          </b-card-text>
          <b-card-text v-if="optimization.score" class="small mb-0">
            Optimization score {{ optimization.score }}
          </b-card-text>
          <div class="optimizations-view__results">
            <playlist-view :playlist="optimization.original" class="w-50"/>
            <playlist-view v-if="optimization.result" :playlist="optimization.result" class="w-50"/>
          </div>
          <b-button v-if="optimization.result" variant="primary">
            Save optimized playlist
          </b-button>
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardHeader, BCollapse, BCardBody, BButton, BBadge, BCardText } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'OptimizationsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton, BBadge, BCardText
  },
  props: {
    optimizations: Array
  },
  methods: {
    optimizationStatusVariant (status) {
      switch (status) {
        case 'completed':
          return 'success'
        default:
          return 'primary'
      }
    }
  }
}
</script>

<style lang="scss">
.optimizations-view {
  width: 1000px;
  align-self: center;

  &__header {
    display: flex;
    justify-content: space-between;
    text-align: left;
    line-height: 2;

    *:focus {
      outline: none;
    }
  }

  &__body {
    display: flex;
    flex-direction: column;
    justify-content: left;
    align-items: flex-start;
  }

  &__results {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 20px 0;
  }
}
</style>
