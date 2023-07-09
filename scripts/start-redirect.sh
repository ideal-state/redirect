#!/bin/bash

# 数据包重定向工具
# MIT License
# Copyright (c) 2023 ideal-state
# 
# 基于 JDK 17 开发
# 支持 IPv4&IPv6 , 目前仅支持 UDP 协议的端口
# 仅在方舟服务器上进行过简单测试

# 最大内存占用
MAX_RAM=256M
# JAR 包文件位置
JAR_FILE=./redirect-1.0.1.jar
# 虚拟网卡设备名称, 不允许空格!!!
DEVICE_NAME=redirect
# 要转发的 IP 的地址协议
SRC_PROTO=v4
# 真实目的地的 IP 的地址协议
DIST_PROTO=v6
# 要转发的主机名, 域名则选择 SRC_PROTO 所配置的协议进行解析, IP 地址则必须与所配置的协议相符
SRC_HOST=xxxrull.dynv6.net
# 真实目的地的主机名, 域名则选择 DIST_PROTO 所配置的协议进行解析, IP 地址则必须与所配置的协议相符
DIST_HOST=xxxrull.dynv6.net
# 日志输出数据包内容时所用的格式, true 则保持字节数组样式, false 则转为字符串样式
LOG_RAW=false
# 要转发的端口范围, 不允许空格!!! 逗号用于分割, 减号用于指定范围端口
PORTS=27015-27016,7777-7780

# 可用的最大直接内存
MAX_DIRECT_RAM=64M

java -jar -Xms"${MAX_RAM}" -Xmx"${MAX_RAM}" \
-XX:MaxRAMPercentage=45 -XX:InitialRAMPercentage=45 -XX:MinHeapFreeRatio=0 -XX:MaxHeapFreeRatio=100 \
-Xss512k -XX:MaxDirectMemorySize="${MAX_DIRECT_RAM}" -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=50 -XX:+UseZGC \
-XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000 \
-Dnetworkaddress.cache.ttl=10 -Djava.security.egd=file:/dev/./urandom \
-Dlog4j.skipJansi=true \
"${JAR_FILE}" \
deviceName="${DEVICE_NAME}" \
srcProto="${SRC_PROTO}" distProto="${DIST_PROTO}" \
srcHost="${SRC_HOST}" distHost="${DIST_HOST}" \
logRaw="${LOG_RAW}" \
ports="${PORTS}"
