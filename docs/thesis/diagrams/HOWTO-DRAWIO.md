# 如何把图放进论文（draw.io / Word）

## 方法一：diagrams.net（draw.io）导入 Mermaid（推荐）

1. 浏览器打开：<https://app.diagrams.net/>  
2. 菜单 **调整图形 → 插入 → 高级 → Mermaid…**（不同语言界面可能为 **Arrange → Insert → Advanced → Mermaid**）。  
3. 打开本目录下对应 `.md` 文件，复制其中 **```mermaid** 与 **```** 之间的整段文字，粘贴到 Mermaid 输入框。  
4. 生成后微调方框位置、统一字体，**文件 → 导出为 → PNG**（论文常用 300 DPI 若可选）。  
5. 插入 Word：**插入 → 图片**，图题写「图 x-x  xxx 架构图」。

> 若你的 draw.io 版本没有 Mermaid 项：可用 <https://mermaid.live> 粘贴同一段代码 → **Actions → PNG/SVG** 导出，再插入 Word。

## 方法二：Visio

将 Mermaid 图在 draw.io 或 mermaid.live 中先导出为 **SVG**，用 Visio **插入 → 图片** 或 Inkscape 转存后编辑。

## 方法三：直接截 VS Code / Cursor 预览

在编辑器中打开 `.md`，若已安装 **Markdown Preview Mermaid Support** 插件，预览后截图（清晰度略低，适合草稿）。

## 论文作图规范小提示

- 每张图正文里**第一次出现**要写：「如图 x-x 所示，……」  
- 图下注明：**图源：自绘** 或 **图源：本系统架构**  
- 部署拓扑中的 IP、域名用**占位符**（如 `api.example.com`），避免泄露真实服务器。
