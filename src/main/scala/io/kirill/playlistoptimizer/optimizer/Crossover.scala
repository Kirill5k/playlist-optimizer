package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.{Key, Track}
import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random


sealed trait Crossover[A] {
  def cross(p1: Seq[A], p2: Seq[A]): Seq[A]
}

object Crossover {
  implicit val keySequenceBasedTracksCrossover: Crossover[Track] = new Crossover[Track] {
    override def cross(p1: Seq[Track], p2: Seq[Track]): Seq[Track] = {
      val (bestSeq, i) = p1.tails.take(p1.size).zipWithIndex.foldLeft[(Seq[Track], Int)]((Nil, -1)) {
        case ((currentBest, bestIndex), (tail, index)) =>
          val newBest = combo(tail)
          if (newBest.size > currentBest.size) (newBest, index) else (currentBest, bestIndex)
      }

      val (left, right) = p2.splitAt(i)
      left.filterNot(bestSeq.contains) :++ bestSeq :++ right.filterNot(bestSeq.contains)
    }

    private def combo(ts: Seq[Track]): Seq[Track] = {
      def go(combo: Seq[Track], remaining: Seq[Track], previous: Track): Seq[Track] = {
        if (remaining.isEmpty) combo
        else {
          val newCombo = combo :+ previous
          val distance = Key.distance(previous.audio.key, remaining.head.audio.key)
          if (distance <= 1) go(newCombo, remaining.tail, remaining.head) else newCombo
        }
      }
      go(Nil, ts.tail, ts.head)
    }
  }

  implicit def threeWayCrossover[A](implicit R: Random): Crossover[A] = new Crossover[A] {
    override def cross(p1: Seq[A], p2: Seq[A]): Seq[A] = {
      val middle = p1.size / 2
      val point1: Int = R.nextInt(middle)
      val point2: Int = R.nextInt(middle) + middle
      val (l, m, r) = p1.splitInThree(point1, point2)
      l ++ p2.filter(m.contains) ++ r
    }
  }
}
