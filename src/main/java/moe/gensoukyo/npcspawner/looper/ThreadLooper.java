package moe.gensoukyo.npcspawner.looper;

import moe.gensoukyo.npcspawner.Looper;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadLooper implements Looper {

    private static ThreadLooper threadLooper;
    public static Looper getLooper() {
        if (threadLooper == null) threadLooper = new ThreadLooper(false);
        return threadLooper;
    }

    private final boolean autoExit;
    private Thread thread;

    private boolean running = false;
    private boolean flag = true;
    private Queue<Runnable> queue = new LinkedList<>();
    private Queue<Runnable> backingQueue = new LinkedList<>();
    private final Object[] lock = new Object[0];
    private final Object[] exitingLock = new Object[0];

    private ThreadLooper(boolean exitAuto) {
        autoExit = exitAuto;
        (thread = new Thread(this::run)).start();
    }

    private void run() {
        if (Thread.currentThread() != thread) return;
        while (!thread.isInterrupted()) {
            synchronized (exitingLock) {
                if (!running) break;
            }
            synchronized (lock) {
                if (queue.isEmpty()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                flag = true;
            }
            while (!queue.isEmpty()) {
                try {
                    queue.poll().run();
                } catch (Throwable tr) {
                    tr.printStackTrace();
                }
            }
            synchronized (lock) {
                flag = false;
                Queue<Runnable> t = queue;
                queue = backingQueue;
                backingQueue = t;
            }
            if (autoExit) break;
        }
        synchronized (exitingLock) {
            queue.clear();
            backingQueue.clear();
            running = false;
        }
    }

    @Override
    public void prepare() {
        synchronized (exitingLock) {
            if (!running) {
                if (thread == null || (thread.getState() != Thread.State.NEW && !thread.isAlive()))
                    thread = new Thread(this::run);
                running = true;
                thread.start();
            }
        }
    }

    @Override
    public void add(Runnable task) {
        synchronized (exitingLock) {
            if (!running) return;
        }
        synchronized (lock) {
            (flag ? backingQueue : queue).add(task);
            lock.notify();
        }
    }

    @Override
    public void exit() {
        synchronized (exitingLock) {
            thread.interrupt();
            running = false;
        }
    }

}
