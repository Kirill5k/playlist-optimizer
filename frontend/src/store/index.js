import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const reject = (res) => res.json().then(e => Promise.reject(new Error(`${res.status}: ${e.message}`)))

export default new Vuex.Store({
  state: {
    isAuthenticated: true,
    playlists: [],
    optimizations: [],
    currentTrack: null
  },
  mutations: {
    setPlaylists (state, playlists) {
      state.playlists = playlists
    },
    setOptimizations (state, optimizations) {
      state.optimizations = optimizations
    },
    unAuthenticate (state) {
      state.isAuthenticated = false
    },
    setCurrentTrack (state, track) {
      state.currentTrack = track
    }
  },
  actions: {
    findTrack ({ commit }, name) {
      return fetch(`/api/spotify/tracks?name=${name}`)
        .then(res => res.status === 200 ? res.json() : reject(res))
        .then(track => commit('setCurrentTrack', track))
    },
    getPlaylists ({ commit }) {
      return fetch('/api/spotify/playlists')
        .then(res => res.status === 200 ? res.json() : reject(res))
        .then(playlists => commit('setPlaylists', playlists))
        .catch(err => {
          console.error(err)
          commit('unAuthenticate')
        })
    },
    getOptimizations ({ commit }) {
      return fetch('/api/playlist-optimizations')
        .then(res => res.status === 200 ? res.json() : reject(res))
        .then(optimizations => commit('setOptimizations', optimizations))
        .catch(err => console.error(err))
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
    deleteOptimization ({ commit, dispatch }, id) {
      return fetch(`/api/playlist-optimizations/${id}`, { method: 'DELETE' })
        .then(res => res.status === 204 ? dispatch('getOptimizations') : reject(res))
    }
  },
  modules: {
  }
})
