package com.akgarg.pool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ThreadPool {

    void execute(Task task) throws ThreadPoolException;

    Optional<ThreadState> getThreadState(long threadId);

    Map<String, ThreadState> getAllThreadStates();

    List<Long> getThreadIds();

    Collection<Long> getThreadsIdsOfState(ThreadState state);

    int size();

    void stop() throws ThreadPoolException;

    void forceStop() throws ThreadPoolException;

}
