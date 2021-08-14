package moe.gensoukyo.npcspawner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author SQwatermark
 */
public class NpcSpawnerConfig {

    private static NpcSpawnerConfig instance;
    public static NpcSpawnerConfig instance() {
        if(instance == null) {
            instance = new NpcSpawnerConfig();
        }
        return instance;
    }

    //最小刷怪距离
    public int minSpawnDistance;
    //最大刷怪距离
    public int maxSpawnDistance;
    //刷怪触发间隔,以tick计算
    public int interval;
    //刷怪区的集合
    Map<String, List<NpcRegion.MobSpawnRegion>> mobSpawnRegions;
    //安全区的集合
    Map<String, List<NpcRegion.BlackListRegion>> blackListRegions;

    //配置文件
    public File spawnerConfig;

    private NpcSpawnerConfig() {
        minSpawnDistance = 12;
        maxSpawnDistance = 36;
        interval = 300;
        mobSpawnRegions = new HashMap<>();
        blackListRegions = new HashMap<>();
        this.refresh();
    }

    public void refresh() {
        if (ModMain.modConfigDi.exists()) {
            spawnerConfig = new File(ModMain.modConfigDi, "npcspawner.json");

            if(!spawnerConfig.exists()) {
                ModMain.logger.info("未找到NPC生成配置");
            } else if(!spawnerConfig.isDirectory()) {
                mobSpawnRegions.clear();
                blackListRegions.clear();
                try {
                    String content = FileUtils.readFileToString(spawnerConfig, StandardCharsets.UTF_8);
                    JsonParser parser = new JsonParser();
                    JsonObject npcSpawnerConfigJson = (JsonObject) parser.parse(content);
                    minSpawnDistance = npcSpawnerConfigJson.get("minSpawnDistance").getAsInt();
                    maxSpawnDistance = npcSpawnerConfigJson.get("maxSpawnDistance").getAsInt();
                    interval = npcSpawnerConfigJson.get("interval").getAsInt();
                    JsonArray array = npcSpawnerConfigJson.get("mobSpawnRegions").getAsJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject mobSpawnRegionJson = array.get(i).getAsJsonObject();
                        NpcRegion.MobSpawnRegion mobSpawnRegion = parseMobSpawnRegion(mobSpawnRegionJson);
                        if (mobSpawnRegion != null) {
                            List<NpcRegion.MobSpawnRegion> list = mobSpawnRegions.computeIfAbsent(mobSpawnRegion.world, k -> new ArrayList<>());
                            list.add(mobSpawnRegion);
                        }
                    }
                    try {
                        JsonArray array2 = npcSpawnerConfigJson.get("blackListRegions").getAsJsonArray();
                        for (int i = 0; i < array2.size(); i++) {
                            JsonObject blackListRegionJson = array2.get(i).getAsJsonObject();
                            NpcRegion.BlackListRegion blackListRegion = parseBlackListRegion(blackListRegionJson);
                            if (blackListRegion != null) {
                                List<NpcRegion.BlackListRegion> list = blackListRegions.computeIfAbsent(blackListRegion.world, k -> new ArrayList<>());
                                list.add(blackListRegion);
                            }
                        }
                    } catch (Exception e) {
                        ModMain.logger.info("MCGProject：未找到黑名单区域的配置");
                        e.printStackTrace();
                    }


                } catch (Exception e) {
                    ModMain.logger.info("MCGProject：读取刷怪配置文件出错！");
                    e.printStackTrace();
                }
            }
        } else {
            ModMain.logger.error("未找到mcgproject目录");
            if (ModMain.modConfigDi.mkdir()) {
                ModMain.logger.info("已生成mcgproject目录 ");
            }
        }
        for (Map.Entry<String, List<NpcRegion.MobSpawnRegion>> e : this.mobSpawnRegions.entrySet()) {
            List<NpcRegion.MobSpawnRegion> spList = e.getValue();
            List<NpcRegion.BlackListRegion> bkList = this.blackListRegions.get(e.getKey());
            if (bkList != null && !bkList.isEmpty()) {
                for (NpcRegion.MobSpawnRegion mobRegion : spList) {
                    for (NpcRegion.BlackListRegion blackRegion : bkList) {
                        if (mobRegion.region.isCoincideWith(blackRegion.region)) mobRegion.blackList.add(blackRegion);
                    }
                }
            }
        }
    }

    public NpcRegion.MobSpawnRegion parseMobSpawnRegion(JsonObject mobSpawnRegionJson) {
        try {
            String name = mobSpawnRegionJson.get("name").getAsString();
            String world = mobSpawnRegionJson.get("world").getAsString();
            JsonArray pos1 = mobSpawnRegionJson.get("pos1").getAsJsonArray();
            JsonArray pos2 = mobSpawnRegionJson.get("pos2").getAsJsonArray();
            int density = mobSpawnRegionJson.get("density").getAsInt();
            JsonArray mobs = mobSpawnRegionJson.get("mobs").getAsJsonArray();
            return new NpcRegion.MobSpawnRegion(name, new Region2d(pos1.get(0).getAsDouble(),
                    pos1.get(1).getAsDouble(), pos2.get(0).getAsDouble(), pos2.get(1).getAsDouble()), density, parseNpcMobs(mobs), world);
        } catch (Exception e) {
            ModMain.logger.error("MCGProject：刷怪配置解析错误！刷怪区信息不符合规范！已跳过该刷怪区！");
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<NpcMob> parseNpcMobs(JsonArray mobsJson) {
        ArrayList<NpcMob> npcMobs = new ArrayList<>();
        for (int i = 0; i < mobsJson.size(); i++) {
            JsonObject mobJson = mobsJson.get(i).getAsJsonObject();
            NpcMob mob = parseNpcMob(mobJson);
            if (mob != null && mob.weight > 0) {
                npcMobs.add(mob);
            }
        }
        return npcMobs;
    }

    @Nullable
    public NpcMob parseNpcMob(JsonObject mobJson) {
        try {
            int tab = mobJson.get("tab").getAsInt();
            String mobName = mobJson.get("name").getAsString();
            double weight = mobJson.get("weight").getAsDouble();
            NpcMob mob = new NpcMob(tab, mobName, weight);
            if (mobJson.has("waterMob")) {
                mob.setWaterMob(mobJson.get("waterMob").getAsBoolean());
            }
            if (mobJson.has("timeStart") && mobJson.has("timeEnd")) {
                mob.setTimeIndex(mobJson.get("timeStart").getAsInt(), mobJson.get("timeEnd").getAsInt());
            }
            return mob;
        } catch (Exception e) {
            ModMain.logger.error("MCGProject：刷怪配置解析错误！怪物信息不符合规范！已跳过该怪物！");
            e.printStackTrace();
            return null;
        }
    }

    public NpcRegion.BlackListRegion parseBlackListRegion(JsonObject blackListRegionJson) {
        try {
            String name = blackListRegionJson.get("name").getAsString();
            JsonArray pos1 = blackListRegionJson.get("pos1").getAsJsonArray();
            JsonArray pos2 = blackListRegionJson.get("pos2").getAsJsonArray();
            String world = blackListRegionJson.get("world").getAsString();
            return new NpcRegion.BlackListRegion(name, new Region2d(pos1.get(0).getAsDouble(),
                    pos1.get(1).getAsDouble(), pos2.get(0).getAsDouble(), pos2.get(1).getAsDouble()), true, world);
        } catch (Exception e) {
            ModMain.logger.error("MCGProject：刷怪配置解析错误！安全区信息不符合规范！已跳过该安全区！");
            e.printStackTrace();
            return null;
        }
    }
}