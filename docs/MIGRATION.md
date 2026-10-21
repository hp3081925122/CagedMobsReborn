# Caged Mobs Reborn 迁移文档

## 来源与许可证

- 来源项目：Corgam/CagedMobsReborn，作者 Corgam。
- 来源页面：https://www.curseforge.com/minecraft/mc-mods/caged-mobs
- 源码仓库：https://github.com/Corgam/CagedMobsReborn
- 许可证：GNU LGPL v2.1；本项目保留来源与许可证说明，并在元数据中注明重生版来源。

## 目标

将 Caged Mobs 1.20.2 Forge/NeoForge 2.0.7 的功能迁移到 Minecraft 1.21.1 NeoForge，目标目录为 `CagedMobsReborn/1211-neoforge-CagedMobsReborn`。

## 已确认的原功能

- 普通 Mob Cage 与 Hopping Mob Cage。
- DNA Sampler、Diamond DNA Sampler、Netherite DNA Sampler 与实体捕获。
- 环境配方、实体/首领配方、额外掉落配方和数据包扩展。
- Cooking、Lightning、Arrow、Experience、Looting、Speed I/II/III、Creative 等升级。
- 笼子方块实体、容器界面、实体渲染、掉落收集、漏斗输出。
- 客户端粒子配置、服务端黑名单与掉落/跳跃笼配置。
- JEI、Jade、CraftTweaker 的可选集成。

## 迁移策略

1. 以模板 `1211-neoforge` 建立 NeoForge 21.1.233 工程。
2. 原 Java 源码、资源、数据配方和语言文件完整导入，不删除旧功能分支。
3. 将 Forge 注册、事件、配置、能力、菜单、客户端注册和可选集成改为 NeoForge 1.21.1 API。
4. 将资源命名空间统一为 `cagedmobsreborn`，避免与旧版模组 ID 冲突；保留显示名称和原数据结构。
5. 使用最小依赖编译验证，再运行服务端和客户端烟测。

## 验收标准

- `gradlew build` 成功，且无 Java 编译错误。
- 独立服务端可加载模组并生成默认配置。
- 客户端可打开 Mob Cage 菜单，注册方块、物品、配方和渲染层。
- 原始数据配方、语言键和纹理资源完整存在。
- 不以简化实现替代原笼子处理、采样、升级、掉落或配置逻辑。

## 验证记录

- `./gradlew.bat --offline --no-daemon clean build`：成功。
- `./gradlew.bat --offline --no-daemon runServer`：服务端完成启动并输出 `Done`，随后手动停止。
- JEI 作为编译期可选依赖保留，避免在 NeoForge 21.1.233 服务端开发环境中强制加载不兼容的 JEI 运行文件；Jade 与 Just Enough Characters 已完成服务端加载烟测。
- 使用 `E:/模组/发cf的模组/Render Preview/1.21.1-neoforge-render-preview` 加载本项目 JAR 完成客户端离屏渲染验证。
- `cagedmobsreborn` 目录的 26 个物品条目全部成功绘制；前景像素均大于 0，洋红缺失贴图比例为 0，证据保存在 `D:/AI临时文件/RenderPreview/20261021-083800-13400`。
