package tutorial.model;

import java.util.concurrent.Callable;

public interface TaskWithResult<V> extends Callable<V> {

}
