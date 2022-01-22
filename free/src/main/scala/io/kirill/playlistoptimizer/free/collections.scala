package io.kirill.playlistoptimizer.free

import scala.reflect.ClassTag
import scala.util.Random

private[free] object collections:
  extension [A](arr: Array[A])
    def shuffle(using rand: Random, ev: ClassTag[A]): Array[A] = rand.shuffle(arr.toVector).toArray

  extension [A](seq: Seq[A])
    def pairs: Seq[(A, A)] =
      seq.zip(seq.tail).zipWithIndex collect { case (x, i) if (i + 1) % 2 != 0 => x }
