@echo off
chcp 65001

rem ���ݰ��ض��򹤾�
rem MIT License
rem Copyright (c) 2023 ideal-state
rem
rem ���� JDK 17 ����
rem ֧�� IPv4&IPv6 , Ŀǰ��֧�� UDP Э��Ķ˿�
rem ���ڷ��۷������Ͻ��й��򵥲���

rem ����ڴ�ռ��
set MAX_RAM=256M
rem JAR ���ļ�λ��
set JAR_FILE=./redirect-1.0.4.jar

rem ���õ����ֱ���ڴ�
set MAX_DIRECT_RAM=64M

java -jar -Xms%MAX_RAM% -Xmx%MAX_RAM%  ^
-XX:MaxRAMPercentage=45 -XX:InitialRAMPercentage=45 -XX:MinHeapFreeRatio=0 -XX:MaxHeapFreeRatio=100 ^
-Xss512k -XX:MaxDirectMemorySize=%MAX_DIRECT_RAM% -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=50 -XX:+UseZGC ^
-XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000 ^
-Dnetworkaddress.cache.ttl=10 -Djava.security.egd=file:/dev/./urandom ^
-Dfile.encoding=utf8 -Dlog4j.skipJansi=false ^
%JAR_FILE% start

pause