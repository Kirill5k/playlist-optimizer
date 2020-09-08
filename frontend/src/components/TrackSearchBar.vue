<template>
  <div class="track-search-bar">
    <b-form-input
      id="track-search-input"
      placeholder="Enter track name"
      :size="size"
      type="text"
      v-model="query"
      debounce="500"
      trim
    />
  </div>
</template>

<script>
import { BFormInput } from 'bootstrap-vue'

export default {
  name: 'TrackSearchBar',
  components: {
    BFormInput
  },
  data () {
    return {
      query: ''
    }
  },
  watch: {
    query (newQuery) {
      this.$emit('input', newQuery)
    }
  },
  props: {
    size: {
      type: String,
      default: 'default'
    }
  }
}
</script>

<style scoped lang="scss">
.track-search-bar {
  width: 100%;
  max-width: 800px;
}
</style>
