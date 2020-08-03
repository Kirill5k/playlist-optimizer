<template>
  <div class="playlist-view">
    <b-table
      sticky-header="400px"
      small
      bordered
      borderless
      hover
      no-border-collapse
      head-variant="dark"
      :items="tracks"
      :fields="fields"
    />
  </div>
</template>

<script>
import { BTable } from 'bootstrap-vue'

export default {
  name: 'PlaylistView',
  props: {
    playlist: Object
  },
  components: {
    BTable
  },
  data () {
    return {
      fields: [
        { key: '#' },
        { key: 'artists' },
        { key: 'name' },
        { key: 'harmonic', tdClass: this.harmonicClass }
      ]
    }
  },
  computed: {
    tracks () {
      return this.playlist.tracks.map((t, i) => ({
        '#': i + 1,
        artists: t.artists.join(', '),
        name: t.name,
        harmonic: this.toHarmonic(t.key, t.mode)
      }))
    }
  },
  methods: {
    toHarmonic (key, mode) {
      const m = mode === 0 ? 'A' : 'B'
      return `${key}${m}`
    },
    harmonicClass (value) {
      return `camelot-${value.toLowerCase()}`
    }
  }
}
</script>

<style lang="scss">
.playlist-view {
  text-align: left;
  display: flex;
  justify-content: center;
  flex-direction: column;
  font-size: 12px;
}
.camelot-1a {
  background-color: #56f1da;
}
.camelot-2a {
  background-color: #7df2aa;
}
.camelot-3a {
  background-color: #aef589;
}
.camelot-4a {
  background-color: #e8daa1;
}
.camelot-5a {
  background-color: #fdbfa7;
}
.camelot-6a {
  background-color: #fdafb7;
}
.camelot-7a {
  background-color: #fdaacc;
}
.camelot-8a {
  background-color: #fdaacc;
}
.camelot-9a {
  background-color: #ddb4fd;
}
.camelot-10a {
  background-color: #becdfd;
}
.camelot-11a {
  background-color: #8ee4f9;
}
.camelot-12a {
  background-color: #55f0f0;
}
.camelot-1b {
  background-color: #01edca;
}
.camelot-2b {
  background-color: #3cee81;
}
.camelot-3b {
  background-color: #86f24f;
}
.camelot-4b {
  background-color: #dfca73;
}
.camelot-5b {
  background-color: #ffa07c;
}
.camelot-6b {
  background-color: #ff8894;
}
.camelot-7b {
  background-color: #ff81b4;
}
.camelot-8b {
  background-color: #ee82d9;
}
.camelot-9b {
  background-color: #ce8fff;
}
.camelot-10b {
  background-color: #9fb6ff;
}
.camelot-11b {
  background-color: #56d9f9;
}
.camelot-12b {
  background-color: #00ebeb;
}

</style>
