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
            Total duration {{ duration(playlist) }} min
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
                  :id="'iterations'+index.toString()"
                  label="Iterations"
                  :label-for="'iterations-input'+index.toString()"
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
                    :id="'iterations-input'+index.toString()"
                    v-model="optimizationParams.iterations"
                    trim
                  />
                </b-form-group>
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
                  :id="'mutation-factor'+index.toString()"
                  label="Mutation Factor"
                  :label-for="'mutation-factor-input'+index.toString()"
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
                    :id="'mutation-factor-input'+index.toString()"
                    v-model="optimizationParams.mutationFactor"
                    trim
                  />
                </b-form-group>
              </div>
              <b-form-group
                :id="'shuffle'+index.toString()"
                :label-for="'shuffle-radio'+index.toString()"
                label="Shuffle on start"
                label-size="sm"
                label-class="mb-0"
              >
                <b-form-radio-group
                  v-model="optimizationParams.shuffle"
                  :options="[{ text: 'Yes', value: true }, { text: 'No', value: false }]"
                  :id="'shuffle-radio'+index.toString()"
                ></b-form-radio-group>
              </b-form-group>
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
import { BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText, BFormGroup, BFormInput, BFormRadioGroup } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

const DEFAULT_OPTIMIZATION_PARAMS = {
  populationSize: 100,
  mutationFactor: 0.2,
  iterations: 250,
  shuffle: true
}

export default {
  name: 'PlaylistsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton, BCardText, BFormGroup, BFormInput, BFormRadioGroup
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
      return duration.toFixed(2)
    },
    optimizePlaylist (playlist) {
      this.$emit('optimize', { playlist, optimizationParameters: this.optimizationParams })
    }
  }
}
</script>

<style scoped lang="scss">
.playlists-view {
  width: 800px;
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
