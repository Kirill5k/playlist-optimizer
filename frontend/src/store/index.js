import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const reject = (res) => res.json().then(e => Promise.reject(new Error(e.message)))

export default new Vuex.Store({
  state: {
    isAuthenticated: false,
    isLoading: false,
    playlists: [],
    optimizations: [],
    currentTrack: null
  },
  mutations: {
    setPlaylists (state, playlists) {
      state.playlists = playlists
    },
    loading (state) {
      state.isLoading = true
    },
    loaded (state) {
      state.isLoading = false
    },
    setOptimizations (state, optimizations) {
      state.optimizations = optimizations
    },
    authenticate (state) {
      state.isAuthenticated = true
    },
    unAuthenticate (state) {
      state.isAuthenticated = false
    },
    setCurrentTrack (state, track) {
      state.currentTrack = track
    },
    clearCurrentTrack (state) {
      state.currentTrack = null
    }
  },
  actions: {
    findTrack ({ commit }, name) {
      return fetch(`/api/spotify/tracks?name=${name}`)
        .then(res => res.status === 200 ? res.json() : reject(res))
    },
    getPlaylists ({ commit, state }) {
      if (state.playlists.length === 0) {
        commit('loading')
      }
      return fetch('/api/spotify/playlists')
        .then(res => {
          commit('loaded')
          return res.status === 200 ? res.json() : reject(res)
        })
        .then(playlists => commit('setPlaylists', playlists))
        .then(() => commit('authenticate'))
        .catch(() => commit('unAuthenticate'))
    },
    getOptimizations ({ commit }) {
      return fetch('/api/playlist-optimizations')
        .then(res => res.status === 200 ? res.json() : reject(res))
        .then(optimizations => commit('setOptimizations', optimizations))
        .catch(() => {})
    },
    optimizePlaylist ({ commit, dispatch }, requestBody) {
      return fetch('/api/playlist-optimizations', {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })
        .then(res => res.status === 201 ? res.json() : reject(res))
    },
    savePlaylist ({ commit, dispatch }, playlist) {
      return fetch('/api/spotify/playlists', {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(playlist)
      })
        .then(res => res.status === 201 ? dispatch('getPlaylists') : reject(res))
    },
    importPlaylist ({ commit, dispatch }, playlist) {
      return fetch('/api/spotify/playlists/import', {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(playlist)
      })
        .then(res => res.status === 201 ? dispatch('getPlaylists') : reject(res))
    },
    deleteOptimization ({ commit, dispatch }, id) {
      return fetch(`/api/playlist-optimizations/${id}`, { method: 'DELETE' })
        .then(res => res.status === 204 ? dispatch('getOptimizations') : reject(res))
    }
  },
  modules: {
  }
})
