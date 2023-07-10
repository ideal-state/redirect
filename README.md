# redirect

一个简单的数据包转发工具，支持 IPv4&amp;IPv6 , 目前仅支持 UDP 协议的端口


### 在哪下载 ?

> 前往 [releases](https://github.com/ideal-state/redirect/releases) 页


### 如何使用

* 新建一个虚拟环回网卡
* 为新建的网卡设置一个合法的名称
* 通过自带的启动脚本启动工具（首次启动会在生成默认配置后自动退出）
* 配置 `./redirect.properties` 中的参数（内含注释）
* 再次启动，启动后会刷新 dns 缓存及更新虚拟网卡 IP 地址（大概会等待 10s）


### 如何构建

```shell
git clone git@github.com:ideal-state/redirect.git
```
```shell
cd ./redirect
```

```shell
./gradlew.bat shadowJar
```

或

```shell
./gradlew shadowJar
```

> 等待构建完成，在 ./build/libs 下会生成包含 jar 包和各平台的启动脚本的 zip 存档

### 关于配置文件

> 请查看 [redirect.properties](./src/main/resources/redirect.properties)

### 怎样成为贡献者 ?

在贡献之前，你需要了解相应的规范。仔细阅读下列内容，对你所贡献的内容是否能够通过审核很有帮助！

> 🔔 首先，请先了解对应子组件所使用的开源许可证内容和 [Developer Certificate of Origin](https://developercertificate.org)
> 协议

#### 📏 一些规范

* 重要！！！贡献者须保证其所贡献的内容遵守了对应的开源许可证（以贡献内容所提交到的目标子组件所使用的开源许可证为准）中的条款
* 重要！！！每次提交贡献内容时须签署 [Developer Certificate of Origin](https://developercertificate.org)
  协议（idea：提交时勾选 `signed-off` 选项；cmd：提交时追加 `-s` 参数）
* 重要！！！为了保证本项目的独立性，本项目下的任何组件都应该避免引用来自第三方库的内容
* 统一缩进，即 4 个空格
* 任何可能会被开放给外部调用的类、方法等内容，都应该尽量为其添加文档注释说明（包括但不限于描述、参数、返回值、异常等必要说明）
* 贡献者可以在其添加或修改的内容上的注释说明中留下其名字，但不能随意地更改或删除已存在的其他贡献者的名字
* 只有 `dev` 分支会接受贡献请求
* 待补充……

#### 📌 步骤说明

1. `fork` 项目并 `clone` 项目到本地
2. 切换到 `dev` 分支，编辑你需要修改的部分
3. 提交并推送 `dev` 分支的改动到你 `fork` 后所创建的仓库
4. 点击 GitHub 页面顶部栏的 `pull request` ，认真填写与改动部分有关的说明信息后提交
5. 等待维护者审核通过后合并
