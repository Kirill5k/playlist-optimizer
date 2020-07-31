import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const reject = err => Promise.reject(new Error(err))

export default new Vuex.Store({
  state: {
    isAuthenticated: true,
    /* eslint-disable */
    playlists: [
      // eslint-disable-next-line
      {
        "name": "Mel optimized",
        "description": "Melodic deep house and techno songs",
        "source": "Spotify",
        "tracks": [
          {
            "name": "April",
            "artists": [
              "Jonas Saalbach"
            ],
            "releaseName": "April",
            "releaseDate": "2018-03-23",
            "releaseType": "single",
            "tempo": 123.988,
            "duration": 498.938,
            "key": 10,
            "mode": 1,
            "uri": "spotify:track:6v4ni85b9wX6IavE1b3Muf",
            "url": "https://open.spotify.com/track/6v4ni85b9wX6IavE1b3Muf"
          },
          {
            "name": "Gwendoline - Original Mix",
            "artists": [
              "Clawz SG"
            ],
            "releaseName": "Gwendoline",
            "releaseDate": "2015-03-09",
            "releaseType": "single",
            "tempo": 124.012,
            "duration": 452.913,
            "key": 10,
            "mode": 0,
            "uri": "spotify:track:6heLeWInDSLU9wdrKhx2l5",
            "url": "https://open.spotify.com/track/6heLeWInDSLU9wdrKhx2l5"
          },
          {
            "name": "Indenait",
            "artists": [
              "Edu Imbernon"
            ],
            "releaseName": "Indenait",
            "releaseDate": "2018-09-24",
            "releaseType": "single",
            "tempo": 121.005,
            "duration": 577.75,
            "key": 9,
            "mode": 0,
            "uri": "spotify:track:5wC5dAdAQPzeuhmITZVAiO",
            "url": "https://open.spotify.com/track/5wC5dAdAQPzeuhmITZVAiO"
          },
          {
            "name": "You Caress",
            "artists": [
              "Giorgia Angiuli",
              "Lake Avalon"
            ],
            "releaseName": "No Body No Pain",
            "releaseDate": "2018-04-13",
            "releaseType": "single",
            "tempo": 124.0,
            "duration": 503.549,
            "key": 9,
            "mode": 1,
            "uri": "spotify:track:7FyQCtWo4BYc9RdpEFcrSp",
            "url": "https://open.spotify.com/track/7FyQCtWo4BYc9RdpEFcrSp"
          },
          {
            "name": "Into The Light",
            "artists": [
              "James Trystan"
            ],
            "releaseName": "Modeplex Presents Authentic Steyoyoke #015",
            "releaseDate": "2019-08-12",
            "releaseType": "album",
            "tempo": 123.01,
            "duration": 409.6,
            "key": 8,
            "mode": 1,
            "uri": "spotify:track:79r2pk6jb2x18D5bLyAiT3",
            "url": "https://open.spotify.com/track/79r2pk6jb2x18D5bLyAiT3"
          },
          {
            "name": "Human",
            "artists": [
              "Julian Wassermann"
            ],
            "releaseName": "Human / Polydo",
            "releaseDate": "2018-03-19",
            "releaseType": "single",
            "tempo": 124.997,
            "duration": 422.197,
            "key": 8,
            "mode": 1,
            "uri": "spotify:track:7duQlpZgEexH9E4alUXwhe",
            "url": "https://open.spotify.com/track/7duQlpZgEexH9E4alUXwhe"
          },
          {
            "name": "Santiago",
            "artists": [
              "YNOT"
            ],
            "releaseName": "U & I",
            "releaseDate": "2016-06-03",
            "releaseType": "single",
            "tempo": 122.011,
            "duration": 310.943,
            "key": 8,
            "mode": 1,
            "uri": "spotify:track:7bWyKnNHQaMxgDqyxJHyyE",
            "url": "https://open.spotify.com/track/7bWyKnNHQaMxgDqyxJHyyE"
          },
          {
            "name": "Shift",
            "artists": [
              "ALMA (GER)"
            ],
            "releaseName": "Timeshift",
            "releaseDate": "2018-06-08",
            "releaseType": "single",
            "tempo": 120.998,
            "duration": 404.635,
            "key": 7,
            "mode": 1,
            "uri": "spotify:track:4R9ElixwzY1W9gNZoMcixQ",
            "url": "https://open.spotify.com/track/4R9ElixwzY1W9gNZoMcixQ"
          },
          {
            "name": "Twisted Shapes",
            "artists": [
              "Jonas Saalbach",
              "Chris McCarthy"
            ],
            "releaseName": "Reminiscence",
            "releaseDate": "2019-02-22",
            "releaseType": "album",
            "tempo": 120.997,
            "duration": 493.008,
            "key": 7,
            "mode": 1,
            "uri": "spotify:track:1DafZ2A2bRbXfLA7KVVYX7",
            "url": "https://open.spotify.com/track/1DafZ2A2bRbXfLA7KVVYX7"
          },
          {
            "name": "Mirage",
            "artists": [
              "Anden"
            ],
            "releaseName": "Mirage",
            "releaseDate": "2019-11-22",
            "releaseType": "single",
            "tempo": 124.032,
            "duration": 308.736,
            "key": 6,
            "mode": 1,
            "uri": "spotify:track:4Rys9v6OjDVCbW7JOdkQY7",
            "url": "https://open.spotify.com/track/4Rys9v6OjDVCbW7JOdkQY7"
          },
          {
            "name": "Rapture",
            "artists": [
              "Lunar Plane"
            ],
            "releaseName": "Rapture / Chimera",
            "releaseDate": "2018-05-21",
            "releaseType": "single",
            "tempo": 120.002,
            "duration": 452.38,
            "key": 6,
            "mode": 0,
            "uri": "spotify:track:2iLz47TwcEN22gTpbTYiU2",
            "url": "https://open.spotify.com/track/2iLz47TwcEN22gTpbTYiU2"
          },
          {
            "name": "Better Off",
            "artists": [
              "HRRSN"
            ],
            "releaseName": "The War on Empathy",
            "releaseDate": "2018-08-24",
            "releaseType": "single",
            "tempo": 122.013,
            "duration": 425.188,
            "key": 5,
            "mode": 0,
            "uri": "spotify:track:6Cyf6viX3iPuSdJixk2wBI",
            "url": "https://open.spotify.com/track/6Cyf6viX3iPuSdJixk2wBI"
          },
          {
            "name": "Putting The World Back Together",
            "artists": [
              "Alex Rusin"
            ],
            "releaseName": "Cold Winds",
            "releaseDate": "2019-05-31",
            "releaseType": "single",
            "tempo": 123.0,
            "duration": 404.895,
            "key": 5,
            "mode": 1,
            "uri": "spotify:track:6E2zQiWndzKPecyTU2NVmF",
            "url": "https://open.spotify.com/track/6E2zQiWndzKPecyTU2NVmF"
          },
          {
            "name": "Glue",
            "artists": [
              "Bicep"
            ],
            "releaseName": "Bicep",
            "releaseDate": "2017-09-01",
            "releaseType": "album",
            "tempo": 129.983,
            "duration": 269.15,
            "key": 5,
            "mode": 0,
            "uri": "spotify:track:2aJDlirz6v2a4HREki98cP",
            "url": "https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"
          },
          {
            "name": "Night Blooming Jasmine - Rodriguez Jr. Remix Edit",
            "artists": [
              "Eli & Fur",
              "Rodriguez Jr."
            ],
            "releaseName": "Night Blooming Jasmine (Rodriguez Jr. Remix)",
            "releaseDate": "2018-08-10",
            "releaseType": "single",
            "tempo": 120.007,
            "duration": 316.0,
            "key": 6,
            "mode": 0,
            "uri": "spotify:track:4p2huo3cTwy477D3Y9bKWP",
            "url": "https://open.spotify.com/track/4p2huo3cTwy477D3Y9bKWP"
          },
          {
            "name": "Sun",
            "artists": [
              "Gallago"
            ],
            "releaseName": "Anjunadeep The Yearbook 2018",
            "releaseDate": "2018-11-29",
            "releaseType": "compilation",
            "tempo": 122.079,
            "duration": 361.831,
            "key": 6,
            "mode": 0,
            "uri": "spotify:track:2nmbPcReCG7ha6LDD9ZXjQ",
            "url": "https://open.spotify.com/track/2nmbPcReCG7ha6LDD9ZXjQ"
          },
          {
            "name": "For A Moment",
            "artists": [
              "Jazz Do It"
            ],
            "releaseName": "Anjunadeep 10 Sampler: Part 1",
            "releaseDate": "2019-03-01",
            "releaseType": "single",
            "tempo": 121.001,
            "duration": 356.529,
            "key": 7,
            "mode": 0,
            "uri": "spotify:track:01J7GlzTYwAp0kHGwLrHiB",
            "url": "https://open.spotify.com/track/01J7GlzTYwAp0kHGwLrHiB"
          },
          {
            "name": "Parabola",
            "artists": [
              "Hammer"
            ],
            "releaseName": "Parabola",
            "releaseDate": "2019-09-06",
            "releaseType": "single",
            "tempo": 118.979,
            "duration": 344.582,
            "key": 7,
            "mode": 0,
            "uri": "spotify:track:7K7x1pCPGStLSObmidIP1S",
            "url": "https://open.spotify.com/track/7K7x1pCPGStLSObmidIP1S"
          },
          {
            "name": "New Sky - Edu Imbernon Remix",
            "artists": [
              "RÜFÜS DU SOL",
              "Edu Imbernon"
            ],
            "releaseName": "SOLACE REMIXED",
            "releaseDate": "2019-09-06",
            "releaseType": "album",
            "tempo": 123.996,
            "duration": 535.975,
            "key": 8,
            "mode": 0,
            "uri": "spotify:track:1wtxI9YhL1t4yDIwGAFljP",
            "url": "https://open.spotify.com/track/1wtxI9YhL1t4yDIwGAFljP"
          },
          {
            "name": "Chrysalis",
            "artists": [
              "Clawz SG",
              "Mashk"
            ],
            "releaseName": "Chrysalis",
            "releaseDate": "2018-08-20",
            "releaseType": "single",
            "tempo": 123.008,
            "duration": 456.081,
            "key": 8,
            "mode": 1,
            "uri": "spotify:track:14F7gfsIpA74Hb6n4eOhI6",
            "url": "https://open.spotify.com/track/14F7gfsIpA74Hb6n4eOhI6"
          },
          {
            "name": "Dapple - Extended Mix",
            "artists": [
              "Jody Wisternoff",
              "James Grant"
            ],
            "releaseName": "Dapple",
            "releaseDate": "2019-02-26",
            "releaseType": "single",
            "tempo": 119.999,
            "duration": 368.645,
            "key": 8,
            "mode": 0,
            "uri": "spotify:track:3FJQLY5VINvftnU1mo0bYW",
            "url": "https://open.spotify.com/track/3FJQLY5VINvftnU1mo0bYW"
          },
          {
            "name": "When The Sun Goes Down",
            "artists": [
              "Braxton"
            ],
            "releaseName": "When The Sun Goes Down",
            "releaseDate": "2019-02-15",
            "releaseType": "single",
            "tempo": 129.019,
            "duration": 284.147,
            "key": 11,
            "mode": 0,
            "uri": "spotify:track:11kQ6z0DWHrhV7Z1aX4vW6",
            "url": "https://open.spotify.com/track/11kQ6z0DWHrhV7Z1aX4vW6"
          },
          {
            "name": "Closer",
            "artists": [
              "ARTBAT",
              "WhoMadeWho"
            ],
            "releaseName": "Montserrat / Closer",
            "releaseDate": "2019-04-19",
            "releaseType": "single",
            "tempo": 125.005,
            "duration": 460.8,
            "key": 11,
            "mode": 0,
            "uri": "spotify:track:1rGnpPG0QHfqjDgM8cIf4A",
            "url": "https://open.spotify.com/track/1rGnpPG0QHfqjDgM8cIf4A"
          },
          {
            "name": "Play - Original Mix",
            "artists": [
              "Ben Ashton"
            ],
            "releaseName": "Déepalma Ibiza 2016 (Compiled by Yves Murasca, Rosario Galati, Holter & Mogyoro)",
            "releaseDate": "2016-07-29",
            "releaseType": "compilation",
            "tempo": 121.986,
            "duration": 342.0,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:69qHUwR2CGfcNvCgUI1rxv",
            "url": "https://open.spotify.com/track/69qHUwR2CGfcNvCgUI1rxv"
          },
          {
            "name": "Rea",
            "artists": [
              "Thomas Schwartz",
              "Fausto Fanizza"
            ],
            "releaseName": "Rea EP",
            "releaseDate": "2016-07-08",
            "releaseType": "single",
            "tempo": 122.007,
            "duration": 399.344,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:1LXU3usSPozHFvxOGePjW7",
            "url": "https://open.spotify.com/track/1LXU3usSPozHFvxOGePjW7"
          },
          {
            "name": "Silent North",
            "artists": [
              "Jonas Saalbach"
            ],
            "releaseName": "Reminiscence",
            "releaseDate": "2019-02-22",
            "releaseType": "album",
            "tempo": 121.997,
            "duration": 475.918,
            "key": 12,
            "mode": 1,
            "uri": "spotify:track:1zlNGW2543jWE4iHNaBMSI",
            "url": "https://open.spotify.com/track/1zlNGW2543jWE4iHNaBMSI"
          },
          {
            "name": "Frequent Tendencies",
            "artists": [
              "GHEIST"
            ],
            "releaseName": "Frequent Tendencies",
            "releaseDate": "2018-06-15",
            "releaseType": "single",
            "tempo": 123.004,
            "duration": 402.133,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:1D986c2Uu3P20kDR31NrhQ",
            "url": "https://open.spotify.com/track/1D986c2Uu3P20kDR31NrhQ"
          },
          {
            "name": "No Place - Will Clarke Remix",
            "artists": [
              "RÜFÜS DU SOL",
              "Will Clarke"
            ],
            "releaseName": "SOLACE REMIXES VOL. 3",
            "releaseDate": "2018-12-14",
            "releaseType": "single",
            "tempo": 125.0,
            "duration": 358.145,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:3zFj4nAh7Tlg50wAmuWPnz",
            "url": "https://open.spotify.com/track/3zFj4nAh7Tlg50wAmuWPnz"
          },
          {
            "name": "Expanse",
            "artists": [
              "Dezza",
              "Kolonie"
            ],
            "releaseName": "Expanse",
            "releaseDate": "2018-11-16",
            "releaseType": "single",
            "tempo": 125.046,
            "duration": 222.73,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:4nSKOvcIJ30Z7bpdyr4KfT",
            "url": "https://open.spotify.com/track/4nSKOvcIJ30Z7bpdyr4KfT"
          },
          {
            "name": "Vapours",
            "artists": [
              "Hot Since 82",
              "Alex Mills"
            ],
            "releaseName": "8-track",
            "releaseDate": "2019-07-26",
            "releaseType": "album",
            "tempo": 121.997,
            "duration": 358.033,
            "key": 12,
            "mode": 0,
            "uri": "spotify:track:1DgfoSbmoJtiRVzuJ9iFK7",
            "url": "https://open.spotify.com/track/1DgfoSbmoJtiRVzuJ9iFK7"
          },
          {
            "name": "Jewel",
            "artists": [
              "Clawz SG"
            ],
            "releaseName": "Inner Symphony Gold 2018",
            "releaseDate": "2018-12-17",
            "releaseType": "album",
            "tempo": 121.961,
            "duration": 450.932,
            "key": 2,
            "mode": 0,
            "uri": "spotify:track:3IHZRkwngHQtoJWlodS4OT",
            "url": "https://open.spotify.com/track/3IHZRkwngHQtoJWlodS4OT"
          },
          {
            "name": "Was It Love",
            "artists": [
              "Liquid Phonk"
            ],
            "releaseName": "Compost Black Label #131 - Was It Love EP",
            "releaseDate": "2016-05-06",
            "releaseType": "single",
            "tempo": 115.013,
            "duration": 476.062,
            "key": 3,
            "mode": 0,
            "uri": "spotify:track:3ifpvzLRWuQD6aYg8otq9p",
            "url": "https://open.spotify.com/track/3ifpvzLRWuQD6aYg8otq9p"
          },
          {
            "name": "Easy Drifter - Claude VonStroke Full Length Mix",
            "artists": [
              "Booka Shade",
              "Claude VonStroke"
            ],
            "releaseName": "Cut the Strings - Album Remixes 1",
            "releaseDate": "2018-05-25",
            "releaseType": "single",
            "tempo": 124.996,
            "duration": 406.535,
            "key": 3,
            "mode": 0,
            "uri": "spotify:track:6qs82ViATNBe87qKIDpXgp",
            "url": "https://open.spotify.com/track/6qs82ViATNBe87qKIDpXgp"
          },
          {
            "name": "No War - Rampa Remix",
            "artists": [
              "Âme",
              "Rampa"
            ],
            "releaseName": "Dream House Remixes Part I",
            "releaseDate": "2019-08-09",
            "releaseType": "single",
            "tempo": 122.998,
            "duration": 439.227,
            "key": 2,
            "mode": 0,
            "uri": "spotify:track:7DkabQv05RGD0Pj9zFhKKG",
            "url": "https://open.spotify.com/track/7DkabQv05RGD0Pj9zFhKKG"
          },
          {
            "name": "Arrival",
            "artists": [
              "GHEIST"
            ],
            "releaseName": "Arrival EP",
            "releaseDate": "2019-07-26",
            "releaseType": "single",
            "tempo": 123.006,
            "duration": 394.15,
            "key": 2,
            "mode": 0,
            "uri": "spotify:track:0GpCIG97O5vQupnxMRDqjR",
            "url": "https://open.spotify.com/track/0GpCIG97O5vQupnxMRDqjR"
          },
          {
            "name": "Pathos",
            "artists": [
              "Mashk"
            ],
            "releaseName": "Pathos",
            "releaseDate": "2017-08-11",
            "releaseType": "single",
            "tempo": 120.0,
            "duration": 523.192,
            "key": 2,
            "mode": 0,
            "uri": "spotify:track:7avI5luJYVkNUk6GObVTLd",
            "url": "https://open.spotify.com/track/7avI5luJYVkNUk6GObVTLd"
          },
          {
            "name": "In Heaven",
            "artists": [
              "Dustin Nantais",
              "Paul Hazendonk"
            ],
            "releaseName": "Novel Creations, Vol. 1",
            "releaseDate": "2017-03-17",
            "releaseType": "compilation",
            "tempo": 123.018,
            "duration": 411.773,
            "key": 2,
            "mode": 1,
            "uri": "spotify:track:6AjUFYqP7oVTUX47cVJins",
            "url": "https://open.spotify.com/track/6AjUFYqP7oVTUX47cVJins"
          },
          {
            "name": "Juniper - Braxton Remix",
            "artists": [
              "Dezza",
              "Braxton"
            ],
            "releaseName": "Juniper (Braxton Remix)",
            "releaseDate": "2019-08-09",
            "releaseType": "single",
            "tempo": 123.983,
            "duration": 300.107,
            "key": 1,
            "mode": 1,
            "uri": "spotify:track:0trMaBLzyGmFNzk0hfFi2m",
            "url": "https://open.spotify.com/track/0trMaBLzyGmFNzk0hfFi2m"
          },
          {
            "name": "Points Beyond",
            "artists": [
              "Cubicolor"
            ],
            "releaseName": "Points Beyond",
            "releaseDate": "2019-11-15",
            "releaseType": "single",
            "tempo": 115.999,
            "duration": 326.5,
            "key": 1,
            "mode": 1,
            "uri": "spotify:track:23N0RehCjU9KC7WfEzxdgJ",
            "url": "https://open.spotify.com/track/23N0RehCjU9KC7WfEzxdgJ"
          },
          {
            "name": "Mio - Clawz SG Remix",
            "artists": [
              "Ceas",
              "Clawz SG"
            ],
            "releaseName": "Inner Symphony Gold 2019",
            "releaseDate": "2020-01-17",
            "releaseType": "album",
            "tempo": 123.009,
            "duration": 415.708,
            "key": 2,
            "mode": 1,
            "uri": "spotify:track:4d9gJg1ZKFPoHwOrtMPx9v",
            "url": "https://open.spotify.com/track/4d9gJg1ZKFPoHwOrtMPx9v"
          },
          {
            "name": "Dune Suave",
            "artists": [
              "Einmusik"
            ],
            "releaseName": "Einmusik / Lake Avalon",
            "releaseDate": "2019-11-29",
            "releaseType": "single",
            "tempo": 124.003,
            "duration": 496.563,
            "key": 2,
            "mode": 1,
            "uri": "spotify:track:3RkZM4hPoN6AupH4Ir1RAO",
            "url": "https://open.spotify.com/track/3RkZM4hPoN6AupH4Ir1RAO"
          },
          {
            "name": "Lazy Dog",
            "artists": [
              "Several Definitions",
              "Marc DePulse"
            ],
            "releaseName": "2019 Day Collection",
            "releaseDate": "2019-12-23",
            "releaseType": "compilation",
            "tempo": 107.991,
            "duration": 375.166,
            "key": 2,
            "mode": 1,
            "uri": "spotify:track:6fzl8LVinIXYtWGORlDWUA",
            "url": "https://open.spotify.com/track/6fzl8LVinIXYtWGORlDWUA"
          },
          {
            "name": "Lissome",
            "artists": [
              "Jobe"
            ],
            "releaseName": "Jobe Presents Authentic Steyoyoke #012",
            "releaseDate": "2018-03-26",
            "releaseType": "album",
            "tempo": 121.004,
            "duration": 510.92,
            "key": 3,
            "mode": 1,
            "uri": "spotify:track:5FzKaAOUwjHT2OK8LUOllY",
            "url": "https://open.spotify.com/track/5FzKaAOUwjHT2OK8LUOllY"
          },
          {
            "name": "Paradox - Extended Mix",
            "artists": [
              "Diversion",
              "Fynn"
            ],
            "releaseName": "The Sound Of Electronica, Vol. 12",
            "releaseDate": "2018-08-13",
            "releaseType": "compilation",
            "tempo": 129.991,
            "duration": 341.25,
            "key": 4,
            "mode": 0,
            "uri": "spotify:track:69xhL5Ug4sgTDCgAVyHynv",
            "url": "https://open.spotify.com/track/69xhL5Ug4sgTDCgAVyHynv"
          },
          {
            "name": "Nightfly - Extended Mix",
            "artists": [
              "Arm In Arm"
            ],
            "releaseName": "Nightfly",
            "releaseDate": "2018-09-07",
            "releaseType": "single",
            "tempo": 124.013,
            "duration": 422.158,
            "key": 4,
            "mode": 0,
            "uri": "spotify:track:0YzPsrhmSsA1R7L58x5id8",
            "url": "https://open.spotify.com/track/0YzPsrhmSsA1R7L58x5id8"
          },
          {
            "name": "Venere",
            "artists": [
              "BOg",
              "GHEIST"
            ],
            "releaseName": "Venere",
            "releaseDate": "2019-07-08",
            "releaseType": "single",
            "tempo": 124.991,
            "duration": 404.956,
            "key": 5,
            "mode": 0,
            "uri": "spotify:track:7rrrjYORC2IAtjA7NCQLPb",
            "url": "https://open.spotify.com/track/7rrrjYORC2IAtjA7NCQLPb"
          }
        ]
      }
    ]
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
