package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}

import scala.reflect.ClassTag
import scala.util.Random

sealed trait Crossover[A] {
  def cross(par1: Array[A], par2: Array[A])(using r: Random): Array[A]

  def cross(par1: Array[A], par2: Array[A], crossoverProbability: Double)(using r: Random): Array[A] =
    if r.nextDouble() < crossoverProbability then cross(par1, par2) else par1
}

object Crossover {
  inline def bestKeySequenceTrackCrossover: Crossover[Track] = new Crossover[Track] {

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

    override def cross(par1: Array[Track], par2: Array[Track])(using r: Random): Array[Track] = {
      var i                = 0
      var bestStreakLength = 0
      var bestStreakPos    = 0
      while (i < par1.length && bestStreakPos + bestStreakLength < par1.length) {
        val newStreakLength = getStreakLength(i, par1)
        if (newStreakLength > bestStreakLength) {
          bestStreakLength = newStreakLength
          bestStreakPos = i
        }
        i += 1
      }

      val bestSeq = par1.slice(bestStreakPos, bestStreakPos + bestStreakLength)

      val sliceSize     = par1.length / 2
      val slicedBestSeq = if (bestStreakLength <= sliceSize) bestSeq else cut(bestSeq, sliceSize)

      val (left, right) = par2.splitAt(bestStreakPos + slicedBestSeq.length / 2)
      val bestSeqGenes = slicedBestSeq.toSet
      left.filterNot(bestSeqGenes.contains) ++ slicedBestSeq ++ right.filterNot(bestSeqGenes.contains)
    }

    private def cut(ts: Array[Track], sliceSize: Int)(using r: Random): Array[Track] = {
      val slicePoint = r.nextInt(sliceSize / 2)
      ts.slice(slicePoint, slicePoint + sliceSize)
    }
  }

  inline def threeWaySplitCrossover[A: ClassTag]: Crossover[A] = new Crossover[A] {
    override def cross(par1: Array[A], par2: Array[A])(using r: Random): Array[A] = {
      val middle             = par1.length / 2
      val point1: Int        = r.nextInt(middle)
      val point2: Int        = r.nextInt(middle) + middle
      val left = par1.slice(0, point1)
      val mid = par1.slice(point1, point2).toSet
      val right = par1.slice(point2, par1.length)
      left ++ par2.filter(mid.contains) ++ right
    }
  }
}
