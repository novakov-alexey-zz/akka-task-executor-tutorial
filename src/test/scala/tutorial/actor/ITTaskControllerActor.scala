package tutorial.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import tutorial.model.{Task, VoidTask}

class ITTaskControllerActor extends TestKit(ActorSystem("AkkaJava")) with FlatSpecLike with ImplicitSender
  with BeforeAndAfterAll with Matchers {

  override def afterAll = TestKit.shutdownActorSystem(system)

  it should "route task to the same executor" in {
    //given
    val taskController = system.actorOf(Props(classOf[TaskController], Props.create(classOf[Echo])), "taskController")

    //when
    sendMessages(taskController, "company1")
    val routee1 = expectMsgType[ActorRef]
    sendMessages(taskController, "company2")
    val routee2 = expectMsgType[ActorRef]

    //then
    routee1 shouldNot be(routee2)

    //when
    sendMessages(taskController, "company1")
    val routee3 = expectMsgType[ActorRef]
    sendMessages(taskController, "company1")
    val routee4 = expectMsgType[ActorRef]

    //then
    routee3 should be(routee4)
  }

  def sendMessages(taskController: ActorRef, companyName: String) = {
    taskController ! new VoidTask with Task {
      override def getCompanyName = companyName

      override def run(): Unit = {}
    }
  }
}

class Echo extends Actor {
  def receive = {
    case _ => sender() ! self
  }
}