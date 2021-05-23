package moe.gensoukyo.npcspawner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
            if (args[0].equalsIgnoreCase("reload")) {
                NpcSpawnerConfig.reload(()->sender.sendMessage(new TextComponentString("NpcSpawner: Reloaded")));
            } else if (args[0].equalsIgnoreCase("info")) {
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
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> list;
        if (args.length == 1) {
            list = new ArrayList<>(2);
            if("reload".startsWith(args[0])) list.add("reload");
            if("info".startsWith(args[0])) list.add("info");
        } else list = Collections.emptyList();
        return list;
    }
}
