package moe.gensoukyo.npcspawner;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadLooper extends Thread implements Looper {

    private static ThreadLooper threadLooper = new ThreadLooper();
    public static Looper getLooper() {
        if (!threadLooper.isRunning()) threadLooper = new ThreadLooper();
        return threadLooper;
    }

    private boolean running = true;
    private boolean flag = true;
    private final Queue<Runnable> queue = new LinkedList<>();
    private final Queue<Runnable> backingQueue = new LinkedList<>();
    private final Object[] lock = new Object[0];
    private final Object[] exitingLock = new Object[0];

    private ThreadLooper() {
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                break;
            }
            synchronized (exitingLock) {
                if (!running || isInterrupted()) break;
            }

            synchronized (lock) {
                flag = !flag;
            }
            if (flag) {
                while (!queue.isEmpty()) queue.poll().run();
            }
            else {
                while (!backingQueue.isEmpty()) backingQueue.poll().run();
            }
        }
        queue.clear();
        backingQueue.clear();
    }

    @Override
    public void add(Runnable task) {
        synchronized (exitingLock) {
            if (!running) return;
        }
        synchronized (lock) {
            if (flag) {
                backingQueue.add(task);
            }
            else {
                queue.add(task);
            }
        }
    }

    @Override
    public void exit() {
        synchronized (exitingLock) {
            running = false;
            this.interrupt();
        }
    }

    private boolean isRunning() {
        synchronized (exitingLock) {
            return running;
        }
    }

}
