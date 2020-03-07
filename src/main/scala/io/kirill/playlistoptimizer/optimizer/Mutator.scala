package io.kirill.playlistoptimizer.optimizer

sealed trait Mutator[A] {
  def mutate(s: Seq[A]): Seq[A]
}
