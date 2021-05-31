package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import io.kirill.playlistoptimizer.core.utils.collections._

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

    private def getStreakLength(i: Int, tracks: Array[Track]): Int = {
      @scala.annotation.tailrec
      def go(currentPos: Int, length: Int): Int =
        if (currentPos + 1 >= tracks.length) length
        else {
          val distance = Key.distance(tracks(currentPos).audio.key, tracks(currentPos + 1).audio.key)
          if (distance <= 1) go(currentPos + 1, length + 1) else length
        }
      go(i, 1)
    }

    override def cross(p1: IndexedSeq[Track], p2: IndexedSeq[Track])(implicit r: Random): IndexedSeq[Track] = {
      var i                = 0
      var bestStreakLength = 0
      var bestStreakPos    = 0
      val tracks           = p1.toArray
      while (i < tracks.length && bestStreakPos + bestStreakLength < tracks.length) {
        val newStreakLength = getStreakLength(i, tracks)
        if (newStreakLength > bestStreakLength) {
          bestStreakLength = newStreakLength
          bestStreakPos = i
        }
        i += 1
      }

      val bestSeq = p1.slice(bestStreakPos, bestStreakPos + bestStreakLength)

      val sliceSize     = p1.size / 2
      val slicedBestSeq = if (bestStreakLength <= sliceSize) bestSeq else cut(bestSeq, sliceSize)

      val (left, right) = p2.splitAt(bestStreakPos + slicedBestSeq.size / 2)
      val bestSeqGenes = slicedBestSeq.toSet
      left.filterNot(bestSeqGenes.contains) ++ slicedBestSeq ++ right.filterNot(bestSeqGenes.contains)
    }

    private def cut(ts: IndexedSeq[Track], sliceSize: Int)(implicit r: Random): IndexedSeq[Track] = {
      val slicePoint = r.nextInt(sliceSize / 2)
      ts.slice(slicePoint, slicePoint + sliceSize)
    }
  }

  implicit def threeWaySplitCrossover[A]: Crossover[A] = new Crossover[A] {
    override def cross(p1: IndexedSeq[A], p2: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] = {
      val middle             = p1.size / 2
      val point1: Int        = r.nextInt(middle)
      val point2: Int        = r.nextInt(middle) + middle
      val (left, mid, right) = p1.splitInThree(point1, point2)
      val midGenes = mid.toSet
      left ++ p2.filter(midGenes.contains) ++ right
    }
  }
}
