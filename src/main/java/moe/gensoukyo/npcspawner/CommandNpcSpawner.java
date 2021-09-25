package moe.gensoukyo.npcspawner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
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
                "开/关调试状态: /npcspawner debug [true|false|1|0]\n" +
                "临时世界黑名单: /npcspawner blacklist [on|off|add|rm|clr|ls] [world]";
    }

    private String playerStr(String msg) {
        return "[npcspawner]" + msg;
    }

    private String consoleStr(String msg, ICommandSender sender) {
        return "[@" + sender.getName() + "]" + msg;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        if (args.length > 0) {
            boolean isServer = sender instanceof MinecraftServer;
            Logger logger = ModMain.logger;
            if ("reload".equalsIgnoreCase(args[0])) {
                sender.sendMessage(new TextComponentString(playerStr("刷新配置中...")));
                WeakReference<ICommandSender> senderRef = new WeakReference<>(sender);
                String senderName = sender.getName();
                ThreadLooper.getLooper().add(()->{
                    NpcSpawnerConfig.reload();
                    MainLooper.START.add(()->{
                        if (!isServer) ModMain.logger.info("[@" + senderName + "]配置刷新");
                        ICommandSender weakSender = senderRef.get();
                        if (weakSender != null) {
                            weakSender.sendMessage(new TextComponentString(playerStr("配置刷新完成")));
                        }
                    });
                });
            } else if ("pause".equalsIgnoreCase(args[0])) {
                boolean last = ModMain.pauseSpawn;
                if (args.length > 1) {
                    String s = args[1];
                    ModMain.pauseSpawn = s.equalsIgnoreCase("true") || s.equals("1");
                }
                String msg = "现在的刷怪器状态: " + (ModMain.pauseSpawn ? "§c暂停" : "§a运行");
                sender.sendMessage(new TextComponentString(playerStr(msg)));
                if (last != ModMain.pauseSpawn && !isServer) logger.info(consoleStr(msg, sender));
            } else if ("debug".equalsIgnoreCase(args[0])) {
                boolean last = ModMain.debugSpawn;
                if (args.length > 1) {
                    String s = args[1];
                    ModMain.debugSpawn = s.equalsIgnoreCase("true") || s.equals("1");
                }
                String msg = "现在的调试状态: " + (ModMain.debugSpawn ? "§c开" : "§a关");
                sender.sendMessage(new TextComponentString(playerStr(msg)));
                if (last != ModMain.debugSpawn && !isServer) logger.info(consoleStr(msg, sender));
            } else if ("blacklist".equalsIgnoreCase(args[0])) {
                if (args.length > 1) {
                    if ("on".equalsIgnoreCase(args[1])) {
                        boolean last = NpcSpawner.enableBlkList;
                        NpcSpawner.enableBlkList = true;
                        String msg = "刷怪世界黑名单: §con";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (!last && !isServer) logger.info(consoleStr(msg,sender));
                    } else if ("off".equalsIgnoreCase(args[1])) {
                        boolean last = NpcSpawner.enableBlkList;
                        NpcSpawner.enableBlkList = false;
                        String msg = "刷怪世界黑名单: §aoff";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (last && !isServer) logger.info(consoleStr(msg, sender));
                    } else if ("clr".equalsIgnoreCase(args[1])) {
                        NpcSpawner.blkList.clear();
                        String msg = "刷怪世界黑名单: 名单已重置";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (!isServer) logger.info(consoleStr(msg, sender));
                    } else if ("ls".equalsIgnoreCase(args[1])) {
                        if (NpcSpawner.blkList.size() == 0) {
                            sender.sendMessage(new TextComponentString("黑名单为空"));
                        } else {
                            StringBuilder bd = new StringBuilder();
                            for (String str : NpcSpawner.blkList) bd.append(str).append(", ");
                            sender.sendMessage(new TextComponentString(bd.substring(0, bd.length() - 2)));
                        }
                    } else if ("add".equalsIgnoreCase(args[1])) {
                        if (args.length > 2) {
                            if (args[2] != null) {
                                boolean f = NpcSpawner.blkList.add(args[2]);
                                String msg = "刷怪世界黑名单已§a添加: " + args[2];
                                sender.sendMessage(new TextComponentString(playerStr(msg)));
                                if (f && !isServer) logger.info(consoleStr(msg, sender));
                            }
                        }
                    } else if ("rm".equalsIgnoreCase(args[1])) {
                        if (args.length > 2) {
                            if (args[2] != null) {
                                boolean f = NpcSpawner.blkList.remove(args[2]);
                                String msg = "刷怪世界黑名单已§c移除: " + args[2];
                                sender.sendMessage(new TextComponentString(playerStr(msg)));
                                if (f && !isServer) logger.info(consoleStr(msg, sender));
                            }
                        }
                    }
                }
                else {
                    sender.sendMessage(new TextComponentString("刷怪世界黑名单: " + (NpcSpawner.enableBlkList ? "§con" : "§aoff")));
                }
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "pause", "debug", "blacklist");
        } else if (args.length == 2) {
            if ("pause".equals(args[0]) || "debug".equalsIgnoreCase(args[0])) return getListOfStringsMatchingLastWord(args, "0", "1", "false", "true");
            else if ("blacklist".equalsIgnoreCase(args[0])) return getListOfStringsMatchingLastWord(args, "on", "off", "add", "rm", "clr", "ls");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
