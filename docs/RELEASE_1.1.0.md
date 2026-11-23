# Caged Mobs Reborn 1.1.0

发布日期：2026-10-25。

## 更新说明

中文：修复普通笼子和漏斗笼子被破坏或替换时丢失环境及升级物品的问题。库存返还改由方块实体的移除前回调执行，含水等状态变化不会触发返还。

English: Fixed environments and upgrades being lost when a Mob Cage or Hopping Mob Cage is broken or replaced. Inventory drops now run before the block entity is removed; changes such as waterlogging keep the inventory intact.

## 验证

- Java 编译通过。
- 真实独立服务端专项回归：10 组、90 次断言全部通过。
- 覆盖普通笼子和漏斗笼子的环境及三个升级精确返还、空笼、重复移除、含水及跳跃属性变化、抑制移除副作用和替换为石头。
- 本次仅修复移除时的库存返还；满漏斗吞产出及损坏 DNA 数据读取异常仍为已知问题。

## 发布目标

- CurseForge 项目：Caged Mobs Reborn，项目 ID `1757115`，slug `caged-mobs-reborn`。
- Minecraft：26.1.2；加载器：NeoForge；环境：客户端和服务端。
- 模组内部版本：`26.1.2-1.1.0`。
- 发布显示名称：`26.1.2-neoforge-CagedMobsReborn-1.1.0`。
- 正式产物：`build/libs/cagedmobsreborn-26.1.2-neoforge-26.1.2-1.1.0.jar`。

## 发布结果

- `gradlew.bat build --offline --console=plain` 成功。
- 官方 Upload API 返回 HTTP 200，文件 ID `9318267`；作者后台已确认状态为 `Under Review`，尚未审核通过。
- 文件链接：https://www.curseforge.com/minecraft/mc-mods/caged-mobs-reborn/files/9318267
- SHA-256：`9f17588affdc7b948776454c74e4ea80741c07fa57def12b99dba6a34634ce45`。
- 上传回执、元数据、构建日志及审核状态截图位于 `D:/AI临时文件/CagedMobsReborn-2612-fix-release-20261025`。
