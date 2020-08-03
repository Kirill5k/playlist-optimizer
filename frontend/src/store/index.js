import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const reject = err => Promise.reject(new Error(err))

export default new Vuex.Store({
  state: {
    isAuthenticated: true,
    playlists: [],
    optimizations: []
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
    }
  },
  actions: {
    getPlaylists ({ commit }) {
      return fetch('/api/spotify/playlists')
        .then(res => res.status === 200 ? res.json() : reject(res.status))
        .then(playlists => commit('setPlaylists', playlists))
        .catch(err => {
          console.error(err)
          commit('unAuthenticate')
        })
    },
    getOptimizations ({ commit }) {
      return fetch('/api/playlist-optimizations')
        .then(res => res.status === 200 ? res.json() : reject(res.status))
        .then(optimizations => commit('setOptimizations', optimizations))
        .catch(err => console.error(err))
    },
    optimizePlaylist ({ commit, dispatch }, playlist) {
      return fetch('/api/playlist-optimizations', {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(playlist)
      })
        .then(res => res.status === 201 ? dispatch('getOptimizations') : reject(res.status))
        .catch(err => console.error(err))
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
        .then(res => res.status === 201 ? dispatch('getPlaylists') : reject(res.status))
        .catch(err => console.error(err))
    },
    deleteOptimization ({ commit, dispatch }, id) {
      return fetch('/api/playlists-optimizations/' + id, {
        method: 'DELETE',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin'
      })
        .then(res => res.status === 204 ? dispatch('getOptimizations') : reject(res.status))
        .catch(err => console.error(err))
    }
  },
  modules: {
  }
})
