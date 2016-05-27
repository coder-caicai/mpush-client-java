package com.mpush.util.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mpush.api.Logger;
import com.mpush.util.DefaultLogger;

/**
 * Created by ohun on 2016/1/23.
 */
public final class ExecutorManager {
    public static final String THREAD_NAME_PREFIX = "mp-client-";
    public static final String WRITE_THREAD_NAME = THREAD_NAME_PREFIX + "write-t";
    public static final String READ_THREAD_NAME = THREAD_NAME_PREFIX + "read-t";
    public static final String DISPATCH_THREAD_NAME = THREAD_NAME_PREFIX + "dispatch-t";
    public static final String START_THREAD_NAME = THREAD_NAME_PREFIX + "start-t";
    public static final String HTTP_THREAD_NAME = THREAD_NAME_PREFIX + "http-t";
    public static final ExecutorManager INSTANCE = new ExecutorManager();
    private ThreadPoolExecutor writeThread;
    private ThreadPoolExecutor dispatchThread;
    private ThreadPoolExecutor startThread;
    private ScheduledExecutorService httpRequestThread;
    private static final Logger logger = new DefaultLogger(ExecutorManager.class);

    public ThreadPoolExecutor getWriteThread() {
        if (writeThread == null || writeThread.isShutdown()) {
            writeThread = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(100),
                    new NamedThreadFactory(WRITE_THREAD_NAME),
                    new RejectedHandler());
        }
        return writeThread;
    }

    public ThreadPoolExecutor getDispatchThread() {
        if (dispatchThread == null || dispatchThread.isShutdown()) {
            dispatchThread = new ThreadPoolExecutor(2, 4,
                    1L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(100),
                    new NamedThreadFactory(DISPATCH_THREAD_NAME),
                    new RejectedHandler());
        }
        return dispatchThread;
    }

    public ThreadPoolExecutor getStartThread() {
        if (startThread == null || startThread.isShutdown()) {
            startThread = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1),
                    new NamedThreadFactory(START_THREAD_NAME),
                    new RejectedHandler());
        }
        return startThread;
    }

    public ScheduledExecutorService getHttpRequestThread() {
        if (httpRequestThread == null || httpRequestThread.isShutdown()) {
            httpRequestThread = new ScheduledThreadPoolExecutor(1,
                    new NamedThreadFactory(HTTP_THREAD_NAME),
                    new RejectedHandler());
        }
        return httpRequestThread;
    }

    public synchronized void shutdown() {
        if (writeThread != null) {
            writeThread.shutdownNow();
            writeThread = null;
        }
        if (dispatchThread != null) {
            dispatchThread.shutdownNow();
            dispatchThread = null;
        }
        if (startThread != null) {
            startThread.shutdownNow();
            startThread = null;

        }
        if (httpRequestThread != null) {
            httpRequestThread.shutdownNow();
            httpRequestThread = null;
        }
    }

    public static boolean isMPThread() {
        return Thread.currentThread().getName().startsWith(THREAD_NAME_PREFIX);
    }

    private static class RejectedHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            logger.w("a task was rejected execute=%s", executor);
        }
    }
}
