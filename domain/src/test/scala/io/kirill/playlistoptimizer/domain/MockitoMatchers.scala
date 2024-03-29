package io.kirill.playlistoptimizer.domain

import org.mockito.stubbing.{Answer, OngoingStubbing, Stubber}
import org.mockito.verification.VerificationMode
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar

trait MockitoMatchers extends MockitoSugar {

  def any[A]: A                                      = ArgumentMatchers.any[A]()
  def anyList[A]: List[A]                            = any[List[A]]
  def eqTo[A](value: A): A                           = ArgumentMatchers.eq[A](value)
  def doAnswer[A](answer: Answer[A]): Stubber        = Mockito.doAnswer(answer)
  def doThrow[A](error: Throwable): Stubber          = Mockito.doThrow(error)
  def when[A](mock: A): OngoingStubbing[A]           = Mockito.when(mock)
  def verify[A](mock: A, mode: VerificationMode): A  = Mockito.verify(mock, mode)
  def verify[A](mock: A): A                          = verify(mock, Mockito.times(1))
  def verifyNoInteractions(mocks: AnyRef*): Unit     = Mockito.verifyNoInteractions(mocks*)
  def verifyNoMoreInteractions(mocks: AnyRef*): Unit = Mockito.verifyNoMoreInteractions(mocks*)
}
