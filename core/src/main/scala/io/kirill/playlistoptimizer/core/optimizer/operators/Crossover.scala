package io.kirill.playlistoptimizer.core.optimizer.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import io.kirill.playlistoptimizer.core.playlist.Key
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random


sealed trait Crossover[A] {
  def cross(p1: Seq[A], p2: Seq[A]): Seq[A]
}

object Crossover {
  implicit def bestKeySequenceTrackCrossover(implicit r: Random): Crossover[Track] = new Crossover[Track] {
    override def cross(p1: Seq[Track], p2: Seq[Track]): Seq[Track] = {
      val (bestSeq, seqIndex) = p1.tails.take(p1.size).zipWithIndex.foldLeft[(Seq[Track], Int)]((Nil, -1)) {
        case ((currentBest, bestIndex), (tail, index)) =>
          val newBest = combo(tail)
          if (newBest.size > currentBest.size) (newBest, index) else (currentBest, bestIndex)
      }

      val sliceSize = p1.size / 2
      val slicedBestSeq = if (bestSeq.size <= sliceSize) bestSeq else cut(bestSeq, sliceSize)

      val (left, right) = p2.splitAt(seqIndex + slicedBestSeq.size/2)
      left.filterNot(slicedBestSeq.contains) :++ slicedBestSeq :++ right.filterNot(slicedBestSeq.contains)
    }

    private def combo(ts: Seq[Track]): Seq[Track] = {
      def go(combo: Seq[Track], remaining: Seq[Track], previous: Track): Seq[Track] = {
        val newCombo = combo :+ previous
        if (remaining.isEmpty) newCombo
        else {
          val distance = Key.distance(previous.audio.key, remaining.head.audio.key)
          if (distance <= 1) go(newCombo, remaining.tail, remaining.head) else newCombo
        }
      }
      go(Nil, ts.tail, ts.head)
    }

    private def cut(ts: Seq[Track], sliceSize: Int)(implicit R: Random): Seq[Track] = {
      val slicePoint = R.nextInt(sliceSize / 2)
      ts.slice(slicePoint, slicePoint+sliceSize)
    }
  }

  implicit def threeWaySplitCrossover[A](implicit r: Random): Crossover[A] = new Crossover[A] {
    override def cross(p1: Seq[A], p2: Seq[A]): Seq[A] = {
      val middle = p1.size / 2
      val point1: Int = r.nextInt(middle)
      val point2: Int = r.nextInt(middle) + middle
      val (left, mid, right) = p1.splitInThree(point1, point2)
      left ++ p2.filter(mid.contains) ++ right
    }
  }
}
