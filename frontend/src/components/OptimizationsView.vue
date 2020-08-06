<template>
  <div class="optimizations-view">
    <b-card
      no-body
      class="mt-1 w-100"
      v-for="(optimization, index) in optimizations"
      :key="index"
    >
      <b-card-header
        header-tag="header"
        header-bg-variant="dark"
        header-text-variant="white"
        class="p-1 optimizations-view__header"
        role="tab"
      >
        <p v-b-toggle="'optimization'+index.toString()" class="mb-0 p-1 w-100">
          <strong>{{ optimization.original.name }}</strong> playlist optimization
          <b-badge :variant="optimizationStatusVariant(optimization.status)" class="ml-2">{{ optimization.status }}</b-badge>
        </p>
        <b-spinner v-if="optimization.status === 'in progress'" small class="mt-2 mr-3" label="Loading..."></b-spinner>
      </b-card-header>
      <b-collapse
        :id="'optimization'+index.toString()"
        accordion="my-accordion"
        role="tabpanel"
      >
        <b-card-body class="optimizations-view__body">
          <b-card-text class="mb-0">
            Initiated on {{ optimization.dateInitiated.slice(0, 10) }} at {{ optimization.dateInitiated.slice(11, 19) }}
          </b-card-text>
          <b-card-text class="mb-0">
            {{ optimizationParameters(optimization.parameters) }}
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
          <div v-if="optimization.result" class="optimizations-view__controls">
            <b-button v-if="!displayPlaylistSaveForm"  variant="primary" size="sm" @click="showSavePlaylistForm(optimization.result.name)">
              Save optimized playlist
            </b-button>
            <b-form v-else inline @submit.prevent="savePlaylist(optimization.result)">
              <label class="sr-only" :for="`optimized-playlist-name-${index}`">New playlist name</label>
              <b-button
                v-if="newPlaylistNameIsValid"
                variant="outline-success"
                size="sm"
                class="mr-2"
                @click="savePlaylist(optimization.result)"
              >
                Save
              </b-button>
              <b-form-input
                :id="`optimized-playlist-name-${index}`"
                size="sm"
                class="mr-2"
                placeholder="Optimized Playlist"
                style="width: 300px"
                v-model="newPlaylistName"
                :state="newPlaylistNameIsValid"
              />
              <b-button
                variant="outline-danger"
                size="sm"
                @click="hideSavePlaylistForm"
                class="mr-2"
              >
                Cancel
              </b-button>
            </b-form>
            <b-button class="float-right"  variant="danger" size="sm" @click="deleteOptimization(optimization.id)">
              Delete optimization
            </b-button>
          </div>
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script>
import { BCard, BCardHeader, BCollapse, BCardBody, BButton, BBadge, BCardText, BForm, BFormInput, BSpinner } from 'bootstrap-vue'
import PlaylistView from '@/components/PlaylistView.vue'

export default {
  name: 'OptimizationsView',
  components: {
    PlaylistView, BCard, BCardHeader, BCollapse, BCardBody, BButton, BBadge, BCardText, BForm, BFormInput, BSpinner
  },
  data () {
    return {
      displayPlaylistSaveForm: false,
      newPlaylistName: ''
    }
  },
  props: {
    optimizations: Array
  },
  computed: {
    newPlaylistNameIsValid () {
      return this.newPlaylistName.length > 2
    }
  },
  methods: {
    optimizationStatusVariant (status) {
      switch (status) {
        case 'completed':
          return 'success'
        default:
          return 'secondary'
      }
    },
    showSavePlaylistForm (newPlaylistName) {
      this.displayPlaylistSaveForm = true
      this.newPlaylistName = newPlaylistName
    },
    hideSavePlaylistForm () {
      this.displayPlaylistSaveForm = false
      this.newPlaylistName = ''
    },
    savePlaylist (playlist) {
      const newPlaylist = { ...playlist, name: this.newPlaylistName }
      this.$emit('save', newPlaylist)
      this.hideSavePlaylistForm()
    },
    deleteOptimization (id) {
      this.$emit('delete', id)
    },
    optimizationParameters (params) {
      const popSize = `Population size ${params.populationSize}`
      const mutFactor = `Mutation factor ${params.mutationFactor}`
      const its = `${params.iterations} iterations`
      const shuffle = params.shuffle ? 'Shuffle' : 'No Shuffle'
      return `${popSize} / ${mutFactor} / ${its} / ${shuffle}`
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
    padding-top: 20px;
  }

  &__controls {
    display: flex;
    width: 100%;
    justify-content: space-between;
  }
}
</style>
