package moe.gensoukyo.npcspawner;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SQwatermark
 * @author MrMks 2021/5/22 config_version 2
 */
public class NpcSpawnerConfig {

    private static NpcSpawnerConfig instance;
    public static NpcSpawnerConfig instance() {
        if(instance == null) {
            instance = new NpcSpawnerConfig();
        }
        return instance;
    }

    private static final int VERSION = 2;
    //最小刷怪距离
    public int minSpawnDistance;
    //最大刷怪距离
    public int maxSpawnDistance;
    //刷怪触发间隔,以tick计算
    public int interval;
    //刷怪区的集合
    List<NpcRegion.Spawn> mobSpawnRegions;
    //安全区的集合
    List<NpcRegion.Black> blackListRegions;

    //配置文件
    public File spawnerConfig;

    private NpcSpawnerConfig() {
        minSpawnDistance = 12;
        maxSpawnDistance = 36;
        interval = 300;
        mobSpawnRegions = new ArrayList<>();
        blackListRegions = new ArrayList<>();
        this.refresh();
    }

    public void refresh() {
        if (ModMain.modConfigDi.exists()) {
            spawnerConfig = Paths.get(ModMain.modConfigDi.getAbsolutePath(), "npcspawner.json").toFile();

            if(!spawnerConfig.exists()) {
                ModMain.logger.info("未找到NPC生成配置");
            } else if(!spawnerConfig.isDirectory()) {
                mobSpawnRegions.clear();
                blackListRegions.clear();
                try {
                    String content = FileUtils.readFileToString(spawnerConfig, StandardCharsets.UTF_8);
                    JsonElement wholeConfigElement = new JsonParser().parse(content);
                    Logger logger = ModMain.logger;
                    if (wholeConfigElement.isJsonObject()) {
                        JsonObject whoCfgObj = wholeConfigElement.getAsJsonObject();
                        int version = getNumber(whoCfgObj, "cfg_version", 1).intValue();
                        minSpawnDistance = getNumber(whoCfgObj, "minSpawnDistance", 6).intValue();
                        maxSpawnDistance = getNumber(whoCfgObj, "maxSpawnDistance", 16).intValue();
                        interval = getNumber(whoCfgObj, "interval", 20).intValue();
                        Map<String, MobTemplate> mobs =
                                whoCfgObj.has("mobs") ? parseNpcMobs(whoCfgObj.get("mobs"), logger) : Collections.emptyMap();
                        mobSpawnRegions =
                                whoCfgObj.has("groups") ? parseMobSpawnRegion(whoCfgObj.get("groups"), mobs, logger) : Collections.emptyList();
                        blackListRegions =
                                whoCfgObj.has("blacks") ? parseBlackListRegion(whoCfgObj.get("blacks"), logger) : Collections.emptyList();
                    } else {
                        logger.warn("NpcSpawner: 配置文件格式有误");
                    }
                } catch (Exception e) {
                    ModMain.logger.info("NpcSpawner：读取刷怪配置文件出错！");
                    e.printStackTrace();
                }
            }
        } else {
            ModMain.logger.error("未找到mcgproject目录");
            if (ModMain.modConfigDi.mkdir()) {
                ModMain.logger.info("已生成mcgproject目录 ");
            }
        }
        this.mobSpawnRegions.forEach(r->r.mapCoincideRegion(this.blackListRegions));
    }

    private JsonPrimitive getJsonPrimitive(JsonObject obj, String key) {
        if (obj.has(key)) {
            JsonElement element = obj.get(key);
            return element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
        }
        return null;
    }

    private Number getNumber(JsonObject obj, String key, Number def) {
        JsonPrimitive pri = getJsonPrimitive(obj, key);
        return pri != null && pri.isNumber() ? pri.getAsNumber() : def;
    }

    private String getString(JsonObject obj, String key, String def) {
        JsonPrimitive pri = getJsonPrimitive(obj, key);
        return pri != null && pri.isString() ? pri.getAsString() : def;
    }

    private boolean getBoolean(JsonObject obj, String key, boolean def) {
        JsonPrimitive pri = getJsonPrimitive(obj, key);
        return pri != null && pri.isBoolean() ? pri.getAsBoolean() : def;
    }

