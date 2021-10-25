package moe.gensoukyo.npcspawner;

import moe.gensoukyo.npcspawner.looper.MainLooper;
import moe.gensoukyo.npcspawner.looper.ThreadLooper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author SQwatermark
 */
@Mod(
        modid = ModMain.MOD_ID,
        name = ModMain.MOD_NAME,
        version = ModMain.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:customnpcs;")
public class ModMain {

    public static final String MOD_ID = "npcspawner";
    public static final String MOD_NAME = "NpcSpawner";
    public static final String VERSION = "1.0.8";

    public static Logger logger;

    public static File modConfigDi;

    public static boolean pauseSpawn = false;
    public static boolean debugSpawn = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        modConfigDi = new File(event.getModConfigurationDirectory(), MOD_ID);
        NpcSpawner.config = NpcSpawnerConfig.instance();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PermissionAPI.registerNode("npcspawner.noMobSpawn", DefaultPermissionLevel.NONE, "Mobs will not spawn around those who has this permission");
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNpcSpawner());
        MainLooper.START.prepare();
        MainLooper.END.prepare();
        MinecraftForge.EVENT_BUS.register(MainLooper.START);
        MinecraftForge.EVENT_BUS.register(MainLooper.END);
        ThreadLooper.getLooper().prepare();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        MainLooper.START.exit();
        MainLooper.END.exit();
        MinecraftForge.EVENT_BUS.unregister(MainLooper.START);
        MinecraftForge.EVENT_BUS.unregister(MainLooper.END);
        ThreadLooper.getLooper().exit();
    }

}
