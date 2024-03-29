package io.kirill.playlistoptimizer.domain

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

trait CatsIOSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {}