    private Map<String, MobTemplate> parseNpcMobs(JsonElement ele, Logger logger) {
        if (ele.isJsonObject()) {
            JsonObject obj = ele.getAsJsonObject();
            Map<String, MobTemplate> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement valE = entry.getValue();
                if (valE.isJsonObject()) {
                    JsonObject valO = valE.getAsJsonObject();
                    int tab = getNumber(valO, "tab", 6).intValue();
                    boolean inWater = getBoolean(valO, "inWater", false);
                    int timeStart = getNumber(valO, "timeStart", 0).intValue();
                    int timeEnd = getNumber(valO, "timeEnd", 24000).intValue();
                    String name = getString(valO, "name", null);
                    if (name != null) {
                        map.put(key, new MobTemplate(tab, name, inWater, timeStart, timeEnd));
                    } else {
                        logger.warn("NpcSpawner: Can't get the name of the mob under key \"" + key + "\"");
                    }
                } else {
                    logger.warn("NpcSpawner: Can't parse mob info with key \"" + key + "\"");
                }
            }
            return map;
        } else {
            logger.warn("NpcSpawner: Can't parse the \"mobs\" array");
        }
        return Collections.emptyMap();
    }

    private Number getElementNumber(JsonElement e, Number def) {
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsNumber() : def;
    }

    private Region3d parseRegion0(JsonArray ary) {
        if (ary.size() > 0 ){
            if (ary.size() > 6) {
                return new Region3d(
                        getElementNumber(ary.get(0), 0).doubleValue(),
                        getElementNumber(ary.get(1), 0).doubleValue(),
                        getElementNumber(ary.get(2), 0).doubleValue(),
                        getElementNumber(ary.get(3), 1).doubleValue(),
                        getElementNumber(ary.get(4), 1).doubleValue(),
                        getElementNumber(ary.get(5), 1).doubleValue()
                );
            } else if (ary.size() > 4) {
                return new Region3d(
                        getElementNumber(ary.get(0), 0).doubleValue(),
                        0,
                        getElementNumber(ary.get(1), 0).doubleValue(),
                        getElementNumber(ary.get(2), 0).doubleValue(),
                        255,
                        getElementNumber(ary.get(3), 0).doubleValue()

                );
            }
        }
        return null;
    }

    private List<Region3d> parseRegion(JsonObject obj, String key) {
        if (obj.has(key)) {
            JsonElement e = obj.get(key);
            if (e.isJsonArray()) {
                List<Region3d> list = new LinkedList<>();
                JsonArray ary = e.getAsJsonArray();
                for (JsonElement ele : ary) {
                    if (ele.isJsonArray()) {
                        Region3d region3d = parseRegion0(ele.getAsJsonArray());
                        if (region3d != null) list.add(region3d);
                    }
                }
                return list;
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    private Map<String, Integer> parseMobWeight(JsonElement ele) {
        if (ele.isJsonObject()) {
            JsonObject obj = ele.getAsJsonObject();
            HashMap<String, Integer> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement wE = entry.getValue();
                int w;
                if (wE.isJsonPrimitive() && wE.getAsJsonPrimitive().isNumber()) w = wE.getAsInt(); else w = 0;
                map.put(key, w);
            }

            return map;

        }
        return Collections.emptyMap();
    }

    private List<NpcRegion.Spawn> parseMobSpawnRegion(JsonElement ele, Map<String, MobTemplate> map, Logger logger) {
        if (ele.isJsonArray()) {
            JsonArray groupAry = ele.getAsJsonArray();
            Iterator<JsonElement> iterator = groupAry.iterator();
            List<NpcRegion.Spawn> list = new LinkedList<>();
            while (iterator.hasNext()) {
                JsonElement groupEle = iterator.next();
                if (groupEle.isJsonObject()) {
                    JsonObject g_obj = groupEle.getAsJsonObject();
                    String world = getString(g_obj, "world", null);
                    if (world == null) {
                        logger.warn("NpcSpawner: world can't be empty in \"groups\"");
                    } else {
                        String name = getString(g_obj, "name", "group" + System.currentTimeMillis());
                        int density = getNumber(g_obj, "density", 20).intValue();
                        List<Region3d> regions = parseRegion(g_obj, "regions");
                        List<Region3d> excludes = parseRegion(g_obj, "excludes");
                        List<Region3d> filteredRegions = regions.stream().filter(r-> excludes.stream().noneMatch(er->er.isVecInRegion(r.p1)&&er.isVecInRegion(r.p2))).collect(Collectors.toList());
                        Map<String, Integer> mobs =
                                g_obj.has("mobs") ? parseMobWeight(g_obj.get("mobs")) : Collections.emptyMap();
                        List<MobEntry> npcMobs = mobs.entrySet().stream()
                                .filter(entry->map.containsKey(entry.getKey()))
                                .map(entry->new MobEntry(map.get(entry.getKey()), entry.getValue()))
                                .collect(Collectors.toList());
                        list.add(new NpcRegion.Spawn(name, world, filteredRegions, excludes, npcMobs, density));
                    }
                }
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    private List<NpcRegion.Black> parseBlackListRegion(JsonElement ele, Logger logger) {
        if (ele.isJsonArray()) {
            JsonArray blackAry = ele.getAsJsonArray();
            List<NpcRegion.Black> list = new LinkedList<>();
            for (JsonElement blackEle : blackAry) {
                if (blackEle.isJsonObject()) {
                    JsonObject b_obj = blackEle.getAsJsonObject();
                    String world = getString(b_obj, "world", null);
                    if (world == null) {
                        logger.warn("NpcSpawner: world can't be null or empty in \"blacks\"");
                    } else {
                        String name = getString(b_obj, "name", "black" + System.currentTimeMillis());
                        List<Region3d> regions = parseRegion(b_obj, "regions");
                        boolean delete = getBoolean(b_obj, "delete", false);
                        list.add(new NpcRegion.Black(name, world, regions, delete));
                    }
                }
            }
            return ImmutableList.copyOf(list);
        } else {
            logger.warn("");
        }
        return Collections.emptyList();
    }
}