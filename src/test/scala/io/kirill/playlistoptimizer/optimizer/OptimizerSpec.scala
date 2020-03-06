package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import io.kirill.playlistoptimizer.domain._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random


class OptimizerSpec extends AnyWordSpec with Matchers {
  import Key._

  implicit val random = new Random(1)

  val s1 = Vector(
    track("song 1", BMajor),
    track("song 2", EMajor),
    track("song 3", EMajor),
    track("song 4", AMajor),
    track("song 5", DMajor),
    track("song 6", GMajor),
    track("song 7", GMajor),
    track("song 8", GMajor),
    track("song 9", GMajor),
    track("song 10", GMajor)
  )

  "An Optimizer" should {

    "create initial population" in {
      val population = Optimizer.initPopulation(s1, 10)

      population must have size (10)
    }

    "distribute population in pairs" in {
      val s2 = Random.shuffle(s1)
      val s3 = Random.shuffle(s1)
      val s4 = Random.shuffle(s1)
      val distributedInPairs = Optimizer.distributeInPairs(List(s1, s2, s3, s4))

      distributedInPairs must contain allOf ((s1, s2), (s3, s4))
    }

    "mutate solution" in {
      val mutatedPlaylist = Optimizer.mutate(s1)

      mutatedPlaylist must not contain theSameElementsInOrderAs (s1)
      mutatedPlaylist must contain theSameElementsAs s1
    }

    "crossover 2 solutions" in {
      val s2 = Random.shuffle(s1)
      val child = Optimizer.crossover(s1, s2)

      child must contain theSameElementsAs s1
      child must contain theSameElementsAs s2
      child must not contain theSameElementsInOrderAs (s1)
      child must not contain theSameElementsInOrderAs (s2)
    }
  }

  "A GeneticAlgorithmOptimizer" should {

    "optimize a playlist" in {
      val songs = playlist.tracks
      val optimizedSongs = Optimizer.geneticAlgorithmOptimizer(100, 250, 0.3).optimize(songs)

      optimizedSongs must contain theSameElementsAs songs
      optimizedSongs must not contain theSameElementsInOrderAs (songs)
      Evaluator.tracksEvaluator.evaluate(optimizedSongs) must be < Evaluator.tracksEvaluator.evaluate(songs) / 4

      println(s"original score: ${Evaluator.tracksEvaluator.evaluate(songs)}, optimized score: ${Evaluator.tracksEvaluator.evaluate(optimizedSongs)}")
      println(optimizedSongs)
    }
  }

