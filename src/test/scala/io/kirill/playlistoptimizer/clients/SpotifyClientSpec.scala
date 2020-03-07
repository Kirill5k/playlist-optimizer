package io.kirill.playlistoptimizer.clients

import java.time.LocalDate

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.domain.Key._
import io.kirill.playlistoptimizer.domain._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Header

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class SpotifyClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-1")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  "A SpotifyClient" - {

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" => Response.ok(json("spotify/flow/find/1-auth.json"))
          case r if isAuthorized(r, "api.spotify.com/users", List("user-1", "playlists")) => Response.ok(json("spotify/flow/find/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com/playlists", List("7npAZEYwEwV2JV7XX2n3wq")) => Response.ok(json("spotify/flow/find/3-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com/audio-features") => Response.ok(json(s"spotify/flow/4-audio-features-${r.uri.path.head}.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = ApiClient.spotifyClient.findPlaylistByName("mel")

      response.asserting(_ must be(Playlist("Mel", Some("Melodic deep house and techno songs"), PlaylistSource.Spotify, Vector(
        Track(SongDetails("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album")), AudioDetails(129.983, 269150 milliseconds, CMinor)),
        Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1"), Some(LocalDate.of(2017, 3, 17)), Some("compilation")), AudioDetails(123.018, 411773 milliseconds, FSharpMajor)),
        Track(SongDetails("New Sky - Edu Imbernon Remix", List("RÜFÜS DU SOL", "Edu Imbernon"), Some("SOLACE REMIXED"), Some(LocalDate.of(2019, 9, 6)), Some("album")), AudioDetails(123.996, 535975 milliseconds, AMinor)),
        Track(SongDetails("Play - Original Mix", List("Ben Ashton"), Some("Déepalma Ibiza 2016 (Compiled by Yves Murasca, Rosario Galati, Holter & Mogyoro)"), Some(LocalDate.of(2016, 7, 29)), Some("compilation")), AudioDetails(121.986, 342 seconds, DFlatMinor)),
        Track(SongDetails("Was It Love", List("Liquid Phonk"), Some("Compost Black Label #131 - Was It Love EP"), Some(LocalDate.of(2016, 5, 6)), Some("single")), AudioDetails(115.013, 476062 milliseconds, BFlatMinor)),
        Track(SongDetails("No Place - Will Clarke Remix", List("RÜFÜS DU SOL", "Will Clarke"), Some("SOLACE REMIXES VOL. 3"), Some(LocalDate.of(2018, 12, 14)), Some("single")), AudioDetails(125.0, 358145 milliseconds, DFlatMinor)),
        Track(SongDetails("Rea", List("Thomas Schwartz", "Fausto Fanizza"), Some("Rea EP"), Some(LocalDate.of(2016, 7, 8)), Some("single")), AudioDetails(122.007, 399344 milliseconds, DFlatMinor)),
        Track(SongDetails("Closer", List("ARTBAT", "WhoMadeWho"), Some("Montserrat / Closer"), Some(LocalDate.of(2019, 4, 19)), Some("single")), AudioDetails(125.005, 460800 milliseconds, FSharpMinor)),
        Track(SongDetails("Santiago", List("YNOT"), Some("U & I"), Some(LocalDate.of(2016, 6, 3)), Some("single")), AudioDetails(122.011, 310943 milliseconds, CMajor)),
        Track(SongDetails("Shift", List("ALMA (GER)"), Some("Timeshift"), Some(LocalDate.of(2018, 6, 8)), Some("single")), AudioDetails(120.998, 404635 milliseconds, FMajor)),
        Track(SongDetails("No War - Rampa Remix", List("Âme", "Rampa"), Some("Dream House Remixes Part I"), Some(LocalDate.of(2019, 8, 9)), Some("single")), AudioDetails(122.998, 439227 milliseconds, EFlatMinor)),
        Track(SongDetails("Nightfly - Extended Mix", List("Arm In Arm"), Some("Nightfly"), Some(LocalDate.of(2018, 9, 7)), Some("single")), AudioDetails(124.013, 422158 milliseconds, FMinor)),
        Track(SongDetails("Easy Drifter - Claude VonStroke Full Length Mix", List("Booka Shade", "Claude VonStroke"), Some("Cut the Strings - Album Remixes 1"), Some(LocalDate.of(2018, 5, 25)), Some("single")), AudioDetails(124.996, 406535 milliseconds, BFlatMinor)),
        Track(SongDetails("When The Sun Goes Down", List("Braxton"), Some("When The Sun Goes Down"), Some(LocalDate.of(2019, 2, 15)), Some("single")), AudioDetails(129.019, 284147 milliseconds, FSharpMinor)),
        Track(SongDetails("Mio - Clawz SG Remix", List("Ceas", "Clawz SG"), Some("Inner Symphony Gold 2019"), Some(LocalDate.of(2020, 1, 17)), Some("album")), AudioDetails(123.009, 415708 milliseconds, FSharpMajor)),
        Track(SongDetails("Jewel", List("Clawz SG"), Some("Inner Symphony Gold 2018"), Some(LocalDate.of(2018, 12, 17)), Some("album")), AudioDetails(121.961, 450932 milliseconds, EFlatMinor)),
        Track(SongDetails("Expanse", List("Dezza", "Kolonie"), Some("Expanse"), Some(LocalDate.of(2018, 11, 16)), Some("single")), AudioDetails(125.046, 222730 milliseconds, DFlatMinor)),
        Track(SongDetails("Putting The World Back Together", List("Alex Rusin"), Some("Cold Winds"), Some(LocalDate.of(2019, 5, 31)), Some("single")), AudioDetails(123.0, 404895 milliseconds, EFlatMajor)),
        Track(SongDetails("Mirage", List("Anden"), Some("Mirage"), Some(LocalDate.of(2019, 11, 22)), Some("single")), AudioDetails(124.032, 308736 milliseconds, BFlatMajor)),
        Track(SongDetails("Venere", List("BOg", "GHEIST"), Some("Venere"), Some(LocalDate.of(2019, 7, 8)), Some("single")), AudioDetails(124.991, 404956 milliseconds, CMinor)),
        Track(SongDetails("Gwendoline - Original Mix", List("Clawz SG"), Some("Gwendoline"), Some(LocalDate.of(2015, 3, 9)), Some("single")), AudioDetails(124.012, 452913 milliseconds, BMinor)),
        Track(SongDetails("Points Beyond", List("Cubicolor"), Some("Points Beyond"), Some(LocalDate.of(2019, 11, 15)), Some("single")), AudioDetails(115.999, 326500 milliseconds, BMajor)),
        Track(SongDetails("Indenait", List("Edu Imbernon"), Some("Indenait"), Some(LocalDate.of(2018, 9, 24)), Some("single")), AudioDetails(121.005, 577750 milliseconds, EMinor)),
        Track(SongDetails("Dune Suave", List("Einmusik"), Some("Einmusik / Lake Avalon"), Some(LocalDate.of(2019, 11, 29)), Some("single")), AudioDetails(124.003, 496563 milliseconds, FSharpMajor)),
        Track(SongDetails("Night Blooming Jasmine - Rodriguez Jr. Remix Edit", List("Eli & Fur", "Rodriguez Jr."), Some("Night Blooming Jasmine (Rodriguez Jr. Remix)"), Some(LocalDate.of(2018, 8, 10)), Some("single")), AudioDetails(120.007, 316 seconds, GMinor)),
        Track(SongDetails("Juniper - Braxton Remix", List("Dezza", "Braxton"), Some("Juniper (Braxton Remix)"), Some(LocalDate.of(2019, 8, 9)), Some("single")), AudioDetails(123.983, 300107 milliseconds, BMajor)),
        Track(SongDetails("Paradox - Extended Mix", List("Diversion", "Fynn"), Some("The Sound Of Electronica, Vol. 12"), Some(LocalDate.of(2018, 8, 13)), Some("compilation")), AudioDetails(129.991, 341250 milliseconds, FMinor)),
        Track(SongDetails("Sun", List("Gallago"), Some("Anjunadeep The Yearbook 2018"), Some(LocalDate.of(2018, 11, 29)), Some("compilation")), AudioDetails(122.079, 361831 milliseconds, GMinor)),
        Track(SongDetails("Arrival", List("GHEIST"), Some("Arrival EP"), Some(LocalDate.of(2019, 7, 26)), Some("single")), AudioDetails(123.006, 394150 milliseconds, EFlatMinor)),
        Track(SongDetails("Frequent Tendencies", List("GHEIST"), Some("Frequent Tendencies"), Some(LocalDate.of(2018, 6, 15)), Some("single")), AudioDetails(123.004, 402133 milliseconds, DFlatMinor)),
        Track(SongDetails("You Caress", List("Giorgia Angiuli", "Lake Avalon"), Some("No Body No Pain"), Some(LocalDate.of(2018, 4, 13)), Some("single")), AudioDetails(124.0, 503549 milliseconds, GMajor)),
        Track(SongDetails("Parabola", List("Hammer"), Some("Parabola"), Some(LocalDate.of(2019, 9, 6)), Some("single")), AudioDetails(118.979, 344582 milliseconds, DMinor)),
        Track(SongDetails("Vapours", List("Hot Since 82", "Alex Mills"), Some("8-track"), Some(LocalDate.of(2019, 7, 26)), Some("album")), AudioDetails(121.997, 358033 milliseconds, DFlatMinor)),
        Track(SongDetails("Better Off", List("HRRSN"), Some("The War on Empathy"), Some(LocalDate.of(2018, 8, 24)), Some("single")), AudioDetails(122.013, 425188 milliseconds, CMinor)),
        Track(SongDetails("Into The Light", List("James Trystan"), Some("Modeplex Presents Authentic Steyoyoke #015"), Some(LocalDate.of(2019, 8, 12)), Some("album")), AudioDetails(123.01, 409600 milliseconds, CMajor)),
        Track(SongDetails("For A Moment", List("Jazz Do It"), Some("Anjunadeep 10 Sampler: Part 1"), Some(LocalDate.of(2019, 3, 1)), Some("single")), AudioDetails(121.001, 356529 milliseconds, DMinor)),
        Track(SongDetails("Lissome", List("Jobe"), Some("Jobe Presents Authentic Steyoyoke #012"), Some(LocalDate.of(2018, 3, 26)), Some("album")), AudioDetails(121.004, 510920 milliseconds, DFlatMajor)),
        Track(SongDetails("Dapple - Extended Mix", List("Jody Wisternoff", "James Grant"), Some("Dapple"), Some(LocalDate.of(2019, 2, 26)), Some("single")), AudioDetails(119.999, 368645 milliseconds, AMinor)),
        Track(SongDetails("April", List("Jonas Saalbach"), Some("April"), Some(LocalDate.of(2018, 3, 23)), Some("single")), AudioDetails(123.988, 498938 milliseconds, DMajor)),
        Track(SongDetails("Silent North", List("Jonas Saalbach"), Some("Reminiscence"), Some(LocalDate.of(2019, 2, 22)), Some("album")), AudioDetails(121.997, 475918 milliseconds, EMajor)),
        Track(SongDetails("Twisted Shapes", List("Jonas Saalbach", "Chris McCarthy"), Some("Reminiscence"), Some(LocalDate.of(2019, 2, 22)), Some("album")), AudioDetails(120.997, 493008 milliseconds, FMajor)),
        Track(SongDetails("Human", List("Julian Wassermann"), Some("Human / Polydo"), Some(LocalDate.of(2018, 3, 19)), Some("single")), AudioDetails(124.997, 422197 milliseconds, CMajor)),
        Track(SongDetails("Rapture", List("Lunar Plane"), Some("Rapture / Chimera"), Some(LocalDate.of(2018, 5, 21)), Some("single")), AudioDetails(120.002, 452380 milliseconds, GMinor)),
        Track(SongDetails("Lazy Dog", List("Several Definitions", "Marc DePulse"), Some("2019 Day Collection"), Some(LocalDate.of(2019, 12, 23)), Some("compilation")), AudioDetails(107.991, 375166 milliseconds, FSharpMajor)),
        Track(SongDetails("Pathos", List("Mashk"), Some("Pathos"), Some(LocalDate.of(2017, 8, 11)), Some("single")), AudioDetails(120.0, 523192 milliseconds, EFlatMinor)),
        Track(SongDetails("Chrysalis", List("Clawz SG", "Mashk"), Some("Chrysalis"), Some(LocalDate.of(2018, 8, 20)), Some("single")), AudioDetails(123.008, 456081 milliseconds, CMajor)))
      )))
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path == paths) &&
      req.headers.contains(new Header("Authorization", "Bearer BQCK-13bJ_7Qp6sa8DPvNBtvviUDasacL___qpx88zl6M2GDFjnL7qzG9WB9j7DtXmGrLML2Dy1DGPutRPabx316KIskN0amIZmdBZd7EKs3kFA1eXyu5HsjmwdHRD5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"))

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString

}
