package moe.gensoukyo.npcspawner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author SQwatermark
 */
public class CommandNpcSpawner extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "npcspawner";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "重载NPC配置文件：/npcspawner reload \n 查看当前位置划分区域: /npcspawner info";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0])) {
                NpcSpawnerConfig.reload(()->sender.sendMessage(new TextComponentString("NpcSpawner: Reloaded")));
            } else if ("info".equalsIgnoreCase(args[0])) {
                ModMain.subLooper.offer(()->{
                    List<NpcRegion.Spawn> list = NpcSpawnerConfig.instance().getMobSpawnRegions();
                    BlockPos pos = sender.getPosition();
                    SimpleVec3d vec = new SimpleVec3d(pos.getX(), pos.getY(), pos.getZ());
                    NpcRegion.Spawn spawn = null;
                    for (NpcRegion.Spawn rg : list) {
                        if (rg.world.equalsIgnoreCase(sender.getEntityWorld().getWorldInfo().getWorldName()) && rg.isVecInRegion(vec)) {
                            spawn = rg;
                            break;
                        }
                    }
                    String msg;
                    if (spawn != null) {
                        msg = "The region you are in is: " + spawn.name + "\nSee config file for details";
                    } else {
                        msg = "You are not in any spawning region";
                    }
                    ModMain.mainLooper.offer(()->sender.sendMessage(new TextComponentString(msg)));
                });
            } else if ("debug".equalsIgnoreCase(args[0])) {
                if (args.length != 1) {
                    NpcSpawner.debugging = !("0".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1]));
                }
                String msg = "The status of debug mode: \u00A7" + (NpcSpawner.debugging ? "aenabled" : "cdisabled");
                TextComponentString text = new TextComponentString(msg);
                sender.sendMessage(text);
                ModMain.logger.info(msg);
            } else if ("pause".equalsIgnoreCase(args[0])) {
                if (args.length != 1) {
                    NpcSpawner.pausing = !("0".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1]));
                }
                String msg = "The spawning is \u00A7" + (NpcSpawner.pausing ? "cpausing" : "arunning") + "\u00A7r now";
                TextComponentString text = new TextComponentString(msg);
                sender.sendMessage(text);
                ModMain.logger.info(msg);
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "info", "debug", "pause");
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "false", "true");
        } else return Collections.emptyList();
    }
}
