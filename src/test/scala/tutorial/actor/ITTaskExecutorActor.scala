package tutorial.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class ITTaskExecutorActor extends TestKit(ActorSystem("AkkaJava")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers{

  it should "execute void tasks in a sequence" in {

  }

  it should "execute tasks with result in a sequence" in {

  }

  override def afterAll = TestKit.shutdownActorSystem(system)
}