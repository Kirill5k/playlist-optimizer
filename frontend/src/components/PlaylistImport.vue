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

    <b-modal ref="my-modal" title="Enter playlist details">
      <div class="d-block text-center">
        <h3>Hello From My Modal!</h3>
      </div>

      <template v-slot:modal-footer="{ ok, hide }">
        <b-button class="mt-3" variant="primary" @click="ok()">Save</b-button>
        <b-button class="mt-3" variant="secondary" @click="hide()">Close</b-button>
      </template>
    </b-modal>
  </div>
</template>

<script>
import { BButton, BModal } from 'bootstrap-vue'

export default {
  name: 'PlaylistImport',
  props: {
    track: Object
  },
  components: {
    BButton, BModal
  },
  methods: {
    showModal () {
      this.$refs['my-modal'].show()
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
