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
JAR_FILE=./redirect-1.0.3.jar

# 可用的最大直接内存
MAX_DIRECT_RAM=64M

java -jar -Xms"${MAX_RAM}" -Xmx"${MAX_RAM}" \
-XX:MaxRAMPercentage=45 -XX:InitialRAMPercentage=45 -XX:MinHeapFreeRatio=0 -XX:MaxHeapFreeRatio=100 \
-Xss512k -XX:MaxDirectMemorySize="${MAX_DIRECT_RAM}" -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=50 -XX:+UseZGC \
-XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000 \
-Dnetworkaddress.cache.ttl=10 -Djava.security.egd=file:/dev/./urandom \
-Dfile.encoding=utf8 -Dlog4j.skipJansi=true \
"${JAR_FILE}" start
