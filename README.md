# NpcSpawner

## 配置介绍
### 总览
```json
{
  "version": 2,
  "minSpawnDistance": 12,
  "maxSpawnDistance": 36,
  "minInterval": 150,
  "maxInterval": 300,
  "mobs": {
    "key1": {"tab": 6, "name": "nameOfSavedMob1"},
    "key2": {"tab": 6, "name": "nameOfSavedMob2"}
  },
  "groups": [
    {
      "name": "groupA",
      "world": "worldA",
      "regions": [
        [500,100,500,600,150,600],[600,600,700,700]
      ],
      "exclude": [
        [0,0,0,0,0,0]
      ],
      "mobs": {
        "key1": 10,
        "key2": 20
      }
    }
  ],
  "blacks": [
    {
      "name": "blackA",
      "world": "worldA",
      "regions": [
        [0,0,0,0,0,0]
      ]
    }
  ]
}

```

|key|type|name|description|optional & default|
|:---|:---|:---|:---|:---|
|version|int|版本|自动生成的版本配置文件版本号，请勿手动更改|true, 1|
|minSpawnDistance|int|最小生成距离|生成的最小距离|true, 12|
|maxSpawnDistance|int|最大生成距离|生成的最大距离|true, 36|
|minInterval|int|最小生成时间间隔|两次npc生成之间的最小时间间隔|true, 150|
|maxInterval|int|最大生存时间间隔|两次npc生成之间的最大时间间隔|true, 300|
|mobs|\{\}|Mobs|Mob列表|true, {}|
|groups|\[\{\}\]|生成区域|Npc生成的区域|true, \{\}|
|blacks|\[\{\}\]|生成区黑名单|Npc生成的黑名单|true, \{\}|

### mobs
mobs配置使用一种形如\{"key1": \{\}\}的配置格式进行。
其中，"key1"可以用在groups中指示对象group所包含的mob信息.
而\{\}中的配置如下表:

|key|type|name|description|optional & default|
|:---|:---|:---|:---|:---|
|name|string|代称|一个用于显示的代称，方便开发者|false|
|tab|int|TAB|保存的npc的tab|true, 6|
|inWater|boolean|水中生成|该npc是否可以在水中生成|true, false|
|timeStart|int|开始生成时间|该NPC在一MC日中的开始生成时间|true, 0|
|timeEnd|int|生成结束时间|该NPC在一MC日中结束生成的时间|true, 24000|

### groups
groups配置使用一种形如\[\{\}\]的格式进行配置，其中{}中的配置如下表所示:

|key|type|name|description|optional & default|
|:---|:---|:---|:---|:---|
|name|string|代称|显示用的代称|false|
|world|string|世界|此配置应用于的世界|false|
|density|int|密度|npc生成的密度，检测范围是生成坐标+-50的正方体|true, 20|
|regions|\[\[\]\]|区域|区域列表，内部\[\]使用4或6个int值指示区域，使用4个int分别代表x1,z1,x2,z2，使用6个int值代表x1,y1,z1,x2,y2,z2，y1默认值为0，y2默认值为255|\[\]|
|excludes|\[\[\]\]|排除区域|同regions|\[\]|
|mobs|\{\}|怪物列表|\{"key1": weight\}，其中，key1填写mobs配置中的"key1", weight代表怪物权重|\{\}|

### blacks(暂时不可用)
blacks配置与groups配置大致相同，指示blacks配置使用的字段少于groups配置。

blacks使用的字段有: name, world, regions

使用方式可见groups.

尽管您现在可以进行blacks的配置，但是这些配置除去可以为groups配置中的excludes区域使用外，没有任何作用。设计上blacks包含的区域会清除任何进入的mob。

## 刷怪算法

* 选择一个玩家，在检查过权限与游戏模式（冒险或生存）后，生成一个随机的x, z坐标。
  * 拥有 npcspawner.noMobSpawn 权限的玩家不会被选为刷怪对象
  * 仅游戏模式为生存或冒险的玩家会被选为刷怪对象
  
x, z坐标的算法是:
```
r = random.nextDouble();
x = r * (minSpawnDistance - maxSpawnDistance) + minSpawnDistance;
x = random.nextBoolean() ? x : -x;
```

* 随后在该x, z坐标上选取合适的y坐标值:  
从玩家y坐标开始向下判断10个方块，后向上判断10个方块。当高度i处的方块为空气或水且i-1处的方块并非空气或水，则该高度i即为选取的y坐标值。若在一次选取中没有符合要求的高度i，则返回-1;

* 之后获取这个坐标所对应的生成区域。
* 从这个区域中获得对应的mobs权重表。
* 从mobs中选取一个mob:  
  每个mob对应一个权重，这个权重在生成成功后便会减少1点。如果在某一次选取mob时权重之和为0，则将mob权重重置。
* 生成目标NPC

## 指令
* /npcspawner reload， 重载config。
* /npcspawner info， 查看目前位置所在的刷怪区。
* /npcspawner debug \[true|false\] 查看或设置调试状态；调试状态下，游戏模式检查将被跳过，同时，您可以在后台看到在何处刷新了哪种怪物。
* /npcspawner pause \[true|false\] 查看或设置暂停刷怪