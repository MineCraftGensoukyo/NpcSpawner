package moe.gensoukyo.npcspawner.looper;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.Queue;

public class MainLooper implements Looper{

    private boolean interrupted = false;
    private final Queue<Runnable> queue = new LinkedList<>();

    @Override
    public void offer(Runnable run) {
        synchronized (queue) {
            queue.offer(run);
        }
    }

    @Override
    public void interrupt() {
        interrupted = true;
        queue.clear();
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        if (interrupted) return;
        Runnable run = null;
        synchronized (queue) {
            if (!queue.isEmpty()) run = queue.poll();
        }
        if (run != null) run.run();
    }


}
