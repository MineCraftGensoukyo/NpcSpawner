package moe.gensoukyo.npcspawner;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author SQwatermark
 * @author MrMks 2021/5/22
 */
@Mod.EventBusSubscriber
public class NpcSpawner {

    private static Random random = new Random();
    private static boolean spawnedLastTime = true;

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        NpcSpawnerConfig config = NpcSpawnerConfig.instance();
        if (!spawnedLastTime || random.nextInt(config.getInterval()) == 0) {
            spawnedLastTime = tryToSpawnMob((WorldServer) event.world, config);
        }
    }

    // 返回false时，下一次尝试刷怪的时间将为下一游戏刻。
    public static boolean tryToSpawnMob(WorldServer worldServer, NpcSpawnerConfig config) {
        //随便找几个倒霉鬼
        List<EntityPlayer> list = new LinkedList<>(worldServer.playerEntities);
        if (list.size() == 0) {
            return true;
        }

        boolean spawned = false;
        boolean normal_checked = false;
        int size = list.size() / 4 + 1;
        for (int i = 0; i < size; i++) {
            EntityPlayer player = list.get(random.nextInt(list.size()));
            if (PermissionAPI.hasPermission(player.getGameProfile(), "npcspawner.noMobSpawn", new PlayerContext(player))) {
                list.remove(player);
                size = Math.min(list.size(), size + 1);
                continue;
            }
            if (!(((EntityPlayerMP) player).interactionManager.getGameType().isSurvivalOrAdventure())) {
                list.remove(player);
                size = Math.min(list.size(), size + 1);
                continue;
            }

            //在这个倒霉鬼周围随便找个地方
            double x = player.posX + calR(config.getMinSpawnDistance(), config.getMaxSpawnDistance());
            double z = player.posZ + calR(config.getMinSpawnDistance(), config.getMaxSpawnDistance());
            //从玩家所在高度开始向下，上找5个高度
            double y = findPosY(worldServer, x, player.posY, z);
            if (y < 0) continue;

            Vec3d place = new Vec3d(x, y, z);

            NpcRegion.Spawn region = findSpawnRegion(config.getMobSpawnRegions(), place, worldServer.getWorldInfo().getWorldName());
            if (region == null) continue;
            //如果选中的地点周围怪太多，则不能生成
            if (worldServer.getEntitiesWithinAABB(EntityCustomNpc.class,
                    new AxisAlignedBB(x - 50, y - 50, z - 50, x + 50, y + 50, z + 50)).size() > region.density) {
                continue;
            }

            normal_checked = true;
            //如果在刷怪区且不在黑名单内，随便挑一个怪物生成
            boolean inWater = worldServer.getBlockState(new BlockPos(place.x, place.y - 1, place.z)).getBlock() == Blocks.WATER;
            int timeOfDay = (int) (worldServer.getWorldTime() % 24000);
            MobEntry mob = chooseMobToSpawn(region, inWater, timeOfDay);
            if (mob != null && NpcAPI.IsAvailable()) {
                try {
                    NpcAPI api = NpcAPI.Instance();
                    IEntity<?> entity = api.getClones().spawn(place.x, place.y, place.z,
                            mob.template.tab, mob.template.name, api.getIWorld(((EntityPlayerMP) player).getServerWorld()));
                    if (entity != null) {
                        if (entity instanceof ICustomNpc<?>) {
                            ICustomNpc<?> customNpc = (ICustomNpc<?>) entity;
                            customNpc.getStats().setRespawnType(4);
                        }
                        mob.reduceWeight();
                        spawned = true;
                    }
                } catch (Exception e) {
                    ModMain.logger.info("NpcSpawner：NPC[" + mob.template.name + "]生成失败，可能是配置文件中提供的信息有误");
                }
            }
        }
        return !normal_checked || spawned;
    }

    private static double calR(double min, double max) {
        double r = random.nextDouble();
        return ((max - min) * r + min) * (r * 2 - 1 > 0 ? 1 : -1);
    }

    @Nullable
    private static NpcRegion.Spawn findSpawnRegion(List<NpcRegion.Spawn> list, Vec3d pos, String world) {
        SimpleVec3d sVec = new SimpleVec3d(pos.x, pos.y, pos.z);
        for (NpcRegion.Spawn region : list) {
            if (world.equalsIgnoreCase(region.world) && region.isVecInRegion(sVec)) return region;
        }
        return null;
    }

    private static double findPosY(World world, double x, double y, double z) {
        int min = -Math.max(Math.min((int) y - 1, 10),0);
        int max = Math.max(Math.min(255 - (int)y, 10),0);
        boolean[] ary = new boolean[max - min];
        for (int i = min; i < max; i++) {
            int o = i <= 0 ? min - i : i;
            IBlockState blockS = world.getBlockState(new BlockPos(x,y + o,z));
            Block block = blockS.getBlock();
            ary[o - min] = Blocks.AIR.equals(block) || Blocks.WATER.equals(block);
            if (i > min) {
                if (i <= 0) {
                    if (!ary[o-min] && ary[o-min+1]) return y + o + 1;
                } else {
                    if (ary[o-min] && !ary[o-min-1]) return y + o;
                }
            }
        }
        return -1;
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
            if (lastSum == 0 && mobE.isWeightEmpty()) mobE.resetWeight();
            MobTemplate mob = mobE.template;
            if (mob.inWater == inWater) {
                if (mob.timeStart <= time && time <= mob.timeEnd) {
                    if (!mobE.isWeightEmpty()) properMobs.add(mobE);
                }
            }
        }
        if (properMobs.size() > 0) {
            MobEntry entry;
            if (properMobs.size() == 1) {
                entry = properMobs.get(0);
                lastSum = entry.getWeight();
            } else {
                IntArrayList weights = new IntArrayList();
                for (MobEntry properMob : properMobs) {
                    weights.add(properMob.getWeight());
                }
                entry = properMobs.get(random(weights));
            }
            return entry;
        } else {
            lastSum = 0;
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
