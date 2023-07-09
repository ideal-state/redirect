# redirect

一个简单的数据包转发工具，支持 IPv4&amp;IPv6 , 目前仅支持 UDP 协议的端口


### 在哪下载 ?

> 前往 [releases](https://github.com/ideal-state/hyper-framework/releases) 页


### 如何使用

* 新建一个虚拟环回网卡
* 为新建的网卡设置一个合法的名称
* 配置 `./start-redirect.bat` 中的启动参数（内含注释）
* 双击启动，开启时会刷新 dns 缓存及更新虚拟网卡 IP 地址（大概会等待 10s）


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
