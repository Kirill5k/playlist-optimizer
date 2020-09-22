package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import io.kirill.playlistoptimizer.core.playlist.Key
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random


sealed trait Crossover[A] {
  def cross(p1: IndexedSeq[A], p2: IndexedSeq[A])(implicit r: Random): IndexedSeq[A]

  def cross(p1: IndexedSeq[A], p2: IndexedSeq[A], crossoverProbability: Double)(implicit r: Random): IndexedSeq[A] = {
    val n = r.nextDouble()
    if (n < crossoverProbability) cross(p1, p2) else p1
  }
}

object Crossover {
  implicit def bestKeySequenceTrackCrossover: Crossover[Track] = new Crossover[Track] {

    override def cross(p1: IndexedSeq[Track], p2: IndexedSeq[Track])(implicit r: Random): IndexedSeq[Track] = {
      val (bestSeq, seqIndex) = p1.tails.take(p1.size).zipWithIndex.foldLeft[(IndexedSeq[Track], Int)]((Vector(), -1)) {
        case ((currentBest, bestIndex), (tail, index)) =>
          val newBest = combo(tail)
          if (newBest.size > currentBest.size) (newBest, index) else (currentBest, bestIndex)
      }

      val sliceSize = p1.size / 2
      val slicedBestSeq = if (bestSeq.size <= sliceSize) bestSeq else cut(bestSeq, sliceSize)

      val (left, right) = p2.splitAt(seqIndex + slicedBestSeq.size/2)
      left.filterNot(slicedBestSeq.contains) ++ slicedBestSeq ++ right.filterNot(slicedBestSeq.contains)
    }

    private def combo(ts: IndexedSeq[Track]): IndexedSeq[Track] = {
      @scala.annotation.tailrec
      def go(combo: IndexedSeq[Track], remaining: IndexedSeq[Track], previous: Track): IndexedSeq[Track] = {
        val newCombo = combo :+ previous
        if (remaining.isEmpty) newCombo
        else {
          val distance = Key.distance(previous.audio.key, remaining.head.audio.key)
          if (distance <= 1) go(newCombo, remaining.tail, remaining.head) else newCombo
        }
      }
      go(Vector(), ts.tail, ts.head)
    }

    private def cut(ts: IndexedSeq[Track], sliceSize: Int)(implicit r: Random): IndexedSeq[Track] = {
      val slicePoint = r.nextInt(sliceSize / 2)
      ts.slice(slicePoint, slicePoint+sliceSize)
    }
  }

  implicit def threeWaySplitCrossover[A]: Crossover[A] = new Crossover[A] {
    override def cross(p1: IndexedSeq[A], p2: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] = {
      val middle = p1.size / 2
      val point1: Int = r.nextInt(middle)
      val point2: Int = r.nextInt(middle) + middle
      val (left, mid, right) = p1.splitInThree(point1, point2)
      left ++ p2.filter(mid.contains) ++ right
    }
  }
}