  val playlist = Playlist("Mel", Some("Melodic deep house and techno songs"), PlaylistSource.Spotify, Vector(
    Track(SongDetails("Glue", List("Bicep" ), Some("Bicep")), AudioDetails(129.983,269150 milliseconds,CMinor)),
    Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1")), AudioDetails(123.018,411773 milliseconds,FSharpMajor)),
    Track(SongDetails("New Sky - Edu Imbernon Remix", List("RÜFÜS DU SOL", "Edu Imbernon"), Some("SOLACE REMIXED")), AudioDetails(123.996,535975 milliseconds,AMinor)),
    Track(SongDetails("Play - Original Mix", List("Ben Ashton"), Some("Déepalma Ibiza 2016 (Compiled by Yves Murasca, Rosario Galati, Holter & Mogyoro)")), AudioDetails(121.986,342 seconds,DFlatMinor)),
    Track(SongDetails("Was It Love", List("Liquid Phonk"), Some("Compost Black Label #131 - Was It Love EP")), AudioDetails(115.013,476062 milliseconds,BFlatMinor)),
    Track(SongDetails("No Place - Will Clarke Remix", List("RÜFÜS DU SOL", "Will Clarke"), Some("SOLACE REMIXES VOL. 3")), AudioDetails(125.0,358145 milliseconds,DFlatMinor)),
    Track(SongDetails("Rea", List("Thomas Schwartz", "Fausto Fanizza"), Some("Rea EP")), AudioDetails(122.007,399344 milliseconds,DFlatMinor)),
    Track(SongDetails("Closer", List("ARTBAT", "WhoMadeWho"), Some("Montserrat / Closer")), AudioDetails(125.005,460800 milliseconds,FSharpMinor)),
    Track(SongDetails("Santiago", List("YNOT"), Some("U & I")), AudioDetails(122.011,310943 milliseconds,CMajor)),
    Track(SongDetails("Shift", List("ALMA (GER)"), Some("Timeshift")), AudioDetails(120.998,404635 milliseconds,FMajor)),
    Track(SongDetails("No War - Rampa Remix", List("Âme", "Rampa"), Some("Dream House Remixes Part I")), AudioDetails(122.998,439227 milliseconds,EFlatMinor)),
    Track(SongDetails("Nightfly - Extended Mix", List("Arm In Arm"), Some("Nightfly")), AudioDetails(124.013,422158 milliseconds,FMinor)),
    Track(SongDetails("Easy Drifter - Claude VonStroke Full Length Mix", List("Booka Shade", "Claude VonStroke"), Some("Cut the Strings - Album Remixes 1")), AudioDetails(124.996,406535 milliseconds,BFlatMinor)),
    Track(SongDetails("When The Sun Goes Down", List("Braxton"), Some("When The Sun Goes Down")), AudioDetails(129.019,284147 milliseconds,FSharpMinor)),
    Track(SongDetails("Mio - Clawz SG Remix", List("Ceas", "Clawz SG"), Some("Inner Symphony Gold 2019")), AudioDetails(123.009,415708 milliseconds,FSharpMajor)),
    Track(SongDetails("Jewel", List("Clawz SG"), Some("Inner Symphony Gold 2018")), AudioDetails(121.961,450932 milliseconds,EFlatMinor)),
    Track(SongDetails("Expanse", List("Dezza", "Kolonie"), Some("Expanse")), AudioDetails(125.046,222730 milliseconds,DFlatMinor)),
    Track(SongDetails("Putting The World Back Together", List("Alex Rusin"), Some("Cold Winds")), AudioDetails(123.0,404895 milliseconds,EFlatMajor)),
    Track(SongDetails("Mirage", List("Anden"), Some("Mirage")), AudioDetails(124.032,308736 milliseconds,BFlatMajor)),
    Track(SongDetails("Venere", List("BOg", "GHEIST"), Some("Venere")), AudioDetails(124.991,404956 milliseconds,CMinor)),
    Track(SongDetails("Gwendoline - Original Mix", List("Clawz SG"), Some("Gwendoline")), AudioDetails(124.012,452913 milliseconds,BMinor)),
    Track(SongDetails("Points Beyond", List("Cubicolor"), Some("Points Beyond")), AudioDetails(115.999,326500 milliseconds,BMajor)),
    Track(SongDetails("Indenait", List("Edu Imbernon"), Some("Indenait")), AudioDetails(121.005,577750 milliseconds,EMinor)),
    Track(SongDetails("Dune Suave", List("Einmusik"), Some("Einmusik / Lake Avalon")), AudioDetails(124.003,496563 milliseconds,FSharpMajor)),
    Track(SongDetails("Night Blooming Jasmine - Rodriguez Jr. Remix Edit", List("Eli & Fur", "Rodriguez Jr."), Some("Night Blooming Jasmine (Rodriguez Jr. Remix)")), AudioDetails(120.007,316 seconds,GMinor)),
    Track(SongDetails("Juniper - Braxton Remix", List("Dezza", "Braxton"), Some("Juniper (Braxton Remix)")), AudioDetails(123.983,300107 milliseconds,BMajor)),
    Track(SongDetails("Paradox - Extended Mix", List("Diversion", "Fynn"), Some("The Sound Of Electronica, Vol. 12")), AudioDetails(129.991,341250 milliseconds,FMinor)),
    Track(SongDetails("Sun", List("Gallago"), Some("Anjunadeep The Yearbook 2018")), AudioDetails(122.079,361831 milliseconds,GMinor)),
    Track(SongDetails("Arrival", List("GHEIST"), Some("Arrival EP")), AudioDetails(123.006,394150 milliseconds,EFlatMinor)),
    Track(SongDetails("Frequent Tendencies", List("GHEIST"), Some("Frequent Tendencies")), AudioDetails(123.004,402133 milliseconds,DFlatMinor)),
    Track(SongDetails("You Caress", List("Giorgia Angiuli", "Lake Avalon"), Some("No Body No Pain")), AudioDetails(124.0,503549 milliseconds,GMajor)),
    Track(SongDetails("Parabola", List("Hammer"), Some("Parabola")), AudioDetails(118.979,344582 milliseconds,DMinor)),
    Track(SongDetails("Vapours", List("Hot Since 82", "Alex Mills"), Some("8-track")), AudioDetails(121.997,358033 milliseconds,DFlatMinor)),
    Track(SongDetails("Better Off", List("HRRSN"), Some("The War on Empathy")), AudioDetails(122.013,425188 milliseconds,CMinor)),
    Track(SongDetails("Into The Light", List("James Trystan"), Some("Modeplex Presents Authentic Steyoyoke #015")), AudioDetails(123.01,409600 milliseconds,CMajor)),
    Track(SongDetails("For A Moment", List("Jazz Do It"), Some("Anjunadeep 10 Sampler: Part 1")), AudioDetails(121.001,356529 milliseconds,DMinor)),
    Track(SongDetails("Lissome", List("Jobe"), Some("Jobe Presents Authentic Steyoyoke #012")), AudioDetails(121.004,510920 milliseconds,DFlatMajor)),
    Track(SongDetails("Dapple - Extended Mix", List("Jody Wisternoff", "James Grant"), Some("Dapple")), AudioDetails(119.999,368645 milliseconds,AMinor)),
    Track(SongDetails("April", List("Jonas Saalbach"), Some("April")), AudioDetails(123.988,498938 milliseconds,DMajor)),
    Track(SongDetails("Silent North", List("Jonas Saalbach"), Some("Reminiscence")), AudioDetails(121.997,475918 milliseconds,EMajor)),
    Track(SongDetails("Twisted Shapes", List("Jonas Saalbach", "Chris McCarthy"), Some("Reminiscence")), AudioDetails(120.997,493008 milliseconds,FMajor)),
    Track(SongDetails("Human", List("Julian Wassermann"), Some("Human / Polydo")), AudioDetails(124.997,422197 milliseconds,CMajor)),
    Track(SongDetails("Rapture", List("Lunar Plane"), Some("Rapture / Chimera")), AudioDetails(120.002,452380 milliseconds,GMinor)),
    Track(SongDetails("Lazy Dog", List("Several Definitions", "Marc DePulse"), Some("2019 Day Collection")), AudioDetails(107.991,375166 milliseconds,FSharpMajor)),
    Track(SongDetails("Pathos", List("Mashk"), Some("Pathos")), AudioDetails(120.0,523192 milliseconds,EFlatMinor)),
    Track(SongDetails("Chrysalis", List("Clawz SG", "Mashk"), Some("Chrysalis")), AudioDetails(123.008,456081 milliseconds,CMajor)))
  )
}
