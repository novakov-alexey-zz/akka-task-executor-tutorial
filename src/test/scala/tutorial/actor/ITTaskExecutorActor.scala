package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.model.{TaskWithResult, VoidTask}

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParRange
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class ITTaskExecutorActor extends TestKit(ActorSystem("AkkaJava")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  def executor = system.actorOf(Props(classOf[TaskExecutorActor]), "taskExecutorActor" + Random.nextInt)

  override def afterAll = TestKit.shutdownActorSystem(system)

  it should "execute void tasks in a sequence" in {
    //given
    val taskExecutor = executor
    val a = ArrayBuffer[Int]()
    val iterations = 100
    val task = new VoidTask() {
      override def run() = a += 10
    }
    //when
    ParRange(1, iterations, 1, inclusive = true) foreach { _ => taskExecutor ! task }

    //then
    Thread.sleep(1000)
    a.length should be(iterations)
    a.sum should be(iterations * 10)
    println(s"a.length = ${a.length}, a = $a")
  }

  it should "execute tasks with result in a sequence" in {
    //given
    val taskExecutor = executor
    val a = ArrayBuffer[Int]()
    val iterations = 100
    var counter = 0
    val taskWithResult = new TaskWithResult[Int] {
      override def call(): Int = {
        counter += 1
        counter
      }
    }
    implicit val timeout = Timeout(5 seconds)
    //when
    ParRange(1, iterations, 1, inclusive = true) foreach { _ =>
      val f = taskExecutor ? taskWithResult
      f.onSuccess { case r: Int => a += r }
    }

    //then
    Thread.sleep(3000)
    a.length should be(iterations)
    a.sum should be(5050)
  }
}