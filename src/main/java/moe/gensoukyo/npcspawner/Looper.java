package moe.gensoukyo.npcspawner;

public interface Looper {
    void add(Runnable task);
    void exit();
}
