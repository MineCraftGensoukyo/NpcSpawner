package moe.gensoukyo.npcspawner;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class MainLooper implements Looper {

    public static final MainLooper START = new MainLooper(TickEvent.Phase.START);
    public static final MainLooper END = new MainLooper(TickEvent.Phase.END);

    private boolean running = false;
    private Queue<Runnable> queue = new LinkedList<>();
    private Queue<Runnable> backingQueue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Object[] queueLock = new Object[0];

    private final TickEvent.Phase phase;

    private MainLooper(TickEvent.Phase phase) {
        this.phase = phase;
    }

    public void stateStart() {
        running = true;
    }

    public void stateStop() {
        exit();
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == phase) {
            if (lock.tryLock()) {
                if (!running) return;
                try {
                    Queue<Runnable> tmp = queue;
                    queue = backingQueue;
                    backingQueue = tmp;
                } finally {
                    lock.unlock();
                }
                synchronized (queueLock) {
                    while (running && !queue.isEmpty()) queue.poll().run();
                }
            }
        }
    }

    @Override
    public void add(Runnable task) {
        lock.lock();
        try {
            if (running) backingQueue.add(task);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void exit() {
        lock.lock();
        try {
            running = false;
            backingQueue.clear();
        } finally {
            lock.unlock();
        }
        synchronized (queueLock) {
            queue.clear();
        }
    }

}
