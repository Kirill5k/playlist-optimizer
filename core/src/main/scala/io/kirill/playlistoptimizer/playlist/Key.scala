package io.kirill.playlistoptimizer.playlist

import io.kirill.playlistoptimizer.utils.CommonUtils._

sealed abstract class Mode(val number: Int)
object Mode {
  final case object Minor extends Mode(0)
  final case object Major extends Mode(1)

  def apply(number: Int): Mode = number match {
    case 0 => Minor
    case 1 => Major
    case _ => throw new IllegalArgumentException(s"couldn't find mode with number $number")
  }
}

sealed abstract class Key(val number: Int, val name: String, val mode: Mode, val abbreviation: String, val altAbbreviation: String = "n/a")
object Key {
  import Mode._

  final case object AFlatMinor extends Key(1, "A-Flat Minor", Minor, "Abm", "G#m")
  final case object BMajor extends Key(1, "B Major", Major, "B")
  final case object EFlatMinor extends Key(2, "E-Flat Minor", Minor, "Ebm", "D#m")
  final case object FSharpMajor extends Key(2, "F-Sharp Major", Major, "F#")
  final case object BFlatMinor extends Key(3, "B-Flat Minor", Minor, "Bbm", "A#m")
  final case object DFlatMajor extends Key(3, "D-Flat Major", Major, "Db", "C#")
  final case object FMinor extends Key(4, "F Minor", Minor, "Fm")
  final case object AFlatMajor extends Key(4, "A-Flat Major", Major, "Ab", "G#")
  final case object CMinor extends Key(5, "C Minor", Minor, "Cm")
  final case object EFlatMajor extends Key(5, "E-Flat Major", Major, "Eb", "D#")
  final case object GMinor extends Key(6, "G Minor", Minor, "Gm")
  final case object BFlatMajor extends Key(6, "B-Flat Major", Major, "Bb", "A#")
  final case object DMinor extends Key(7, "D Minor", Minor, "Dm")
  final case object FMajor extends Key(7, "F Major", Major, "F")
  final case object AMinor extends Key(8, "A Minor", Minor, "Am")
  final case object CMajor extends Key(8, "C Major", Major, "C")
  final case object EMinor extends Key(9, "E Minor", Minor, "Em")
  final case object GMajor extends Key(9, "G Major", Major, "G")
  final case object BMinor extends Key(10, "B Minor", Minor, "Bm")
  final case object DMajor extends Key(10, "D Major", Major, "D")
  final case object FSharpMinor extends Key(11, "F-Sharp Minor", Minor, "F#m")
  final case object AMajor extends Key(11, "A Major", Major, "A")
  final case object DFlatMinor extends Key(12, "D-Flat Minor", Minor, "Dbm", "C#m")
  final case object EMajor extends Key(12, "E Major", Major, "E")

  lazy val values: Seq[Key] = List(
    AFlatMinor, EFlatMinor, BFlatMinor, FMinor, CMinor, GMinor, DMinor, AMinor, EMinor, BMinor, FSharpMinor, DFlatMinor,
    BMajor, FSharpMajor, DFlatMajor, AFlatMajor, EFlatMajor, BFlatMajor, FMajor, CMajor, GMajor, DMajor, AMajor, EMajor
  )

  def distance(key1: Key, key2: Key): Int = (key1, key2) match {
    case (k1, k2) if k1 == k2 => 0
    case (k1, k2) => (k1.number, k2.number, k1.mode, k2.mode) match {
      case (n1, n2, _, _) if n1 == n2 => 1
      case (n1, n2, m1, m2) if math.abs(n1 - n2) > 6 => math.min(n1, n2) + 12 - math.max(n1, n2) + (m1 != m2).toInt
      case (n1, n2, m1, m2) => math.abs(n1 - n2) + (m1 != m2).toInt
    }
  }

  def apply(keyNumber: Int, modeNumber: Int): Key =
    apply(keyNumber, Mode(modeNumber))

  def apply(keyNumber: Int, mode: Mode): Key =
    values
      .find(key => key.number == keyNumber && key.mode == mode)
      .getOrElse(throw new IllegalArgumentException(s"couldn't find key with number $keyNumber and mode $mode"))
}
