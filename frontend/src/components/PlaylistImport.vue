<template>
  <div class="playlist-import">
    <b-button
      class="playlist-import__import-button"
      variant="outline-primary"
      size="sm"
      @click="showModal"
    >
      Import
    </b-button>

    <b-modal
      size="sm"
      ref="my-modal"
      title="New playlist"
      content-class="playlist-import__import-modal"
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
    showModal () {
      this.$refs['my-modal'].show()
    },
    save () {
      console.log('saving form')
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

  &__import-modal {
    min-width: 600px;
  }
}
</style>
