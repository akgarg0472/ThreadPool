package com.akgarg.pool;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FixedThreadPool implements ThreadPool {

    private static final Logger LOGGER = Logger.getLogger("FixedThreadPool");
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private static final String DEFAULT_THREAD_NAME_PREFIX = "ThreadPool";

    private final BlockingQueue<Task> tasks;
    private final Collection<Thread> threads;
    private final int size;
    private final AtomicBoolean isShutdownSignalSent = new AtomicBoolean(false);

    public FixedThreadPool() {
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    public FixedThreadPool(final int nThreads) {
        this(nThreads, DEFAULT_THREAD_NAME_PREFIX);
    }

    public FixedThreadPool(final int nThreads, final String threadNamePrefix) {
        if (nThreads <= 0) {
            throw new ThreadPoolException("Invalid threads size: " + nThreads);
        }

        this.size = nThreads;
        this.tasks = new LinkedBlockingQueue<>(nThreads);
        this.threads = new ArrayList<>(nThreads);

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Initializing Thread pool of size %s with thread name prefix=%s".formatted(size, threadNamePrefix));
        }

        initThreads(threadNamePrefix);
        initExecution();
    }

    private void initExecution() {
        LOGGER.info("Initializing threads execution");
        threads.forEach(Thread::start);
    }

    private void initThreads(final String threadNamePrefix) {
        for (int i = 0; i < size; i++) {
            final Thread thread = new Thread(() -> {
                while (!isShutdownSignalSent.get()) {
                    try {
                        final Task task = tasks.take();
                        task.doProcess();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, threadNamePrefix + "-" + i);

            threads.add(thread);
        }

        LOGGER.info("Threads initialized successfully");
    }

    @Override
    public void execute(final Task task) throws ThreadPoolException {
        if (task == null) {
            throw new ThreadPoolException("Invalid task provided: " + null);
        }

        this.tasks.add(task);
    }

    @Override
    public Optional<ThreadState> getThreadState(final long threadId) {
        final AtomicReference<ThreadState> threadStateRef = new AtomicReference<>(null);

        for (final Thread thread : threads) {
            if (thread.threadId() == threadId) {
                threadStateRef.set(threadState(thread.getState()));
                break;
            }
        }

        return Optional.ofNullable(threadStateRef.get());
    }

    @Override
    public Map<String, ThreadState> getAllThreadStates() {
        final Map<String, ThreadState> threadStateMap = new HashMap<>();
        this.threads.forEach(thread -> threadStateMap.put(thread.getName(), threadState(thread.getState())));
        return threadStateMap;
    }

    @Override
    public List<Long> getThreadIds() {
        return threads.stream()
                .map(Thread::threadId)
                .toList();
    }

    @Override
    public Collection<Long> getThreadsIdsOfState(final ThreadState state) {
        if (state == null) {
            throw new ThreadPoolException("Invalid thread state provided: " + null);
        }

        return threads.stream()
                .filter(thread -> isThreadOfState(thread, state))
                .map(Thread::threadId)
                .toList();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void stop() throws ThreadPoolException {
        this.isShutdownSignalSent.set(true);
    }

    @Override
    public void forceStop() throws ThreadPoolException {
        this.isShutdownSignalSent.set(true);

        for (final Thread thread : threads) {
            thread.interrupt();
        }
    }

    private boolean isThreadOfState(final Thread thread, final ThreadState state) {
        return thread.getState().name().equalsIgnoreCase(state.name());
    }

    private ThreadState threadState(final Thread.State state) {
        return ThreadState.valueOf(state.name());
    }

}
