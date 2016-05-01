package tutorial.actor;

import akka.actor.ActorPath;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;
import tutorial.model.Task;

public class TaskController extends UntypedActor {
  private ActorPath taskExecutorRouter;
  private ConsistentHashingRouter.ConsistentHashMapper hashMapper;

  {
    hashMapper = message -> {
      if (message instanceof Task) {
        return ((Task) message).getCompanyName();
      } else {
        return null;
      }
    };
  }

  public TaskController() {
    this(Props.create(TaskExecutor.class));
  }

  public TaskController(Props props) {
    taskExecutorRouter = getContext().system().actorOf(
            new ConsistentHashingPool(5).withHashMapper(hashMapper).props(props), "taskExecutorRouter"
    ).path();
  }

  public static Props props(final Props props) {
    return Props.create(new Creator<TaskController>() {
      private static final long serialVersionUID = 1L;

      @Override
      public TaskController create() throws Exception {
        return new TaskController(props);
      }
    });
  }

  @Override
  public void onReceive(Object message) throws Exception {
    getContext().actorSelection(taskExecutorRouter).tell(message, getSender());
  }
}
