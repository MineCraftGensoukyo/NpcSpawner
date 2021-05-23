package moe.gensoukyo.npcspawner.looper;

public interface Looper {
    void offer(Runnable run);
    void interrupt();
}
