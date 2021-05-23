package moe.gensoukyo.npcspawner.looper;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadLooper extends Thread implements Looper {

    private final Queue<Runnable> queue = new LinkedList<>();

    public ThreadLooper() {
        this.start();
    }

    public void offer(Runnable runnable) {
        synchronized (queue) {
            queue.offer(runnable);
        }
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            Runnable runnable = null;
            synchronized (queue) {
                if (!queue.isEmpty()) runnable = queue.poll();
            }
            if (runnable != null) runnable.run();
            else {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    queue.clear();
                    running = false;
                }
            }
        }
    }
}
