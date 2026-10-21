# 1.21.1 NeoForge 模板

这是一个无 Mixin、无自定义内容的最小 NeoForge 工作区，使用 NeoForge 21.1.233 与 Java 21。

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-21.0.11'
.\gradlew.bat build
.\gradlew.bat runClient
```

复制项目和批量改名请在上一级目录运行 `new-mod-from-template.ps1`。

## 原模组与许可证

本项目是 Corgam 制作的 Caged Mobs 的 NeoForge 1.21.1 重生与移植版本，保留并迁移原模组的生物捕捉、DNA 样本、笼子自动化、升级和掉落复制功能。

- 原模组页面：https://www.curseforge.com/minecraft/mc-mods/caged-mobs
- 原项目仓库：https://github.com/Corgam/CagedMobsReborn
- 许可证：GNU Lesser General Public License v2.1（LGPL-2.1）
- 本项目的完整许可证文本位于仓库根目录 `LICENSE`，并随发布 JAR 一并提供。

This project is a NeoForge 1.21.1 reborn and port of Corgam's Caged Mobs. It preserves and ports the original mob capture, DNA sample, cage automation, upgrade, and drop replication features.

- Original mod page: https://www.curseforge.com/minecraft/mc-mods/caged-mobs
- Original project repository: https://github.com/Corgam/CagedMobsReborn
- License: GNU Lesser General Public License v2.1 (LGPL-2.1)
- The complete license text is included in the repository as `LICENSE` and packaged in the release JAR.
