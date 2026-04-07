# MyBateans: Smart MyBatis Tooling for NetBeans

[English](#english) | [日本語](#日本語) | [简体中文](#简体中文)

---

> [!IMPORTANT]
> **Preview Version** > This plugin is currently in a preview stage and under active development. Some features may change, and we welcome your feedback and bug reports.

## English

MyBateans is a professional NetBeans IDE plugin designed to make MyBatis development "Happy". It bridges the gap between Java interfaces and Mapper XML files with advanced static analysis.

### ✨ Key Features
* **Smart Navigation (Hyperlinks)**: Instantly jump between Java methods and XML SQL IDs using `Ctrl+Click`.
* **Auto Code Generation (Hint & Fix)**: Define a method in Java, and automatically generate the corresponding `<select>`, `<insert>`, `<update>`, or `<delete>` tag in XML with full parameter mapping.
* **JavaBean Property Analysis**: Automatically detects DTO fields to generate complex `INSERT` and `UPDATE` statements.
* **SQL Log Formatter**: Convert MyBatis debug logs (Preparing/Parameters) into executable, pretty-printed SQL statements.
* **Real-time Validation**: Highlights missing namespaces or methods directly in the XML editor.

### 🚀 Installation (Update Center)
You can install and update MyBateans by adding the following URL to your NetBeans Update Center (`Tools > Plugins > Settings > Add`):

| Channel | Update Center URL |
| :--- | :--- |
| **Main** (Stable) | `https://tatoo2018.github.io/MyBateans/updatesite/main/updates.xml` |
| **Develop** (Latest) | `https://tatoo2018.github.io/MyBateans/updatesite/develop/updates.xml` |

---

## 日本語

> [!IMPORTANT]
> **プレビュー版** > このプラグインは現在開発途中のプレビュー版です。機能の変更が行われる可能性があるため、フィードバックや不具合報告を歓迎します。

MyBateans は、NetBeans IDE での MyBatis 開発をより快適（Happy）にするためのプラグインです。高度な静的解析により、Java インターフェースと Mapper XML ファイルを強力に連携させます。



### ✨ 主な機能
* **スマート・ナビゲーション (相互ジャンプ)**: `Ctrl+クリック` で Java メソッドから対応する XML の SQL ID へ、またはその逆へ瞬時に移動できます。
* **コード自動生成 (Hint & Fix)**: Java 側でメソッドを定義するだけで、不足している XML タグ（`<select>`, `<insert>` 等）をパラメータ付きで自動生成します。
* **JavaBean プロパティ解析**: DTO のフィールドを解析し、`INSERT` 文のカラム名や `#{...}` を自動列挙します。
* **SQL ログ整形**: MyBatis のデバッグログ（Preparing/Parameters）を解析し、実行可能な SQL 文に整形して表示します。
* **リアルタイム検証**: 存在しない Namespace やメソッド名を XML エディタ上で即座に警告表示します。

### 🚀 インストール方法 (更新サイト)
NetBeans のプラグイン設定（`ツール > プラグイン > 設定 > 追加`）に以下の URL を登録することでインストール・更新が可能です。

| :--- | :--- |
| **Main** (安定版) | `https://tatoo2018.github.io/MyBateans/updatesite/main/updates.xml` |
| **Develop** (最新版) | `https://tatoo2018.github.io/MyBateans/updatesite/develop/updates.xml` |


### プロジェクトフォルダの説明
| チャンネル | 更新サイト URL | 説明 |
| :--- | :--- | :--- |
| **NetBeans Module 本体** | `com.jhappy.mybateans` | モジュール本体です | 
| **MyBatis サンプルプロジェクト** | `MyBatisSampleTemplateProject` | MyBaitisのサンプルプロジェクト|

MyBatis サンプルプロジェクトは
MyBateans内で
[New Project]>[Samples]>[MyBatis Sample Template Project]
として利用されます。

---

## 简体中文

> [!IMPORTANT]
> **预览版本** > 本插件目前处于预览阶段，正在积极开发中。部分功能可能会有变动，欢迎提供反馈和错误报告。

MyBateans 是一款专为 NetBeans IDE 打造的 MyBatis 增强插件。通过高级静态分析技术，它实现了 Java 接口与 Mapper XML 文件之间的无缝协作。

### ✨ 核心功能
* **智能导航 (超链接)**: 通过 `Ctrl+左键` 在 Java 方法与 XML 中的 SQL ID 之间实现双向快速跳转。
* **自动代码生成 (提示与修复)**: 仅需在 Java 中定义方法，即可在 XML 中自动生成对应的 `<select>`, `<insert>`, `<update>` 或 `<delete>` 标签及参数映射。
* **JavaBean 属性分析**: 自动识别 DTO 字段，快速生成复杂的 `INSERT` 和 `UPDATE` SQL 语句。
* **SQL 日志格式化**: 将 MyBatis 调试日志（Preparing/Parameters）解析为可执行且美观的 SQL 语句。
* **实时校验**: 在 XML 编辑器中自动检测并高亮不存在的命名空间（Namespace）或方法。

### 🚀 安装步骤 (更新站点)
您可以将以下 URL 添加到 NetBeans 的插件中心（`工具 > 插件 > 设置 > 添加`）来安装和更新：

| 频道 | 更新站点 URL |
| :--- | :--- |
| **Main** (稳定版) | `https://tatoo2018.github.io/MyBateans/updatesite/main/updates.xml` |
| **Develop** (最新版) | `https://tatoo2018.github.io/MyBateans/updatesite/develop/updates.xml` |
