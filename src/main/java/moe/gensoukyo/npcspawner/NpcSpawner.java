package moe.gensoukyo.npcspawner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author SQwatermark
 */
@Mod.EventBusSubscriber
public class NpcSpawner {

    static NpcSpawnerConfig config;
    private static final Random random = new Random();
    // the two cache array will occupy total 45Kb of memories: 8 Byte/double * 360 double / array * 2 array = 5760Byte = 46080 bits = 45k bits
    private static final double[] ccos = new double[360];
    private static final double[] csin = new double[360];

    public static HashSet<String> blkList = new HashSet<>();
    public static boolean enableBlkList = false;

    private static int remainingTick = -1;

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote
                || event.phase == TickEvent.Phase.END
                || CustomNpcs.FreezeNPCs
                || ModMain.pauseSpawn) return;
        remainingTick --;
        if (remainingTick < 0) {
            tryToSpawnMob((WorldServer) event.world);
            remainingTick = config.intervalMin + random.nextInt(config.intervalLength);
        }
    }

    public static void tryToSpawnMob(WorldServer worldServer) {
        String worldName = worldServer.getWorldInfo().getWorldName();
        if (enableBlkList && blkList.contains(worldName)) return;
        List<NpcRegion.MobSpawnRegion> regions = config.mobSpawnRegions.get(worldName);
        if (regions == null || regions.isEmpty()) return;

        //随便找几个倒霉鬼
        List<EntityPlayer> list = worldServer.playerEntities.stream()
                .filter(p->!PermissionAPI.hasPermission(p, "npcspawner.noMobSpawn"))
                .filter(p->((EntityPlayerMP)p).interactionManager.survivalOrAdventure() || ModMain.debugSpawn)
                .collect(Collectors.toList());
        if (list.size() == 0) {
            return;
        }
        label:
        for (int i = random.nextInt(4); i < list.size(); i += 4) {
            EntityPlayer player = list.get(i);
            //在这个倒霉鬼周围随便找个地方
            int r = config.minSpawnDistance + random.nextInt(config.maxSpawnDistance - config.minSpawnDistance);
            int angle = random.nextInt(360);
            if (ccos[angle] == 0 && csin[angle] == 0) {
                double rad = Math.toRadians(angle);
                ccos[angle] = Math.cos(rad);
                csin[angle] = Math.sin(rad);
            }
            double x = player.posX + r * ccos[angle];
            double z = player.posZ + r * csin[angle];
            double y;
            //自下而上找个高度
            double minY = Math.max(player.posY - 10, 0);
            double maxY = Math.min(player.posY + 10, 255);
            boolean findY = false;
            for (y = minY; y < maxY + 1 && !findY; y++) {
                findY = worldServer.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR;
            }
            if (!findY) continue;
            Vec3d place = new Vec3d(x, y, z);

            //选中的地点在不在某个刷怪区里
            for (NpcRegion.MobSpawnRegion mobSpawnRegion : regions) {
                //判断位置
                Vec2d vec2d = new Vec2d(place.x, place.z);
                //如果选中的地点周围怪太多，则不能生成
                if (worldServer.getEntitiesWithinAABB(EntityCustomNpc.class,
                        new AxisAlignedBB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50)).size() >= mobSpawnRegion.density) {
                    continue;
                }
                if (anyPlayerInDistance(list, i, x, y, z, config.minSpawnDistance, config.minSpawnDisPow)) continue label;
                //要在刷怪区内
                if (mobSpawnRegion.region.isVecInRegion(vec2d) && mobSpawnRegion.region.isVecInRegion(new Vec2d(player.posX, player.posZ))) {
                    //如果在黑名单内，则不刷怪
                    for (NpcRegion.BlackListRegion blackListRegion : mobSpawnRegion.blackList) {
                        if (blackListRegion.region.isVecInRegion(vec2d)) {
                            continue label;
                        }
                        if (blackListRegion.region.isVecInRegion(new Vec2d(player.posX, player.posZ))) {
                            continue label;
                        }
                    }
                    //如果在刷怪区且不在黑名单内，随便挑一个怪物生成
                    boolean inWater = worldServer.getBlockState(new BlockPos(place.x, place.y - 1, place.z)).getBlock() == Blocks.WATER;
                    int timeOfDay = (int)(worldServer.getWorldTime() % 24000);
                    NpcMob mob = chooseMobToSpawn(mobSpawnRegion, inWater, timeOfDay);
                    if (mob != null) {
                        try {
                            IEntity<?> entity = NpcAPI.Instance().getClones().spawn(place.x, place.y, place.z,
                                    mob.tab, mob.name, NpcAPI.Instance().getIWorld(worldServer));
                            if (entity instanceof ICustomNpc<?>) {
                                ICustomNpc<?> icn = (ICustomNpc<?>) entity;
                                if (icn.getStats().getRespawnType() < 3) icn.getStats().setRespawnType(4);
                            }
                        } catch (Exception e) {
                            ModMain.logger.info("NpcSpawner：NPC[" + mob.name + "]生成失败，可能是配置文件中提供的信息有误");
                        }
                    }
                }
            }
        }
    }

    /**
     * 按权重随机挑一个NPC怪物
     * @param mobSpawnRegion 刷怪区
     * @return 选出的NPC
     */
    @Nullable
    public static NpcMob chooseMobToSpawn(NpcRegion.MobSpawnRegion mobSpawnRegion, boolean inWater, int time) {
        ArrayList<NpcMob> properMobs = new ArrayList<>();
        for (NpcMob mob : mobSpawnRegion.mobs) {
            if (mob.waterMob == inWater) {
                if (mob.timeStart < mob.timeEnd) {
                    if (mob.timeStart <= time && time < mob.timeEnd) {
                        properMobs.add(mob);
                    }
                } else {
                    if (time >= mob.timeStart || time < mob.timeEnd) {
                        properMobs.add(mob);
                    }
                }
            }
        }
        if (properMobs.size() > 0) {
            int sum = 0, i = 0, size = properMobs.size();
            int[] weights = new int[size + 1];
            weights[i++] = 0;
            for (NpcMob properMob : properMobs) {
                int w = properMob.weight;
                sum += w;
                weights[i++] = sum;
            }
            int rnd = random.nextInt(sum);
            int bi = 0, ei = size - 1;
            if (weights[bi + 1] > rnd) i = bi;
            else if (weights[ei] <= rnd) i = ei;
            else while (ei - bi > 1) {
                i = bi + (ei - bi) / 2;
                int w = weights[i], w1 = weights[i + 1];
                if (rnd < w) ei = i;
                else if (rnd >= w1) bi = i;
                else break;
            }
            return properMobs.get(i);
        } else {
            return null;
        }
    }

    public static boolean anyPlayerInDistance(List<EntityPlayer> src, int playerIn, double x, double y, double z, double min, double d2) {
        for (int i = 0; i < src.size(); i++) {
            if (i == playerIn) continue;
            EntityPlayer p = src.get(i);
            double dx = abs(p.posX - x);
            double dy = abs(p.posY - y);
            double dz = abs(p.posZ - z);
            if (dx > min || dy > min || dz > min) continue;
            double ds = dx * dx + dy * dy + dz * dz;
            if (ds < d2) return true;
        }
        return false;
    }

    public static double abs(double a) {
        return a < 0 ? -a : a;
    }
}
