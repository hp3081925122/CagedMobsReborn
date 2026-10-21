# Caged Mobs Reborn 1.21.1 → 26.1.2 迁移记录

## 分支与工作树

- 源分支：`1211-neoforge`
- 目标分支：`2612-neoforge`
- 目标工作树：`2612-neoforge-CagedMobsReborn`
- Minecraft：26.1.2
- NeoForge：26.1.2.99
- Java：25
- ModDev Gradle 插件：2.0.147

## 已完成迁移

- 使用 26.1.2 NeoForge Gradle Wrapper（Gradle 9.4.0）。
- 将数据目录迁移到 26.1.2 的单数目录：`recipe`、`advancement`、`loot_table`、`structure`、`function`、`predicate`、`item_modifier` 及单数标签目录。
- `pack.mcmeta` 改为 `min_format`/`max_format`，数据包格式 101.1。
- 将 `ResourceLocation` 迁移为 26.1.2 的 `Identifier`。
- 迁移配方序列化器、`Recipe` 接口、`Ingredient` 新 Holder API 和方块实体 ValueInput/ValueOutput 存档 API。
- 迁移物品使用结果、方块交互、方块更新、粒子、Jade、JEI、GUI 提取器及实体/方块实体渲染状态 API。
- 保留 DNA、环境、掉落、烹饪、升级、成长、方块实体存档和网络同步逻辑。

## 验证

- `./gradlew clean build --no-daemon`：通过。
- `compileJava`：通过；当前仅有 NeoForge 26.1.2 标记的弃用警告。
- 目标 JAR：`build/libs/cagedmobsreborn-26.1.2-neoforge-26.1.2-1.0.0.jar`（构建后文件名随 `mod_version` 变化）。
- `./gradlew runServer --no-daemon`：服务端到达 `Done`，加载 1643 个配方和 1644 个进度。
- `./gradlew runClient --no-daemon`：客户端启动并完成资源重载，无模型属性注册错误。

## 继续验证

- 需要使用 26.1.2 的 Render Preview 实例做客户端实体/方块实体渲染截图。
- 仍需使用 26.1.2 Render Preview 检查实体/方块实体渲染截图，以及实际世界内的方块实体存档、掉落和升级逻辑。
- DNA 采样器的 `full` 模型选择已迁移到 `ConditionalItemModelProperty` 和 `assets/<namespace>/items/*.json` 条件模型。
- CraftTweaker 26.1.2 专用 Maven 坐标尚未找到，当前源码保留兼容 API 依赖，需在安装 CraftTweaker 26.1.2 环境中做一次可选集成验证。
