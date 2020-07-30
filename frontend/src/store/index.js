import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const reject = err => Promise.reject(new Error(err))

export default new Vuex.Store({
  state: {
    isAuthenticated: true,
    playlists: []
  },
  mutations: {
    setPlaylists (state, playlists) {
      state.playlists = playlists
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
    }
  },
  modules: {
  }
})
