package io.kirill.playlistoptimizer.core.spotify.clients

import java.time.LocalDate

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.Key._
import io.kirill.playlistoptimizer.core.playlist.{AudioDetails, Playlist, PlaylistBuilder, PlaylistSource, SongDetails, SourceDetails, Track}
import io.kirill.playlistoptimizer.core.playlist._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, Method}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class SpotifyApiClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  val token = "token-5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"

  "A SpotifyClient" - {

    "create new playlist" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "users", "user-1", "playlists")) && r.method == Method.POST && r.body.toString.contains("""{"name":"Mel","description":"Melodic deep house and techno songs","public":true,"collaborative":false}""") =>
            Response.ok(json("spotify/flow/create/1-new-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "7d2D2S200NyUE5KYs80PwO", "tracks")) && r.method == Method.POST =>
            Response.ok(json("spotify/flow/create/2-add-tracks.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient().createPlaylist(token, "user-1", PlaylistBuilder.playlist)

      response.asserting(_ must be (()))
    }

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists")) => Response.ok(json("spotify/flow/find/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "7npAZEYwEwV2JV7XX2n3wq")) => Response.ok(json("spotify/flow/find/3-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) => Response.ok(json(s"spotify/flow/find/4-audio-features-${r.uri.path.last}.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient().findPlaylistByName(token, "mel")

      response.asserting(_ must be(Playlist("Mel", Some("Melodic deep house and techno songs"), Vector(
        Track(SongDetails("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album")), AudioDetails(129.983, 269150 milliseconds, CMinor),SourceDetails("spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))),
        Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1"), Some(LocalDate.of(2017, 3, 17)), Some("compilation")), AudioDetails(123.018, 411773 milliseconds, FSharpMajor),SourceDetails("spotify:track:6AjUFYqP7oVTUX47cVJins", Some("https://open.spotify.com/track/6AjUFYqP7oVTUX47cVJins"))),
        Track(SongDetails("New Sky - Edu Imbernon Remix", List("RÜFÜS DU SOL", "Edu Imbernon"), Some("SOLACE REMIXED"), Some(LocalDate.of(2019, 9, 6)), Some("album")), AudioDetails(123.996, 535975 milliseconds, AMinor),SourceDetails("spotify:track:1wtxI9YhL1t4yDIwGAFljP", Some("https://open.spotify.com/track/1wtxI9YhL1t4yDIwGAFljP"))),
        Track(SongDetails("Play - Original Mix", List("Ben Ashton"), Some("Déepalma Ibiza 2016 (Compiled by Yves Murasca, Rosario Galati, Holter & Mogyoro)"), Some(LocalDate.of(2016, 7, 29)), Some("compilation")), AudioDetails(121.986, 342 seconds, DFlatMinor),SourceDetails("spotify:track:69qHUwR2CGfcNvCgUI1rxv", Some("https://open.spotify.com/track/69qHUwR2CGfcNvCgUI1rxv"))),
        Track(SongDetails("Was It Love", List("Liquid Phonk"), Some("Compost Black Label #131 - Was It Love EP"), Some(LocalDate.of(2016, 5, 6)), Some("single")), AudioDetails(115.013, 476062 milliseconds, BFlatMinor),SourceDetails("spotify:track:3ifpvzLRWuQD6aYg8otq9p", Some("https://open.spotify.com/track/3ifpvzLRWuQD6aYg8otq9p"))),
        Track(SongDetails("No Place - Will Clarke Remix", List("RÜFÜS DU SOL", "Will Clarke"), Some("SOLACE REMIXES VOL. 3"), Some(LocalDate.of(2018, 12, 14)), Some("single")), AudioDetails(125.0, 358145 milliseconds, DFlatMinor),SourceDetails("spotify:track:3zFj4nAh7Tlg50wAmuWPnz", Some("https://open.spotify.com/track/3zFj4nAh7Tlg50wAmuWPnz"))),
        Track(SongDetails("Rea", List("Thomas Schwartz", "Fausto Fanizza"), Some("Rea EP"), Some(LocalDate.of(2016, 7, 8)), Some("single")), AudioDetails(122.007, 399344 milliseconds, DFlatMinor),SourceDetails("spotify:track:1LXU3usSPozHFvxOGePjW7", Some("https://open.spotify.com/track/1LXU3usSPozHFvxOGePjW7"))),
        Track(SongDetails("Closer", List("ARTBAT", "WhoMadeWho"), Some("Montserrat / Closer"), Some(LocalDate.of(2019, 4, 19)), Some("single")), AudioDetails(125.005, 460800 milliseconds, FSharpMinor),SourceDetails("spotify:track:1rGnpPG0QHfqjDgM8cIf4A", Some("https://open.spotify.com/track/1rGnpPG0QHfqjDgM8cIf4A"))),
        Track(SongDetails("Santiago", List("YNOT"), Some("U & I"), Some(LocalDate.of(2016, 6, 3)), Some("single")), AudioDetails(122.011, 310943 milliseconds, CMajor),SourceDetails("spotify:track:7bWyKnNHQaMxgDqyxJHyyE", Some("https://open.spotify.com/track/7bWyKnNHQaMxgDqyxJHyyE"))),
        Track(SongDetails("Shift", List("ALMA (GER)"), Some("Timeshift"), Some(LocalDate.of(2018, 6, 8)), Some("single")), AudioDetails(120.998, 404635 milliseconds, FMajor),SourceDetails("spotify:track:4R9ElixwzY1W9gNZoMcixQ", Some("https://open.spotify.com/track/4R9ElixwzY1W9gNZoMcixQ"))),
        Track(SongDetails("No War - Rampa Remix", List("Âme", "Rampa"), Some("Dream House Remixes Part I"), Some(LocalDate.of(2019, 8, 9)), Some("single")), AudioDetails(122.998, 439227 milliseconds, EFlatMinor),SourceDetails("spotify:track:7DkabQv05RGD0Pj9zFhKKG", Some("https://open.spotify.com/track/7DkabQv05RGD0Pj9zFhKKG"))),
        Track(SongDetails("Nightfly - Extended Mix", List("Arm In Arm"), Some("Nightfly"), Some(LocalDate.of(2018, 9, 7)), Some("single")), AudioDetails(124.013, 422158 milliseconds, FMinor),SourceDetails("spotify:track:0YzPsrhmSsA1R7L58x5id8", Some("https://open.spotify.com/track/0YzPsrhmSsA1R7L58x5id8"))),
        Track(SongDetails("Easy Drifter - Claude VonStroke Full Length Mix", List("Booka Shade", "Claude VonStroke"), Some("Cut the Strings - Album Remixes 1"), Some(LocalDate.of(2018, 5, 25)), Some("single")), AudioDetails(124.996, 406535 milliseconds, BFlatMinor),SourceDetails("spotify:track:6qs82ViATNBe87qKIDpXgp", Some("https://open.spotify.com/track/6qs82ViATNBe87qKIDpXgp"))),
        Track(SongDetails("When The Sun Goes Down", List("Braxton"), Some("When The Sun Goes Down"), Some(LocalDate.of(2019, 2, 15)), Some("single")), AudioDetails(129.019, 284147 milliseconds, FSharpMinor),SourceDetails("spotify:track:11kQ6z0DWHrhV7Z1aX4vW6", Some("https://open.spotify.com/track/11kQ6z0DWHrhV7Z1aX4vW6"))),
        Track(SongDetails("Mio - Clawz SG Remix", List("Ceas", "Clawz SG"), Some("Inner Symphony Gold 2019"), Some(LocalDate.of(2020, 1, 17)), Some("album")), AudioDetails(123.009, 415708 milliseconds, FSharpMajor),SourceDetails("spotify:track:4d9gJg1ZKFPoHwOrtMPx9v", Some("https://open.spotify.com/track/4d9gJg1ZKFPoHwOrtMPx9v"))),
        Track(SongDetails("Jewel", List("Clawz SG"), Some("Inner Symphony Gold 2018"), Some(LocalDate.of(2018, 12, 17)), Some("album")), AudioDetails(121.961, 450932 milliseconds, EFlatMinor),SourceDetails("spotify:track:3IHZRkwngHQtoJWlodS4OT", Some("https://open.spotify.com/track/3IHZRkwngHQtoJWlodS4OT"))),
        Track(SongDetails("Expanse", List("Dezza", "Kolonie"), Some("Expanse"), Some(LocalDate.of(2018, 11, 16)), Some("single")), AudioDetails(125.046, 222730 milliseconds, DFlatMinor),SourceDetails("spotify:track:4nSKOvcIJ30Z7bpdyr4KfT", Some("https://open.spotify.com/track/4nSKOvcIJ30Z7bpdyr4KfT"))),
        Track(SongDetails("Putting The World Back Together", List("Alex Rusin"), Some("Cold Winds"), Some(LocalDate.of(2019, 5, 31)), Some("single")), AudioDetails(123.0, 404895 milliseconds, EFlatMajor),SourceDetails("spotify:track:6E2zQiWndzKPecyTU2NVmF", Some("https://open.spotify.com/track/6E2zQiWndzKPecyTU2NVmF"))),
        Track(SongDetails("Mirage", List("Anden"), Some("Mirage"), Some(LocalDate.of(2019, 11, 22)), Some("single")), AudioDetails(124.032, 308736 milliseconds, BFlatMajor),SourceDetails("spotify:track:4Rys9v6OjDVCbW7JOdkQY7", Some("https://open.spotify.com/track/4Rys9v6OjDVCbW7JOdkQY7"))),
        Track(SongDetails("Venere", List("BOg", "GHEIST"), Some("Venere"), Some(LocalDate.of(2019, 7, 8)), Some("single")), AudioDetails(124.991, 404956 milliseconds, CMinor),SourceDetails("spotify:track:7rrrjYORC2IAtjA7NCQLPb", Some("https://open.spotify.com/track/7rrrjYORC2IAtjA7NCQLPb"))),
        Track(SongDetails("Gwendoline - Original Mix", List("Clawz SG"), Some("Gwendoline"), Some(LocalDate.of(2015, 3, 9)), Some("single")), AudioDetails(124.012, 452913 milliseconds, BMinor),SourceDetails("spotify:track:6heLeWInDSLU9wdrKhx2l5", Some("https://open.spotify.com/track/6heLeWInDSLU9wdrKhx2l5"))),
        Track(SongDetails("Points Beyond", List("Cubicolor"), Some("Points Beyond"), Some(LocalDate.of(2019, 11, 15)), Some("single")), AudioDetails(115.999, 326500 milliseconds, BMajor),SourceDetails("spotify:track:23N0RehCjU9KC7WfEzxdgJ", Some("https://open.spotify.com/track/23N0RehCjU9KC7WfEzxdgJ"))),
        Track(SongDetails("Indenait", List("Edu Imbernon"), Some("Indenait"), Some(LocalDate.of(2018, 9, 24)), Some("single")), AudioDetails(121.005, 577750 milliseconds, EMinor),SourceDetails("spotify:track:5wC5dAdAQPzeuhmITZVAiO", Some("https://open.spotify.com/track/5wC5dAdAQPzeuhmITZVAiO"))),
        Track(SongDetails("Dune Suave", List("Einmusik"), Some("Einmusik / Lake Avalon"), Some(LocalDate.of(2019, 11, 29)), Some("single")), AudioDetails(124.003, 496563 milliseconds, FSharpMajor),SourceDetails("spotify:track:3RkZM4hPoN6AupH4Ir1RAO", Some("https://open.spotify.com/track/3RkZM4hPoN6AupH4Ir1RAO"))),
        Track(SongDetails("Night Blooming Jasmine - Rodriguez Jr. Remix Edit", List("Eli & Fur", "Rodriguez Jr."), Some("Night Blooming Jasmine (Rodriguez Jr. Remix)"), Some(LocalDate.of(2018, 8, 10)), Some("single")), AudioDetails(120.007, 316 seconds, GMinor),SourceDetails("spotify:track:4p2huo3cTwy477D3Y9bKWP", Some("https://open.spotify.com/track/4p2huo3cTwy477D3Y9bKWP"))),
        Track(SongDetails("Juniper - Braxton Remix", List("Dezza", "Braxton"), Some("Juniper (Braxton Remix)"), Some(LocalDate.of(2019, 8, 9)), Some("single")), AudioDetails(123.983, 300107 milliseconds, BMajor),SourceDetails("spotify:track:0trMaBLzyGmFNzk0hfFi2m", Some("https://open.spotify.com/track/0trMaBLzyGmFNzk0hfFi2m"))),
        Track(SongDetails("Paradox - Extended Mix", List("Diversion", "Fynn"), Some("The Sound Of Electronica, Vol. 12"), Some(LocalDate.of(2018, 8, 13)), Some("compilation")), AudioDetails(129.991, 341250 milliseconds, FMinor),SourceDetails("spotify:track:69xhL5Ug4sgTDCgAVyHynv", Some("https://open.spotify.com/track/69xhL5Ug4sgTDCgAVyHynv"))),
        Track(SongDetails("Sun", List("Gallago"), Some("Anjunadeep The Yearbook 2018"), Some(LocalDate.of(2018, 11, 29)), Some("compilation")), AudioDetails(122.079, 361831 milliseconds, GMinor),SourceDetails("spotify:track:2nmbPcReCG7ha6LDD9ZXjQ", Some("https://open.spotify.com/track/2nmbPcReCG7ha6LDD9ZXjQ"))),
        Track(SongDetails("Arrival", List("GHEIST"), Some("Arrival EP"), Some(LocalDate.of(2019, 7, 26)), Some("single")), AudioDetails(123.006, 394150 milliseconds, EFlatMinor),SourceDetails("spotify:track:0GpCIG97O5vQupnxMRDqjR", Some("https://open.spotify.com/track/0GpCIG97O5vQupnxMRDqjR"))),
        Track(SongDetails("Frequent Tendencies", List("GHEIST"), Some("Frequent Tendencies"), Some(LocalDate.of(2018, 6, 15)), Some("single")), AudioDetails(123.004, 402133 milliseconds, DFlatMinor),SourceDetails("spotify:track:1D986c2Uu3P20kDR31NrhQ", Some("https://open.spotify.com/track/1D986c2Uu3P20kDR31NrhQ"))),
        Track(SongDetails("You Caress", List("Giorgia Angiuli", "Lake Avalon"), Some("No Body No Pain"), Some(LocalDate.of(2018, 4, 13)), Some("single")), AudioDetails(124.0, 503549 milliseconds, GMajor),SourceDetails("spotify:track:7FyQCtWo4BYc9RdpEFcrSp", Some("https://open.spotify.com/track/7FyQCtWo4BYc9RdpEFcrSp"))),
        Track(SongDetails("Parabola", List("Hammer"), Some("Parabola"), Some(LocalDate.of(2019, 9, 6)), Some("single")), AudioDetails(118.979, 344582 milliseconds, DMinor),SourceDetails("spotify:track:7K7x1pCPGStLSObmidIP1S", Some("https://open.spotify.com/track/7K7x1pCPGStLSObmidIP1S"))),
        Track(SongDetails("Vapours", List("Hot Since 82", "Alex Mills"), Some("8-track"), Some(LocalDate.of(2019, 7, 26)), Some("album")), AudioDetails(121.997, 358033 milliseconds, DFlatMinor),SourceDetails("spotify:track:1DgfoSbmoJtiRVzuJ9iFK7", Some("https://open.spotify.com/track/1DgfoSbmoJtiRVzuJ9iFK7"))),
        Track(SongDetails("Better Off", List("HRRSN"), Some("The War on Empathy"), Some(LocalDate.of(2018, 8, 24)), Some("single")), AudioDetails(122.013, 425188 milliseconds, CMinor),SourceDetails("spotify:track:6Cyf6viX3iPuSdJixk2wBI", Some("https://open.spotify.com/track/6Cyf6viX3iPuSdJixk2wBI"))),
        Track(SongDetails("Into The Light", List("James Trystan"), Some("Modeplex Presents Authentic Steyoyoke #015"), Some(LocalDate.of(2019, 8, 12)), Some("album")), AudioDetails(123.01, 409600 milliseconds, CMajor),SourceDetails("spotify:track:79r2pk6jb2x18D5bLyAiT3", Some("https://open.spotify.com/track/79r2pk6jb2x18D5bLyAiT3"))),
        Track(SongDetails("For A Moment", List("Jazz Do It"), Some("Anjunadeep 10 Sampler: Part 1"), Some(LocalDate.of(2019, 3, 1)), Some("single")), AudioDetails(121.001, 356529 milliseconds, DMinor),SourceDetails("spotify:track:01J7GlzTYwAp0kHGwLrHiB", Some("https://open.spotify.com/track/01J7GlzTYwAp0kHGwLrHiB"))),
        Track(SongDetails("Lissome", List("Jobe"), Some("Jobe Presents Authentic Steyoyoke #012"), Some(LocalDate.of(2018, 3, 26)), Some("album")), AudioDetails(121.004, 510920 milliseconds, DFlatMajor),SourceDetails("spotify:track:5FzKaAOUwjHT2OK8LUOllY", Some("https://open.spotify.com/track/5FzKaAOUwjHT2OK8LUOllY"))),
        Track(SongDetails("Dapple - Extended Mix", List("Jody Wisternoff", "James Grant"), Some("Dapple"), Some(LocalDate.of(2019, 2, 26)), Some("single")), AudioDetails(119.999, 368645 milliseconds, AMinor),SourceDetails("spotify:track:3FJQLY5VINvftnU1mo0bYW", Some("https://open.spotify.com/track/3FJQLY5VINvftnU1mo0bYW"))),
        Track(SongDetails("April", List("Jonas Saalbach"), Some("April"), Some(LocalDate.of(2018, 3, 23)), Some("single")), AudioDetails(123.988, 498938 milliseconds, DMajor),SourceDetails("spotify:track:6v4ni85b9wX6IavE1b3Muf", Some("https://open.spotify.com/track/6v4ni85b9wX6IavE1b3Muf"))),
        Track(SongDetails("Silent North", List("Jonas Saalbach"), Some("Reminiscence"), Some(LocalDate.of(2019, 2, 22)), Some("album")), AudioDetails(121.997, 475918 milliseconds, EMajor),SourceDetails("spotify:track:1zlNGW2543jWE4iHNaBMSI", Some("https://open.spotify.com/track/1zlNGW2543jWE4iHNaBMSI"))),
        Track(SongDetails("Twisted Shapes", List("Jonas Saalbach", "Chris McCarthy"), Some("Reminiscence"), Some(LocalDate.of(2019, 2, 22)), Some("album")), AudioDetails(120.997, 493008 milliseconds, FMajor),SourceDetails("spotify:track:1DafZ2A2bRbXfLA7KVVYX7", Some("https://open.spotify.com/track/1DafZ2A2bRbXfLA7KVVYX7"))),
        Track(SongDetails("Human", List("Julian Wassermann"), Some("Human / Polydo"), Some(LocalDate.of(2018, 3, 19)), Some("single")), AudioDetails(124.997, 422197 milliseconds, CMajor),SourceDetails("spotify:track:7duQlpZgEexH9E4alUXwhe", Some("https://open.spotify.com/track/7duQlpZgEexH9E4alUXwhe"))),
        Track(SongDetails("Rapture", List("Lunar Plane"), Some("Rapture / Chimera"), Some(LocalDate.of(2018, 5, 21)), Some("single")), AudioDetails(120.002, 452380 milliseconds, GMinor),SourceDetails("spotify:track:2iLz47TwcEN22gTpbTYiU2", Some("https://open.spotify.com/track/2iLz47TwcEN22gTpbTYiU2"))),
        Track(SongDetails("Lazy Dog", List("Several Definitions", "Marc DePulse"), Some("2019 Day Collection"), Some(LocalDate.of(2019, 12, 23)), Some("compilation")), AudioDetails(107.991, 375166 milliseconds, FSharpMajor),SourceDetails("spotify:track:6fzl8LVinIXYtWGORlDWUA", Some("https://open.spotify.com/track/6fzl8LVinIXYtWGORlDWUA"))),
        Track(SongDetails("Pathos", List("Mashk"), Some("Pathos"), Some(LocalDate.of(2017, 8, 11)), Some("single")), AudioDetails(120.0, 523192 milliseconds, EFlatMinor),SourceDetails("spotify:track:7avI5luJYVkNUk6GObVTLd", Some("https://open.spotify.com/track/7avI5luJYVkNUk6GObVTLd"))),
        Track(SongDetails("Chrysalis", List("Clawz SG", "Mashk"), Some("Chrysalis"), Some(LocalDate.of(2018, 8, 20)), Some("single")), AudioDetails(123.008, 456081 milliseconds, CMajor),SourceDetails("spotify:track:14F7gfsIpA74Hb6n4eOhI6", Some("https://open.spotify.com/track/14F7gfsIpA74Hb6n4eOhI6")))),
        PlaylistSource.Spotify
      )))
    }

    "return all playlists that belong to a user" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists")) => Response.ok(json("spotify/flow/get/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists")) => Response.ok(json(s"spotify/flow/get/3-playlist-${r.uri.path.last}.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) => Response.ok(json(s"spotify/flow/find/4-audio-features-${r.uri.path.last}.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient().getAllPlaylists(token)

      response.asserting(_.map(_.name) must be (List("Mel 1", "Mel 2")))
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path.startsWith(paths)) &&
      req.headers.contains(new Header("Authorization", "Bearer token-5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"))

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString

}