<template>
  <div class="playlist-import">
    <b-button
      class="playlist-import__import-button"
      variant="outline-primary"
      size="sm"
      @click="show"
    >
      Import
    </b-button>

    <b-modal
      size="lg"
      ref="playlist-import-modal"
      title="New playlist"
      @hidden="reset"
    >
      <b-form
        @submit.prevent.stop
      >
        <b-form-group
          id="playlist-name-group"
          label="Playlist name:"
          label-for="playlist-name"
          label-size="sm"
          label-class="mb-0"
        >
          <b-form-input
            id="playlist-name"
            v-model="name"
            type="text"
            required
            placeholder="My new playlist"
            size="sm"
            :state="isValidName"
            trim
          />

          <b-form-invalid-feedback size="sm" :state="isValidName">
            Playlist name is required
          </b-form-invalid-feedback>
        </b-form-group>

        <b-form-group
          id="playlist-description-group"
          label="Playlist description:"
          label-for="playlist-description"
          label-size="sm"
          label-class="mb-0"
        >
          <b-form-input
            id="playlist-description"
            v-model="description"
            placeholder="Manually imported playlist"
            size="sm"
            trim
          />
        </b-form-group>

        <b-form-group
          id="playlist-tracks-group"
          label="Playlist tracks:"
          label-for="playlist-tracks"
          label-size="sm"
          label-class="mb-0"
        >
          <b-form-textarea
            id="playlist-tracks"
            v-model="tracks"
            rows="9"
            size="sm"
            no-resize
            no-auto-shrink
            required
            trim
            :state="areValidTracks"
            @drop="dropFile"
          />

          <b-form-invalid-feedback size="sm" :state="areValidTracks">
            At least 1 track is required
          </b-form-invalid-feedback>
        </b-form-group>
      </b-form>

      <template v-slot:modal-footer="{ ok, hide }">
        <b-button size="sm" class="mt-3" variant="primary" @click="save">Save</b-button>
        <b-button size="sm" class="mt-3" variant="secondary" @click="hide()">Close</b-button>
      </template>
    </b-modal>
  </div>
</template>

<script>
import { BButton, BModal, BForm, BFormGroup, BFormInput, BFormTextarea, BFormInvalidFeedback } from 'bootstrap-vue'

export default {
  name: 'PlaylistImport',
  props: {
    track: Object
  },
  components: {
    BButton, BModal, BForm, BFormGroup, BFormInput, BFormTextarea, BFormInvalidFeedback
  },
  data () {
    return {
      name: null,
      description: null,
      tracks: null
    }
  },
  computed: {
    isValidName () {
      return this.name === null ? null : this.name.length > 0
    },
    areValidTracks () {
      return this.tracks === null ? null : this.tracks.length > 0
    }
  },
  methods: {
    reset () {
      this.name = null
      this.description = null
      this.tracks = null
    },
    show () {
      this.$refs['playlist-import-modal'].show()
    },
    save () {
      this.triggerValidation()
      if (this.isValidName === true && this.areValidTracks === true) {
        const importedPlaylist = {
          name: this.name,
          description: this.description,
          tracks: this.tracks.split('\n')
        }
        this.$emit('import', importedPlaylist)
      }
    },
    triggerValidation () {
      if (this.name === null) {
        this.name = ''
      }
      if (this.tracks === null) {
        this.tracks = ''
      }
    },
    dropFile (event) {
      event.preventDefault()
      const files = event.dataTransfer.items
        ? Array.from(event.dataTransfer.items).filter(f => f.kind === 'file').map(f => f.getAsFile())
        : Array.from(event.dataTransfer.files)
      const fileNames = files.map(f => f.name.replace(/\.[0-9a-z]{1,5}$/i, ''))
      const currentTracks = this.tracks && this.tracks.length > 0
        ? this.tracks.endsWith('\n') ? this.tracks : `${this.tracks}\n`
        : ''
      this.tracks = currentTracks + fileNames.join('\n')
    }
  }
}
</script>

<style lang="scss">
.playlist-import {
  text-align: left;
  font-size: 12px;
  margin: 6px 0;

  &__import-button {
    float: right;
    border: 1px solid #000;
  }
}
</style>
