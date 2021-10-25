package moe.gensoukyo.npcspawner;

public interface Looper {
    void prepare();
    void add(Runnable task);
    void exit();
}
