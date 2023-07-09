@echo off
chcp 65001

rem ���ݰ��ض��򹤾�
rem MIT License
rem Copyright (c) 2023 ideal-state
rem 
rem ���� JDK 17 ����
rem ֧�� IPv4&IPv6 , Ŀǰ��֧�� UDP Э��Ķ˿�
rem ���ڷ��۷������Ͻ��й��򵥲���

rem ����ڴ�ռ��, 128M ��������Ӧ��
set MAX_RAM=256M
rem JAR ���ļ�λ��
set JAR_FILE=./redirect-1.0.0.jar
rem ���������豸����, ������ո�!!!
set DEVICE_NAME=redirect
rem Ҫת���� IP �ĵ�ַЭ��
set SRC_PROTO=v4
rem ��ʵĿ�ĵص� IP �ĵ�ַЭ��
set DIST_PROTO=v6
rem Ҫת����������, ������ѡ�� SRC_PROTO �����õ�Э����н���, IP ��ַ������������õ�Э�����
set SRC_HOST=xxxrull.dynv6.net
rem ��ʵĿ�ĵص�������, ������ѡ�� DIST_PROTO �����õ�Э����н���, IP ��ַ������������õ�Э�����
set DIST_HOST=xxxrull.dynv6.net
rem ��־������ݰ�����ʱ���õĸ�ʽ, true �򱣳��ֽ�������ʽ, false ��תΪ�ַ�����ʽ
set LOG_RAW=false
rem Ҫת���Ķ˿ڷ�Χ, ������ո�!!! �������ڷָ�, ��������ָ����Χ�˿�
set PORTS=27015-27016,7777-7780

java -jar -Xms%MAX_RAM% -Xmx%MAX_RAM% ^
-XX:MaxRAMPercentage=45 -XX:InitialRAMPercentage=45 -XX:MinHeapFreeRatio=0 -XX:MaxHeapFreeRatio=100 ^
-Xss512k -XX:MaxDirectMemorySize=64M -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=50 -XX:+UseZGC ^
-XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000 ^
-Dnetworkaddress.cache.ttl=10 -Djava.security.egd=file:/dev/./urandom ^
-Dfile.encoding=utf-8 -Dlog4j.skipJansi=false ^
%JAR_FILE% ^
deviceName=%DEVICE_NAME% ^
srcProto=%SRC_PROTO% distProto=%DIST_PROTO% ^
srcHost=%SRC_HOST% distHost=%DIST_HOST% ^
logRaw=%LOG_RAW% ^
ports=%PORTS%

pause