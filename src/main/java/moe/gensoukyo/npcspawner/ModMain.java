package moe.gensoukyo.npcspawner;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
    public static final String VERSION = "1.0.1";

    public static Logger logger;

    public static File modConfigDi;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        modConfigDi = Paths.get(event.getModConfigurationDirectory().getAbsolutePath(), MOD_ID).toFile();
        NpcSpawner.config = NpcSpawnerConfig.instance();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PermissionAPI.registerNode("npcspawner.noMobSpawn", DefaultPermissionLevel.NONE, "Mobs will not spawn around those who has this permission");
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNpcSpawner());
    }

}
