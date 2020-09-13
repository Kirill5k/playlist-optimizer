<template>
  <div class="playlists-view">
    <b-card
      no-body
      class="mt-1 w-100"
      v-for="(playlist, index) in playlists"
      :key="index"
    >
      <b-card-header
        header-bg-variant="dark"
        header-text-variant="white"
        header-tag="header"
        class="p-1 playlists-view__header"
        role="tab"
      >
        <p v-b-toggle="'playlist'+index.toString()" class="mb-0 p-1 w-100">
          <strong>{{ playlist.name }}</strong>
        </p>
      </b-card-header>
      <b-collapse
        :id="'playlist'+index.toString()"
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body>
          <b-card-text class="mb-0">
            {{ playlist.tracks.length }} tracks
          </b-card-text>
          <b-card-text>
            {{ duration(playlist) }}
          </b-card-text>
          <playlist-view :playlist="playlist"/>
          <b-button size="sm" variant="info" v-b-toggle="'playlist-params'+index.toString()" class="mb-1">
            Optimize
          </b-button>
          <b-collapse :id="'playlist-params'+index.toString()">
            <b-card
              border-variant="info"
              class="mt-1"
              bg-variant="light"
              body-class="pt-3 pb-3"
            >
              <div class="d-flex justify-content-start">
                <b-form-group
                  :id="'population-size'+index.toString()"
                  label="Population size"
                  :label-for="'population-size-input'+index.toString()"
                  label-size="sm"
                  label-class="mb-0"
                  class="mr-2 w-25"
                >
                  <b-form-input
                    type="number"
                    step="10"
                    min="10"
                    max="10000"
                    size="sm"
                    :id="'population-size-input'+index.toString()"
                    v-model="optimizationParams.populationSize"
                    trim
                  />
                </b-form-group>
                <b-form-group
                  :id="'max-gen'+index.toString()"
                  label="Max Generation"
                  :label-for="'max-gen-input'+index.toString()"
                  label-size="sm"
                  label-class="mb-0"
                  class="mr-2 w-25"
                >
                  <b-form-input
                    step="10"
                    type="number"
                    min="10"
                    max="10000"
                    size="sm"
                    :id="'max-gen-input'+index.toString()"
                    v-model="optimizationParams.maxGen"
                    trim
                  />
                </b-form-group>
              </div>
              <div class="d-flex justify-content-start">
                <b-form-group
                  :id="'crossover-probability'+index.toString()"
                  label="Crossover Probability"
                  :label-for="'crossover-probability-input'+index.toString()"
                  label-size="sm"
                  label-class="mb-0"
                  class="mr-2 w-25"
                >
                  <b-form-input
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="1"
                    size="sm"
                    :id="'crossover-probability-input'+index.toString()"
                    v-model="optimizationParams.crossoverProbability"
                    trim
                  />
                </b-form-group>
                <b-form-group
                  :id="'mutation-probability'+index.toString()"
                  label="Mutation Probability"
                  :label-for="'mutation-probability-input'+index.toString()"
                  label-size="sm"
                  label-class="mb-0"
                  class="mr-2 w-25"
                >
                  <b-form-input
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="1"
                    size="sm"
                    :id="'mutation-probability-input'+index.toString()"
                    v-model="optimizationParams.mutationProbability"
                    trim
                  />
                </b-form-group>
                <b-form-group
                  :id="'elitism-ratio'+index.toString()"
                  label="Elitism ratio"
                  :label-for="'elitism-ratio-input'+index.toString()"
                  label-size="sm"
                  label-class="mb-0"
                  class="w-25"
                >
                  <b-form-input
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="1"
                    size="sm"
                    :id="'elitism-ratio-input'+index.toString()"
                    v-model="optimizationParams.elitismRatio"
                    trim
                  />
                </b-form-group>
              </div>
              <b-form-checkbox
                class="mb-1"
                v-model="optimizationParams.shuffle"
                :id="'shuffle-radio'+index.toString()"
                size="sm"
                switch
              >
                Shuffle on start
              </b-form-checkbox>
              <b-button
                variant="success"
                @click="optimizePlaylist(playlist)"
                size="sm"
              >
                Start optimization
              </b-button>
            </b-card>
          </b-collapse>
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText, BFormGroup, BFormInput, BFormCheckbox } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

const DEFAULT_OPTIMIZATION_PARAMS = {
  populationSize: 250,
  maxGen: 1000,
  crossoverProbability: 0.6,
  mutationProbability: 0.1,
  elitismRatio: 0.1,
  shuffle: true
}

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText, BFormGroup, BFormInput, BFormCheckbox
  },
  props: {
    playlists: Array
  },
  data () {
    return {
      optimizationParams: {
        ...DEFAULT_OPTIMIZATION_PARAMS
      }
    }
  },
  methods: {
    duration (playlist) {
      const duration = playlist.tracks.map(t => t.duration).reduce((s, d) => s + d, 0) / 60
      if (duration < 60) {
        return `Total duration ${duration.toFixed(2)}m`
      } else {
        const hours = Math.floor(duration / 60)
        const mins = duration - hours * 60
        return `Total duration ${hours}h ${mins.toFixed(2)}m`
      }
    },
    optimizePlaylist (playlist) {
      this.$emit('optimize', { playlist, optimizationParameters: this.optimizationParams })
    }
  }
}
</script>

<style scoped lang="scss">
.playlists-view {
  width: 100%;
  align-self: center;
  text-align: left;

  &__header {
    display: flex;
    justify-content: space-between;
    line-height: 2;

    *:focus {
      outline: none;
    }
  }
}
</style>
