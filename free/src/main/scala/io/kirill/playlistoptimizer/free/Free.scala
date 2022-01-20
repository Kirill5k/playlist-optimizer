package io.kirill.playlistoptimizer.free

enum Free[F[_], A]:
  case Pure[F[_], A](a: A) extends Free[F, A]
  case Suspend[F[_], A](fa: F[A]) extends Free[F, A]
  case Bind[F[_], E, A](target: Free[F, E], f: E => Free[F, A]) extends Free[F, A]

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = Bind(this, f)
  def map[B](f: A => B): Free[F, B]              = flatMap(a => Pure(f(a)))

object Free:
  def pure[F[_], A](a: A): Free[F, A]      = Free.Pure(a)
  def liftM[F[_], A](fa: F[A]): Free[F, A] = Free.Suspend(fa)

  extension [F[_], A](fa: F[A])
    def lift: Free[F, A] = Free.liftM(fa)