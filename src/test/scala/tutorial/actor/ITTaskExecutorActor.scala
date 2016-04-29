package tutorial.actor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.model.{Task, TaskWithResult, VoidTask}

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParRange
import scala.concurrent.Await
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
    //println(s"a.length = ${a.length}, a = $a")
  }

  it should "execute tasks with result in a sequence" in {
    //given
    val taskExecutor = executor
    val a = ArrayBuffer[Int]()
    val iterations = 100
    var counter = 0
    implicit val timeout = Timeout(5 seconds)
    val taskWithResult = new TaskWithResult[Int] {
      override def call(): Int = {
        counter += 1
        counter
      }
    }

    //when
    (1 to iterations).toList.par.map(_ =>
      taskExecutor ? taskWithResult
    ).map(f => {
      f.onSuccess { case r: Int => a += r }
      f
    }
    ).foreach(f => Await.ready(f, 1 second))

    //then
    counter should be(iterations)
    //below assertion for 'a' may fail, because we update 'a' on client side in not thread safe way/
    a.length should be(iterations)
    a.sum should be(5050)
  }
}