package moe.gensoukyo.npcspawner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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
        return "重载NPC配置文件：/npcspawner reload\n" +
                "暂停/恢复刷怪: /npcspawner pause [true|false|1|0]\n" +
                "开/关调试状态: /npcspawner debug [true|false|1|0]";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0])) {
                NpcSpawnerConfig.instance().refresh();
                sender.sendMessage(new TextComponentString("已刷新，请通过控制台查看具体信息！"));
                if (!(sender instanceof MinecraftServer)) {
                    ModMain.logger.info("配置刷新");
                }
            } else if ("pause".equalsIgnoreCase(args[0])) {
                if (args.length > 1) {
                    String s = args[1];
                    ModMain.pauseSpawn = s.equalsIgnoreCase("true") || s.equals("1");
                }
                String msg = "现在的刷怪器状态: " + (ModMain.pauseSpawn ? "§c暂停" : "§a运行");
                sender.sendMessage(new TextComponentString(msg));
                if (!(sender instanceof MinecraftServer)) ModMain.logger.info(msg);
            } else if ("debug".equalsIgnoreCase(args[0])) {
                if (args.length > 1) {
                    String s = args[1];
                    ModMain.debugSpawn = s.equalsIgnoreCase("true") || s.equals("1");
                }
                String msg = "现在的调试状态: " + (ModMain.debugSpawn ? "§c开" : "§a关");
                sender.sendMessage(new TextComponentString(msg));
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "pause", "debug");
        } else if (args.length == 2) {
            if ("pause".equals(args[0]) || "debug".equalsIgnoreCase(args[0])) return getListOfStringsMatchingLastWord(args, "0", "1", "false", "true");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
