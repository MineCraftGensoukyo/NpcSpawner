package moe.gensoukyo.npcspawner;

import moe.gensoukyo.npcspawner.looper.Looper;
import moe.gensoukyo.npcspawner.looper.MainLooper;
import moe.gensoukyo.npcspawner.looper.ThreadLooper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
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
        dependencies = "required-after:customnpcs;",
        serverSideOnly = true
)
public class ModMain {

    public static final String MOD_ID = "npcspawner";
    public static final String MOD_NAME = "NpcSpawner";
    public static final String VERSION = "1.0";

    public static Logger logger;

    public static File modConfigDi;

    public static Looper mainLooper;
    public static Looper subLooper;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        modConfigDi = Paths.get(event.getModConfigurationDirectory().getAbsolutePath(), MOD_ID).toFile();
        NpcSpawnerConfig.file = Paths.get(ModMain.modConfigDi.getAbsolutePath(), "npcspawner.json").toFile();
        NpcSpawnerConfig.instance();
        logger.info("NpcSpawner: Config loaded");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PermissionAPI.registerNode("npcspawner.noMobSpawn", DefaultPermissionLevel.NONE, "Mobs will not spawn around those who has this permission");
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        subLooper = new ThreadLooper();
        mainLooper = new MainLooper();
        MinecraftForge.EVENT_BUS.register(mainLooper);
        event.registerServerCommand(new CommandNpcSpawner());
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        MinecraftForge.EVENT_BUS.unregister(mainLooper);
        subLooper.interrupt();
        subLooper = null;
        mainLooper.interrupt();
        mainLooper = null;
    }

}
