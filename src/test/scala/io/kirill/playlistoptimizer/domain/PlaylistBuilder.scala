package io.kirill.playlistoptimizer.domain

import io.kirill.playlistoptimizer.domain.Key._

import scala.concurrent.duration._
import scala.language.postfixOps

object PlaylistBuilder {

  def playlist: Playlist = Playlist("Mel", Some("Melodic deep house and techno songs"), PlaylistSource.Spotify, Vector(
    Track(SongDetails("Glue", List("Bicep"), Some("Bicep"), None, None), AudioDetails(129.983,269150 milliseconds,CMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1"), None, None), AudioDetails(123.018,411773 milliseconds,FSharpMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("New Sky - Edu Imbernon Remix", List("RÜFÜS DU SOL", "Edu Imbernon"), Some("SOLACE REMIXED"), None, None), AudioDetails(123.996,535975 milliseconds,AMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Play - Original Mix", List("Ben Ashton"), Some("Déepalma Ibiza 2016 (Compiled by Yves Murasca, Rosario Galati, Holter & Mogyoro)"), None, None), AudioDetails(121.986,342 seconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Was It Love", List("Liquid Phonk"), Some("Compost Black Label #131 - Was It Love EP"), None, None), AudioDetails(115.013,476062 milliseconds,BFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("No Place - Will Clarke Remix", List("RÜFÜS DU SOL", "Will Clarke"), Some("SOLACE REMIXES VOL. 3"), None, None), AudioDetails(125.0,358145 milliseconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Rea", List("Thomas Schwartz", "Fausto Fanizza"), Some("Rea EP"), None, None), AudioDetails(122.007,399344 milliseconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Closer", List("ARTBAT", "WhoMadeWho"), Some("Montserrat / Closer"), None, None), AudioDetails(125.005,460800 milliseconds,FSharpMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Santiago", List("YNOT"), Some("U & I"), None, None), AudioDetails(122.011,310943 milliseconds,CMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Shift", List("ALMA (GER)"), Some("Timeshift"), None, None), AudioDetails(120.998,404635 milliseconds,FMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("No War - Rampa Remix", List("Âme", "Rampa"), Some("Dream House Remixes Part I"), None, None), AudioDetails(122.998,439227 milliseconds,EFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Nightfly - Extended Mix", List("Arm In Arm"), Some("Nightfly"), None, None), AudioDetails(124.013,422158 milliseconds,FMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Easy Drifter - Claude VonStroke Full Length Mix", List("Booka Shade", "Claude VonStroke"), Some("Cut the Strings - Album Remixes 1"), None, None), AudioDetails(124.996,406535 milliseconds,BFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("When The Sun Goes Down", List("Braxton"), Some("When The Sun Goes Down"), None, None), AudioDetails(129.019,284147 milliseconds,FSharpMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Mio - Clawz SG Remix", List("Ceas", "Clawz SG"), Some("Inner Symphony Gold 2019"), None, None), AudioDetails(123.009,415708 milliseconds,FSharpMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Jewel", List("Clawz SG"), Some("Inner Symphony Gold 2018"), None, None), AudioDetails(121.961,450932 milliseconds,EFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Expanse", List("Dezza", "Kolonie"), Some("Expanse"), None, None), AudioDetails(125.046,222730 milliseconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Putting The World Back Together", List("Alex Rusin"), Some("Cold Winds"), None, None), AudioDetails(123.0,404895 milliseconds,EFlatMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Mirage", List("Anden"), Some("Mirage"), None, None), AudioDetails(124.032,308736 milliseconds,BFlatMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Venere", List("BOg", "GHEIST"), Some("Venere"), None, None), AudioDetails(124.991,404956 milliseconds,CMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Gwendoline - Original Mix", List("Clawz SG"), Some("Gwendoline"), None, None), AudioDetails(124.012,452913 milliseconds,BMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Points Beyond", List("Cubicolor"), Some("Points Beyond"), None, None), AudioDetails(115.999,326500 milliseconds,BMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Indenait", List("Edu Imbernon"), Some("Indenait"), None, None), AudioDetails(121.005,577750 milliseconds,EMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Dune Suave", List("Einmusik"), Some("Einmusik / Lake Avalon"), None, None), AudioDetails(124.003,496563 milliseconds,FSharpMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Night Blooming Jasmine - Rodriguez Jr. Remix Edit", List("Eli & Fur", "Rodriguez Jr."), Some("Night Blooming Jasmine (Rodriguez Jr. Remix)"), None, None), AudioDetails(120.007,316 seconds,GMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Juniper - Braxton Remix", List("Dezza", "Braxton"), Some("Juniper (Braxton Remix)"), None, None), AudioDetails(123.983,300107 milliseconds,BMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Paradox - Extended Mix", List("Diversion", "Fynn"), Some("The Sound Of Electronica, Vol. 12"), None, None), AudioDetails(129.991,341250 milliseconds,FMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Sun", List("Gallago"), Some("Anjunadeep The Yearbook 2018"), None, None), AudioDetails(122.079,361831 milliseconds,GMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Arrival", List("GHEIST"), Some("Arrival EP"), None, None), AudioDetails(123.006,394150 milliseconds,EFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Frequent Tendencies", List("GHEIST"), Some("Frequent Tendencies"), None, None), AudioDetails(123.004,402133 milliseconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("You Caress", List("Giorgia Angiuli", "Lake Avalon"), Some("No Body No Pain"), None, None), AudioDetails(124.0,503549 milliseconds,GMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Parabola", List("Hammer"), Some("Parabola"), None, None), AudioDetails(118.979,344582 milliseconds,DMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Vapours", List("Hot Since 82", "Alex Mills"), Some("8-track"), None, None), AudioDetails(121.997,358033 milliseconds,DFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Better Off", List("HRRSN"), Some("The War on Empathy"), None, None), AudioDetails(122.013,425188 milliseconds,CMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Into The Light", List("James Trystan"), Some("Modeplex Presents Authentic Steyoyoke #015"), None, None), AudioDetails(123.01,409600 milliseconds,CMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("For A Moment", List("Jazz Do It"), Some("Anjunadeep 10 Sampler: Part 1"), None, None), AudioDetails(121.001,356529 milliseconds,DMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Lissome", List("Jobe"), Some("Jobe Presents Authentic Steyoyoke #012"), None, None), AudioDetails(121.004,510920 milliseconds,DFlatMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Dapple - Extended Mix", List("Jody Wisternoff", "James Grant"), Some("Dapple"), None, None), AudioDetails(119.999,368645 milliseconds,AMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("April", List("Jonas Saalbach"), Some("April"), None, None), AudioDetails(123.988,498938 milliseconds,DMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Silent North", List("Jonas Saalbach"), Some("Reminiscence"), None, None), AudioDetails(121.997,475918 milliseconds,EMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Twisted Shapes", List("Jonas Saalbach", "Chris McCarthy"), Some("Reminiscence"), None, None), AudioDetails(120.997,493008 milliseconds,FMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Human", List("Julian Wassermann"), Some("Human / Polydo"), None, None), AudioDetails(124.997,422197 milliseconds,CMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Rapture", List("Lunar Plane"), Some("Rapture / Chimera"), None, None), AudioDetails(120.002,452380 milliseconds,GMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Lazy Dog", List("Several Definitions", "Marc DePulse"), Some("2019 Day Collection"), None, None), AudioDetails(107.991,375166 milliseconds,FSharpMajor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Pathos", List("Mashk"), Some("Pathos"), None, None), AudioDetails(120.0,523192 milliseconds,EFlatMinor), SourceDetails("uri", Some("url"))),
    Track(SongDetails("Chrysalis", List("Clawz SG", "Mashk"), Some("Chrysalis"), None, None), AudioDetails(123.008,456081 milliseconds,CMajor), SourceDetails("uri", Some("url")))))
}
