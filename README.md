# Caged Mobs Reborn — Minecraft 1.20.1 Forge

本工作树从 1.21.1 NeoForge 分支迁移，使用 Forge 47.4.23、Java 17 和 Mojang 官方映射。模组 ID 为 `cagedmobsreborn`。

包含三档 DNA 采样器、普通及漏斗生物笼、环境、升级、数据包掉落与自动实体掉落，以及 JEI 和 Jade 集成。

## 开发

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat compileJava processResources
.\gradlew.bat runClient
```

开发辅助依赖 Just Enough Characters 仅适用于客户端；运行专用服务端测试时需从测试运行依赖中排除它。该依赖不会打入本模组产物。

## 数据包

配方使用 `data/<命名空间>/recipes/`，标签使用 `tags/items/`、`tags/blocks/`。通用物品标签使用 `forge:` 命名空间。条件配方顶层使用 `conditions`，条件类型使用 `forge:mod_loaded` 等 Forge 类型。

## 原模组与许可证

原作者为 Corgam，许可证为 GNU Lesser General Public License v2.1（LGPL-2.1）；完整条款见根目录 `LICENSE`。

- [原模组页面](https://www.curseforge.com/minecraft/mc-mods/caged-mobs)
- [原项目仓库](https://github.com/Corgam/CagedMobsReborn)
