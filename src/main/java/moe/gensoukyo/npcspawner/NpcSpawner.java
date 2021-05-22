package moe.gensoukyo.npcspawner;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author SQwatermark
 * @author MrMks 2021/5/22
 */
@Mod.EventBusSubscriber
public class NpcSpawner {

    static NpcSpawnerConfig config;
    private static Random random = new Random();
    private static boolean spawnedLastTime = true;

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (!spawnedLastTime || random.nextInt(config.interval) == 0) {
            spawnedLastTime = tryToSpawnMob((WorldServer) event.world);
        }
    }

    // 返回false时，下一次尝试刷怪的时间将为下一游戏刻。
    public static boolean tryToSpawnMob(WorldServer worldServer) {
        boolean spawned = false;

        //随便找几个倒霉鬼
        List<EntityPlayer> list = worldServer.playerEntities;
        if (list.size() == 0) {
            return true;
        }
        for (int i = 0; i < list.size() / 4 + 1; i++) {
            EntityPlayer player = list.get(random.nextInt(list.size()));
            if (PermissionAPI.hasPermission(player.getGameProfile(), "npcspawner.noMobSpawn", new PlayerContext(player))) {
                continue;
            }
            if (!(((EntityPlayerMP) player).interactionManager.getGameType().isSurvivalOrAdventure())) continue;

            //在这个倒霉鬼周围随便找个地方
            int r = config.minSpawnDistance + random.nextInt(config.maxSpawnDistance - config.minSpawnDistance);
            double x = player.posX + r * random.nextDouble();
            double z = player.posZ + r * random.nextDouble();
            //从玩家所在高度开始向下，上找5个高度
            double y = player.posY;
            if (!isPosBlockIsAir(worldServer, x, y, z)) {
                double ry = -1, ty;
                for (int j = 1; j < 6; j++) {
                    if (isPosBlockIsAir(worldServer, x, ty = y - j, z)) ry = ty;
                    else if (isPosBlockIsAir(worldServer, x, ty = y + j, z)) ry = ty;
                    if (ry > 0) break;
                }
                if (ry > 0) y = ry;
                else continue;
            }
            Vec3d place = new Vec3d(x, y, z);

            NpcRegion.Spawn region = findSpawnRegion(config.mobSpawnRegions, place, worldServer.getWorldInfo().getWorldName());
            if (region == null) continue;
            //如果选中的地点周围怪太多，则不能生成
            if (worldServer.getEntitiesWithinAABB(EntityCustomNpc.class,
                    new AxisAlignedBB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50)).size() > region.density) {
                continue;
            }

            //如果在刷怪区且不在黑名单内，随便挑一个怪物生成
            boolean inWater = worldServer.getBlockState(new BlockPos(place.x, place.y - 1, place.z)).getBlock() == Blocks.WATER;
            int timeOfDay = (int) (worldServer.getWorldTime() % 24000);
            MobEntry mob = chooseMobToSpawn(region, inWater, timeOfDay);
            if (mob != null && NpcAPI.IsAvailable()) {
                try {
                    NpcAPI api = NpcAPI.Instance();
                    IEntity<?> entity = api.getClones().spawn(place.x, place.y, place.z,
                            mob.template.tab, mob.template.name, api.getIWorld(((EntityPlayerMP) player).getServerWorld()));
                    if (entity instanceof ICustomNpc<?>) {
                        ICustomNpc<?> customNpc = (ICustomNpc<?>) entity;
                        customNpc.getStats().setRespawnType(4);
                    }
                    spawned = true;
                } catch (Exception e) {
                    ModMain.logger.info("NpcSpawner：NPC[" + mob.template.name + "]生成失败，可能是配置文件中提供的信息有误");
                }
            }
        }
        return spawned;
    }

    @Nullable
    private static NpcRegion.Spawn findSpawnRegion(List<NpcRegion.Spawn> list, Vec3d pos, String world) {
        SimpleVec3d sVec = new SimpleVec3d(pos);
        for (NpcRegion.Spawn region : list) {
            if (world.equalsIgnoreCase(region.world) && region.isVecInRegion(sVec)) return region;
        }
        return null;
    }

    /**
     * 检查目标坐标上的方块是否为AIR
     * @param world 检查进行的世界
     * @return true 如果 目标坐标上的方块是AIR
     */
    private static boolean isPosBlockIsAir(World world, double x, double y, double z) {
        return world.getBlockState(new BlockPos(x,y,z)).getBlock() == Blocks.AIR;
    }

    public static int lastSum = -1;
    /**
     * 按权重随机挑一个NPC怪物
     * @param mobSpawnRegion 刷怪区
     * @return 选出的NPC
     */
    @Nullable
    public static MobEntry chooseMobToSpawn(NpcRegion.Spawn mobSpawnRegion, boolean inWater, int time) {
        List<MobEntry> properMobs = new LinkedList<>();
        for (MobEntry mobE : mobSpawnRegion.mobs) {
            if (lastSum == 0) mobE.resetWeight();
            MobTemplate mob = mobE.template;
            if (mob.inWater == inWater) {
                if (mob.timeStart <= time && time <= mob.timeEnd) {
                    if (mobE.getWeightSilence() > 0) properMobs.add(mobE);
                }
            }
        }
        if (properMobs.size() > 0) {
            IntArrayList weights = new IntArrayList();
            for (MobEntry properMob : properMobs) {
                weights.add(properMob.getWeight());
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
        List<Integer> weightTmp = new IntArrayList(weight.size() + 1);
        weightTmp.add(0);
        int sum = 0;
        for(int d : weight){
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
        lastSum = sum;
        return index;
    }
}
