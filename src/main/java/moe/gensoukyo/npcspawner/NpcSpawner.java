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
import net.minecraftforge.server.permission.context.PlayerContext;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author SQwatermark
 */
@Mod.EventBusSubscriber
public class NpcSpawner {

    static NpcSpawnerConfig config;
    private static Random random = new Random();

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (random.nextInt(config.interval) == 0) {
            tryToSpawnMob((WorldServer) event.world);
        }
    }

    public static void tryToSpawnMob(WorldServer worldServer) {

        //随便找几个倒霉鬼
        List<EntityPlayer> list = worldServer.playerEntities;
        if (list.size() == 0) {
            return;
        }
        label:
        for (int i = 0; i < list.size() / 4 + 1; i++) {
            EntityPlayer player = list.get(random.nextInt(list.size()));
            if (PermissionAPI.hasPermission(player.getGameProfile(), "npcspawner.noMobSpawn", new PlayerContext(player))) {
                continue;
            }
            //在这个倒霉鬼周围随便找个地方
            int r = config.minSpawnDistance + random.nextInt(config.maxSpawnDistance - config.minSpawnDistance);
            double angle = Math.toRadians(random.nextInt(360));
            double x = player.posX + r * Math.cos(angle);
            double z = player.posZ + r * Math.sin(angle);
            //自下而上找个高度
            int y = 1;
            for (int j = 0; j < 255; j++) {
                if (worldServer.getBlockState(new BlockPos(x, j, z)).getBlock() == Blocks.AIR) {
                    y = j;
                    break;
                }
            }
            if (y > player.posY + 10) {
                continue;
            }
            Vec3d place = new Vec3d(x, y, z);

            //选中的地点在不在某个刷怪区里
            for (NpcRegion.MobSpawnRegion mobSpawnRegion : config.mobSpawnRegions) {
                //判断世界
                if (!worldServer.getWorldInfo().getWorldName().toLowerCase().equals(mobSpawnRegion.world.toLowerCase())) {
                    continue;
                }
                //判断位置
                Vec2d vec2d = new Vec2d(place.x, place.z);
                //如果选中的地点周围怪太多，则不能生成
                if (worldServer.getEntitiesWithinAABB(EntityCustomNpc.class,
                        new AxisAlignedBB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50)).size() > mobSpawnRegion.density) {
                    continue;
                }
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
                            NpcAPI.Instance().getClones().spawn(place.x, place.y, place.z,
                                    mob.tab, mob.name, NpcAPI.Instance().getIWorld(((EntityPlayerMP)player).getServerWorld()));
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
        ArrayList<Integer> weights = new ArrayList<>();
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
            for (NpcMob properMob : properMobs) {
                weights.add((int)(properMob.weight * 100));
            }
            return properMobs.get(random(weights));
        } else {
            return null;
        }
    }

    /**
     * 权重随机数
     * @param weight
     * @return 索引值
     */
    public static int random(List<Integer> weight) {
        List<Integer> weightTmp = new ArrayList<>(weight.size() + 1);
        weightTmp.add(0);
        Integer sum = 0;
        for(Integer d : weight){
            sum += d;
            weightTmp.add(sum);
        }
        int rand = random.nextInt(sum);
        int index = 0;
        for(int i = weightTmp.size()-1; i >0; i--){
            if( rand >= weightTmp.get(i)){
                index = i;
                break;
            }
        }
        return index;
    }
}
