package io.kirill.playlistoptimizer.domain

import io.kirill.playlistoptimizer.utils.CommonUtils._

sealed abstract class Mode(val number: Int)
object Mode {
  case object Minor extends Mode(0)
  case object Major extends Mode(1)
}

sealed abstract class Key(val number: Int, val name: String, val mode: Mode, val abbreviation: String, val altAbbreviation: String = "n/a")
object Key {
  import Mode._

  case object AFlatMinor extends Key(1, "A-Flat Minor", Minor, "Abm", "G#m")
  case object BMajor extends Key(1, "B Major", Major, "B")
  case object EFlatMinor extends Key(2, "E-Flat Minor", Minor, "Ebm", "D#m")
  case object FSharpMajor extends Key(2, "F-Sharp Major", Major, "F#")
  case object BFlatMinor extends Key(3, "B-Flat Minor", Minor, "Bbm", "A#m")
  case object DFlatMajor extends Key(3, "D-Flat Major", Major, "Db", "C#")
  case object FMinor extends Key(4, "F Minor", Minor, "Fm")
  case object AFlatMajor extends Key(4, "A-Flat Major", Major, "Ab", "G#")
  case object CMinor extends Key(5, "C Minor", Minor, "Cm")
  case object EFlatMajor extends Key(5, "E-Flat Major", Major, "Eb", "D#")
  case object GMinor extends Key(6, "G Minor", Minor, "Gm")
  case object BFlatMajor extends Key(6, "B-Flat Major", Major, "Bb", "A#")
  case object DMinor extends Key(7, "D Minor", Minor, "Dm")
  case object FMajor extends Key(7, "F Major", Major, "F")
  case object AMinor extends Key(8, "A Minor", Minor, "Am")
  case object CMajor extends Key(8, "C Major", Major, "C")
  case object EMinor extends Key(9, "E Minor", Minor, "Em")
  case object GMajor extends Key(9, "G Major", Major, "G")
  case object BMinor extends Key(10, "B Minor", Minor, "Bm")
  case object DMajor extends Key(10, "D Major", Major, "D")
  case object FSharpMinor extends Key(11, "F-Sharp Minor", Minor, "F#m")
  case object AMajor extends Key(11, "A Major", Major, "A")
  case object DFlatMinor extends Key(12, "D-Flat Minor", Minor, "Dbm", "C#m")
  case object EMajor extends Key(12, "E Major", Major, "E")

  def distance(key1: Key, key2: Key): Int = (key1, key2) match {
    case (k1, k2) if k1 == k2 => 0
    case (k1, k2) => (k1.number, k2.number, k1.mode, k2.mode) match {
      case (n1, n2, _, _) if n1 == n2 => 1
      case (n1, n2, m1, m2) if math.abs(n1 - n2) > 6 => math.min(n1, n2) + 12 - math.max(n1, n2) + (m1 != m2).toInt
      case (n1, n2, m1, m2) => math.abs(n1 - n2) + (m1 != m2).toInt
    }
  }
}

sealed case class Song(title: String, artist: String, key: Key)
