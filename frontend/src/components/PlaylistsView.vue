<template>
  <div class="playlists-view">
    <dropdown
      v-for="(playlist, index) in playlists"
      :id="index.toString()"
      :key="index"
    >
      <strong slot="header">{{ playlist.name }}</strong>
      <b-card-body slot="body">
        <b-card-text class="mb-0">
          {{ playlist.tracks.length }} tracks
        </b-card-text>
        <b-card-text>
          {{ duration(playlist) }}
        </b-card-text>
        <playlist-view :playlist="playlist"/>
        <b-button
          size="sm"
          variant="light"
          v-b-toggle="'playlist-params'+index.toString()"
          class="mb-1"
        >
          Optimize
        </b-button>
        <b-button
          size="sm"
          variant="light"
          class="mb-1 float-right"
          @click="copyToClipboard(playlist)"
        >
          <b-icon icon="clipboard-check" aria-label="Copy to clipboard"/>
        </b-button>
        <b-collapse :id="'playlist-params'+index.toString()">
          <b-card
            class="mt-1"
            border-variant="light"
            bg-variant="dark"
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
    </dropdown>
  </div>
</template>

<script>
import { BCard, BCollapse, BCardBody, BButton, BCardText, BFormGroup, BFormInput, BFormCheckbox, BIcon } from 'bootstrap-vue'
import Dropdown from '@/components/Dropdown'
import PlaylistView from '@/components/PlaylistView'

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
    Dropdown,
    PlaylistView,
    BCard,
    BCollapse,
    BCardBody,
    BButton,
    BCardText,
    BFormGroup,
    BFormInput,
    BFormCheckbox,
    BIcon
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
        const mins = Math.round(duration - hours * 60)
        return `Total duration ${hours}h ${mins}m`
      }
    },
    optimizePlaylist (playlist) {
      this.$emit('optimize', { playlist, optimizationParameters: this.optimizationParams })
    },
    copyToClipboard (playlist) {
      const tracks = playlist.tracks
        .map((t, i) => `${i + 1}. ${t.artists.join(', ')} - ${t.name}`)
        .join('\n')
      navigator.clipboard.writeText(tracks)
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
