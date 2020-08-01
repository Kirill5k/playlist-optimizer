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
          <strong>{{ optimization.original.name }}</strong>
          <b-badge variant="info">{{ optimization.status }}</b-badge>
        </p>
      </b-card-header>
      <b-collapse
        :id="'optimization'+index.toString()"
        visible
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body>
          <b-card-text>
            Initiated on {{ optimization.dateInitiated.replace(/T/g, " ") }}
          </b-card-text>
          <b-card-text v-if="optimization.duration" class="small">
            Total duration {{ optimization.duration / 1000 }}s
          </b-card-text>
          <playlist-view :playlist="optimization.original"/>
          <b-button v-if="optimization.result" variant="primary">
            Save
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
  }
}
</script>

<style scoped lang="scss">
.optimizations-view {
  width: 800px;
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
}
</style>
