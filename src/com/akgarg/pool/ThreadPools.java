package com.akgarg.pool;

public final class ThreadPools {

    public ThreadPool fixedThreadPool(final int nThreads) {
        return new FixedThreadPool(nThreads);
    }

    public ThreadPool fixedThreadPool() {
        return new FixedThreadPool();
    }

    public ThreadPool fixedThreadPool(final int nThreads, final String threadNamePrefix) {
        return new FixedThreadPool(nThreads, threadNamePrefix);
    }

}
