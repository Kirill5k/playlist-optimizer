package io.kirill.playlistoptimizer.domain.playlist

import io.kirill.playlistoptimizer.domain.playlist.Key.*

import java.time.LocalDate
import scala.concurrent.duration.*

object PlaylistBuilder {

  val defaultRelease = Release("Awesome mix", "compilation", Some(LocalDate.of(2020, 3, 17)), None)

  def playlist: Playlist = Playlist(
    "Mel",
    Some("Melodic deep house and techno songs"),
    Vector(
      Track(
        SongDetails("Glue", List("Bicep"), defaultRelease, None),
        AudioDetails(129.983, 269150.milliseconds, CMinor, 0.613, 0.807),
        SourceDetails("spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))
      ),
      Track(
        SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), defaultRelease, None),
        AudioDetails(123.018, 411773.milliseconds, FSharpMajor, 0.0, 0.0),
        SourceDetails("spotify:track:6AjUFYqP7oVTUX47cVJins", Some("https://open.spotify.com/track/6AjUFYqP7oVTUX47cVJins"))
      ),
      Track(
        SongDetails("New Sky - Edu Imbernon Remix", List("RÜFÜS DU SOL", "Edu Imbernon"), defaultRelease, None),
        AudioDetails(123.996, 535975.milliseconds, AMinor, 0.0, 0.0),
        SourceDetails("spotify:track:1wtxI9YhL1t4yDIwGAFljP", Some("https://open.spotify.com/track/1wtxI9YhL1t4yDIwGAFljP"))
      ),
      Track(
        SongDetails("Play - Original Mix", List("Ben Ashton"), defaultRelease, None),
        AudioDetails(121.986, 342.seconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:69qHUwR2CGfcNvCgUI1rxv", Some("https://open.spotify.com/track/69qHUwR2CGfcNvCgUI1rxv"))
      ),
      Track(
        SongDetails("Was It Love", List("Liquid Phonk"), defaultRelease, None),
        AudioDetails(115.013, 476062.milliseconds, BFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:3ifpvzLRWuQD6aYg8otq9p", Some("https://open.spotify.com/track/3ifpvzLRWuQD6aYg8otq9p"))
      ),
      Track(
        SongDetails("No Place - Will Clarke Remix", List("RÜFÜS DU SOL", "Will Clarke"), defaultRelease, None),
        AudioDetails(125.0, 358145.milliseconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:3zFj4nAh7Tlg50wAmuWPnz", Some("https://open.spotify.com/track/3zFj4nAh7Tlg50wAmuWPnz"))
      ),
      Track(
        SongDetails("Rea", List("Thomas Schwartz", "Fausto Fanizza"), defaultRelease, None),
        AudioDetails(122.007, 399344.milliseconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:1LXU3usSPozHFvxOGePjW7", Some("https://open.spotify.com/track/1LXU3usSPozHFvxOGePjW7"))
      ),
      Track(
        SongDetails("Closer", List("ARTBAT", "WhoMadeWho"), defaultRelease, None),
        AudioDetails(125.005, 460800.milliseconds, FSharpMinor, 0.0, 0.0),
        SourceDetails("spotify:track:1rGnpPG0QHfqjDgM8cIf4A", Some("https://open.spotify.com/track/1rGnpPG0QHfqjDgM8cIf4A"))
      ),
      Track(
        SongDetails("Santiago", List("YNOT"), defaultRelease, None),
        AudioDetails(122.011, 310943.milliseconds, CMajor, 0.0, 0.0),
        SourceDetails("spotify:track:7bWyKnNHQaMxgDqyxJHyyE", Some("https://open.spotify.com/track/7bWyKnNHQaMxgDqyxJHyyE"))
      ),
      Track(
        SongDetails("Shift", List("ALMA (GER)"), defaultRelease, None),
        AudioDetails(120.998, 404635.milliseconds, FMajor, 0.0, 0.0),
        SourceDetails("spotify:track:4R9ElixwzY1W9gNZoMcixQ", Some("https://open.spotify.com/track/4R9ElixwzY1W9gNZoMcixQ"))
      ),
      Track(
        SongDetails("No War - Rampa Remix", List("Âme", "Rampa"), defaultRelease, None),
        AudioDetails(122.998, 439227.milliseconds, EFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:7DkabQv05RGD0Pj9zFhKKG", Some("https://open.spotify.com/track/7DkabQv05RGD0Pj9zFhKKG"))
      ),
      Track(
        SongDetails("Nightfly - Extended Mix", List("Arm In Arm"), defaultRelease, None),
        AudioDetails(124.013, 422158.milliseconds, FMinor, 0.0, 0.0),
        SourceDetails("spotify:track:0YzPsrhmSsA1R7L58x5id8", Some("https://open.spotify.com/track/0YzPsrhmSsA1R7L58x5id8"))
      ),
      Track(
        SongDetails("Easy Drifter - Claude VonStroke Full Length Mix", List("Booka Shade", "Claude VonStroke"), defaultRelease, None),
        AudioDetails(124.996, 406535.milliseconds, BFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:6qs82ViATNBe87qKIDpXgp", Some("https://open.spotify.com/track/6qs82ViATNBe87qKIDpXgp"))
      ),
      Track(
        SongDetails("When The Sun Goes Down", List("Braxton"), defaultRelease, None),
        AudioDetails(129.019, 284147.milliseconds, FSharpMinor, 0.0, 0.0),
        SourceDetails("spotify:track:11kQ6z0DWHrhV7Z1aX4vW6", Some("https://open.spotify.com/track/11kQ6z0DWHrhV7Z1aX4vW6"))
      ),
      Track(
        SongDetails("Mio - Clawz SG Remix", List("Ceas", "Clawz SG"), defaultRelease, None),
        AudioDetails(123.009, 415708.milliseconds, FSharpMajor, 0.0, 0.0),
        SourceDetails("spotify:track:4d9gJg1ZKFPoHwOrtMPx9v", Some("https://open.spotify.com/track/4d9gJg1ZKFPoHwOrtMPx9v"))
      ),
      Track(
        SongDetails("Jewel", List("Clawz SG"), defaultRelease, None),
        AudioDetails(121.961, 450932.milliseconds, EFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:3IHZRkwngHQtoJWlodS4OT", Some("https://open.spotify.com/track/3IHZRkwngHQtoJWlodS4OT"))
      ),
      Track(
        SongDetails("Expanse", List("Dezza", "Kolonie"), defaultRelease, None),
        AudioDetails(125.046, 222730.milliseconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:4nSKOvcIJ30Z7bpdyr4KfT", Some("https://open.spotify.com/track/4nSKOvcIJ30Z7bpdyr4KfT"))
      ),
      Track(
        SongDetails("Putting The World Back Together", List("Alex Rusin"), defaultRelease, None),
        AudioDetails(123.0, 404895.milliseconds, EFlatMajor, 0.0, 0.0),
        SourceDetails("spotify:track:6E2zQiWndzKPecyTU2NVmF", Some("https://open.spotify.com/track/6E2zQiWndzKPecyTU2NVmF"))
      ),
      Track(
        SongDetails("Mirage", List("Anden"), defaultRelease, None),
        AudioDetails(124.032, 308736.milliseconds, BFlatMajor, 0.0, 0.0),
        SourceDetails("spotify:track:4Rys9v6OjDVCbW7JOdkQY7", Some("https://open.spotify.com/track/4Rys9v6OjDVCbW7JOdkQY7"))
      ),
      Track(
        SongDetails("Venere", List("BOg", "GHEIST"), defaultRelease, None),
        AudioDetails(124.991, 404956.milliseconds, CMinor, 0.0, 0.0),
        SourceDetails("spotify:track:7rrrjYORC2IAtjA7NCQLPb", Some("https://open.spotify.com/track/7rrrjYORC2IAtjA7NCQLPb"))
      ),
      Track(
        SongDetails("Gwendoline - Original Mix", List("Clawz SG"), defaultRelease, None),
        AudioDetails(124.012, 452913.milliseconds, BMinor, 0.0, 0.0),
        SourceDetails("spotify:track:6heLeWInDSLU9wdrKhx2l5", Some("https://open.spotify.com/track/6heLeWInDSLU9wdrKhx2l5"))
      ),
      Track(
        SongDetails("Points Beyond", List("Cubicolor"), defaultRelease, None),
        AudioDetails(115.999, 326500.milliseconds, BMajor, 0.0, 0.0),
        SourceDetails("spotify:track:23N0RehCjU9KC7WfEzxdgJ", Some("https://open.spotify.com/track/23N0RehCjU9KC7WfEzxdgJ"))
      ),
      Track(
        SongDetails("Indenait", List("Edu Imbernon"), defaultRelease, None),
        AudioDetails(121.005, 577750.milliseconds, EMinor, 0.0, 0.0),
        SourceDetails("spotify:track:5wC5dAdAQPzeuhmITZVAiO", Some("https://open.spotify.com/track/5wC5dAdAQPzeuhmITZVAiO"))
      ),
      Track(
        SongDetails("Dune Suave", List("Einmusik"), defaultRelease, None),
        AudioDetails(124.003, 496563.milliseconds, FSharpMajor, 0.0, 0.0),
        SourceDetails("spotify:track:3RkZM4hPoN6AupH4Ir1RAO", Some("https://open.spotify.com/track/3RkZM4hPoN6AupH4Ir1RAO"))
      ),
      Track(
        SongDetails("Night Blooming Jasmine - Rodriguez Jr. Remix Edit", List("Eli & Fur", "Rodriguez Jr."), defaultRelease, None),
        AudioDetails(120.007, 316.seconds, GMinor, 0.0, 0.0),
        SourceDetails("spotify:track:4p2huo3cTwy477D3Y9bKWP", Some("https://open.spotify.com/track/4p2huo3cTwy477D3Y9bKWP"))
      ),
      Track(
        SongDetails("Juniper - Braxton Remix", List("Dezza", "Braxton"), defaultRelease, None),
        AudioDetails(123.983, 300107.milliseconds, BMajor, 0.0, 0.0),
        SourceDetails("spotify:track:0trMaBLzyGmFNzk0hfFi2m", Some("https://open.spotify.com/track/0trMaBLzyGmFNzk0hfFi2m"))
      ),
      Track(
        SongDetails("Paradox - Extended Mix", List("Diversion", "Fynn"), defaultRelease, None),
        AudioDetails(129.991, 341250.milliseconds, FMinor, 0.0, 0.0),
        SourceDetails("spotify:track:69xhL5Ug4sgTDCgAVyHynv", Some("https://open.spotify.com/track/69xhL5Ug4sgTDCgAVyHynv"))
      ),
      Track(
        SongDetails("Sun", List("Gallago"), defaultRelease, None),
        AudioDetails(122.079, 361831.milliseconds, GMinor, 0.0, 0.0),
        SourceDetails("spotify:track:2nmbPcReCG7ha6LDD9ZXjQ", Some("https://open.spotify.com/track/2nmbPcReCG7ha6LDD9ZXjQ"))
      ),
      Track(
        SongDetails("Arrival", List("GHEIST"), defaultRelease, None),
        AudioDetails(123.006, 394150.milliseconds, EFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:0GpCIG97O5vQupnxMRDqjR", Some("https://open.spotify.com/track/0GpCIG97O5vQupnxMRDqjR"))
      ),
      Track(
        SongDetails("Frequent Tendencies", List("GHEIST"), defaultRelease, None),
        AudioDetails(123.004, 402133.milliseconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:1D986c2Uu3P20kDR31NrhQ", Some("https://open.spotify.com/track/1D986c2Uu3P20kDR31NrhQ"))
      ),
      Track(
        SongDetails("You Caress", List("Giorgia Angiuli", "Lake Avalon"), defaultRelease, None),
        AudioDetails(124.0, 503549.milliseconds, GMajor, 0.0, 0.0),
        SourceDetails("spotify:track:7FyQCtWo4BYc9RdpEFcrSp", Some("https://open.spotify.com/track/7FyQCtWo4BYc9RdpEFcrSp"))
      ),
      Track(
        SongDetails("Parabola", List("Hammer"), defaultRelease, None),
        AudioDetails(118.979, 344582.milliseconds, DMinor, 0.0, 0.0),
        SourceDetails("spotify:track:7K7x1pCPGStLSObmidIP1S", Some("https://open.spotify.com/track/7K7x1pCPGStLSObmidIP1S"))
      ),
      Track(
        SongDetails("Vapours", List("Hot Since 82", "Alex Mills"), defaultRelease, None),
        AudioDetails(121.997, 358033.milliseconds, DFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:1DgfoSbmoJtiRVzuJ9iFK7", Some("https://open.spotify.com/track/1DgfoSbmoJtiRVzuJ9iFK7"))
      ),
      Track(
        SongDetails("Better Off", List("HRRSN"), defaultRelease, None),
        AudioDetails(122.013, 425188.milliseconds, CMinor, 0.0, 0.0),
        SourceDetails("spotify:track:6Cyf6viX3iPuSdJixk2wBI", Some("https://open.spotify.com/track/6Cyf6viX3iPuSdJixk2wBI"))
      ),
      Track(
        SongDetails("Into The Light", List("James Trystan"), defaultRelease, None),
        AudioDetails(123.01, 409600.milliseconds, CMajor, 0.0, 0.0),
        SourceDetails("spotify:track:79r2pk6jb2x18D5bLyAiT3", Some("https://open.spotify.com/track/79r2pk6jb2x18D5bLyAiT3"))
      ),
      Track(
        SongDetails("For A Moment", List("Jazz Do It"), defaultRelease, None),
        AudioDetails(121.001, 356529.milliseconds, DMinor, 0.0, 0.0),
        SourceDetails("spotify:track:01J7GlzTYwAp0kHGwLrHiB", Some("https://open.spotify.com/track/01J7GlzTYwAp0kHGwLrHiB"))
      ),
      Track(
        SongDetails("Lissome", List("Jobe"), defaultRelease, None),
        AudioDetails(121.004, 510920.milliseconds, DFlatMajor, 0.0, 0.0),
        SourceDetails("spotify:track:5FzKaAOUwjHT2OK8LUOllY", Some("https://open.spotify.com/track/5FzKaAOUwjHT2OK8LUOllY"))
      ),
      Track(
        SongDetails("Dapple - Extended Mix", List("Jody Wisternoff", "James Grant"), defaultRelease, None),
        AudioDetails(119.999, 368645.milliseconds, AMinor, 0.0, 0.0),
        SourceDetails("spotify:track:3FJQLY5VINvftnU1mo0bYW", Some("https://open.spotify.com/track/3FJQLY5VINvftnU1mo0bYW"))
      ),
      Track(
        SongDetails("April", List("Jonas Saalbach"), defaultRelease, None),
        AudioDetails(123.988, 498938.milliseconds, DMajor, 0.0, 0.0),
        SourceDetails("spotify:track:6v4ni85b9wX6IavE1b3Muf", Some("https://open.spotify.com/track/6v4ni85b9wX6IavE1b3Muf"))
      ),
      Track(
        SongDetails("Silent North", List("Jonas Saalbach"), defaultRelease, None),
        AudioDetails(121.997, 475918.milliseconds, EMajor, 0.0, 0.0),
        SourceDetails("spotify:track:1zlNGW2543jWE4iHNaBMSI", Some("https://open.spotify.com/track/1zlNGW2543jWE4iHNaBMSI"))
      ),
      Track(
        SongDetails("Twisted Shapes", List("Jonas Saalbach", "Chris McCarthy"), defaultRelease, None),
        AudioDetails(120.997, 493008.milliseconds, FMajor, 0.0, 0.0),
        SourceDetails("spotify:track:1DafZ2A2bRbXfLA7KVVYX7", Some("https://open.spotify.com/track/1DafZ2A2bRbXfLA7KVVYX7"))
      ),
      Track(
        SongDetails("Human", List("Julian Wassermann"), defaultRelease, None),
        AudioDetails(124.997, 422197.milliseconds, CMajor, 0.0, 0.0),
        SourceDetails("spotify:track:7duQlpZgEexH9E4alUXwhe", Some("https://open.spotify.com/track/7duQlpZgEexH9E4alUXwhe"))
      ),
      Track(
        SongDetails("Rapture", List("Lunar Plane"), defaultRelease, None),
        AudioDetails(120.002, 452380.milliseconds, GMinor, 0.0, 0.0),
        SourceDetails("spotify:track:2iLz47TwcEN22gTpbTYiU2", Some("https://open.spotify.com/track/2iLz47TwcEN22gTpbTYiU2"))
      ),
      Track(
        SongDetails("Lazy Dog", List("Several Definitions", "Marc DePulse"), defaultRelease, None),
        AudioDetails(107.991, 375166.milliseconds, FSharpMajor, 0.0, 0.0),
        SourceDetails("spotify:track:6fzl8LVinIXYtWGORlDWUA", Some("https://open.spotify.com/track/6fzl8LVinIXYtWGORlDWUA"))
      ),
      Track(
        SongDetails("Pathos", List("Mashk"), defaultRelease, None),
        AudioDetails(120.0, 523192.milliseconds, EFlatMinor, 0.0, 0.0),
        SourceDetails("spotify:track:7avI5luJYVkNUk6GObVTLd", Some("https://open.spotify.com/track/7avI5luJYVkNUk6GObVTLd"))
      ),
      Track(
        SongDetails("Chrysalis", List("Clawz SG", "Mashk"), defaultRelease, None),
        AudioDetails(123.008, 456081.milliseconds, CMajor, 0.0, 0.0),
        SourceDetails("spotify:track:14F7gfsIpA74Hb6n4eOhI6", Some("https://open.spotify.com/track/14F7gfsIpA74Hb6n4eOhI6"))
      )
    ),
    PlaylistSource.Spotify
  )
}
