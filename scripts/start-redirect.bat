@echo off
chcp 65001

rem 数据包重定向工具
rem MIT License
rem Copyright (c) 2023 ideal-state
rem
rem 基于 JDK 17 开发
rem 支持 IPv4&IPv6 , 目前仅支持 UDP 协议的端口
rem 仅在方舟服务器上进行过简单测试

rem 最大内存占用
set MAX_RAM=256M
rem JAR 包文件位置
set JAR_FILE=./redirect-1.0.4.jar

rem 可用的最大直接内存
set MAX_DIRECT_RAM=64M

java -jar -Xms%MAX_RAM% -Xmx%MAX_RAM%  ^
-XX:MaxRAMPercentage=45 -XX:InitialRAMPercentage=45 -XX:MinHeapFreeRatio=0 -XX:MaxHeapFreeRatio=100 ^
-Xss512k -XX:MaxDirectMemorySize=%MAX_DIRECT_RAM% -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=50 -XX:+UseZGC ^
-XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000 ^
-Dnetworkaddress.cache.ttl=10 -Djava.security.egd=file:/dev/./urandom ^
-Dfile.encoding=utf8 -Dlog4j.skipJansi=false ^
%JAR_FILE% start

pause