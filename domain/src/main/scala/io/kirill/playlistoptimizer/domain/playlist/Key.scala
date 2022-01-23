package io.kirill.playlistoptimizer.domain.playlist

import io.kirill.playlistoptimizer.domain.errors.{InvalidKey, InvalidMode}
import io.kirill.playlistoptimizer.domain.utils.common.*
import io.kirill.playlistoptimizer.domain.playlist.{Key, Mode}

enum Mode(val number: Int):
  case Minor extends Mode(0)
  case Major extends Mode(1)

object Mode:
  def apply(number: Int): Mode = Mode.values.find(_.number == number).getOrElse(throw InvalidMode(number))

enum Key(val number: Int, val name: String, val mode: Mode, val abbreviation: String, val altAbbreviation: String = "n/a"):
  case AFlatMinor extends Key(1, "A-Flat Minor", Mode.Minor, "Abm", "G#m")
  case BMajor extends Key(1, "B Major", Mode.Major, "B")
  case EFlatMinor extends Key(2, "E-Flat Minor", Mode.Minor, "Ebm", "D#m")
  case FSharpMajor extends Key(2, "F-Sharp Major", Mode.Major, "F#")
  case BFlatMinor extends Key(3, "B-Flat Minor", Mode.Minor, "Bbm", "A#m")
  case DFlatMajor extends Key(3, "D-Flat Major", Mode.Major, "Db", "C#")
  case FMinor extends Key(4, "F Minor", Mode.Minor, "Fm")
  case AFlatMajor extends Key(4, "A-Flat Major", Mode.Major, "Ab", "G#")
  case CMinor extends Key(5, "C Minor", Mode.Minor, "Cm")
  case EFlatMajor extends Key(5, "E-Flat Major", Mode.Major, "Eb", "D#")
  case GMinor extends Key(6, "G Minor", Mode.Minor, "Gm")
  case BFlatMajor extends Key(6, "B-Flat Major", Mode.Major, "Bb", "A#")
  case DMinor extends Key(7, "D Minor", Mode.Minor, "Dm")
  case FMajor extends Key(7, "F Major", Mode.Major, "F")
  case AMinor extends Key(8, "A Minor", Mode.Minor, "Am")
  case CMajor extends Key(8, "C Major", Mode.Major, "C")
  case EMinor extends Key(9, "E Minor", Mode.Minor, "Em")
  case GMajor extends Key(9, "G Major", Mode.Major, "G")
  case BMinor extends Key(10, "B Minor", Mode.Minor, "Bm")
  case DMajor extends Key(10, "D Major", Mode.Major, "D")
  case FSharpMinor extends Key(11, "F-Sharp Minor", Mode.Minor, "F#m")
  case AMajor extends Key(11, "A Major", Mode.Major, "A")
  case DFlatMinor extends Key(12, "D-Flat Minor", Mode.Minor, "Dbm", "C#m")
  case EMajor extends Key(12, "E Major", Mode.Major, "E")

object Key:
  inline def distance(key1: Key, key2: Key): Int = (key1, key2) match
    case (k1, k2) if k1 == k2 => 0
    case (k1, k2) =>
      (k1.number, k2.number, k1.mode, k2.mode) match
        case (n1, n2, _, _) if n1 == n2                => 1
        case (n1, n2, m1, m2) if math.abs(n1 - n2) > 6 => math.min(n1, n2) + 12 - math.max(n1, n2) + (m1 != m2).toInt
        case (n1, n2, m1, m2)                          => math.abs(n1 - n2) + (m1 != m2).toInt


  def apply(keyNumber: Int, modeNumber: Int): Key =
    apply(keyNumber, Mode(modeNumber))

  def apply(keyNumber: Int, mode: Mode): Key =
    Key.values.find(key => key.number == keyNumber && key.mode == mode).getOrElse(throw InvalidKey(keyNumber, mode.number))
