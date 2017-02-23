set OPENNMS_HOME=.
set OPENNMS_REMOTING_URI=http://192.168.54.187:8980/opennms-remoting
set OPENNMS_REMOTING_USER=admin
set OPENNMS_REMOTING_PASS=admin

OpenNMSRemotePoller install ^
  --DisplayName="OpenNMS Remote Poller ${project.version}" ^
  --Description="OpenNMS remote poller client for monitoring availability of services from multiple locations." ^
  --Install="%OPENNMS_HOME%\OpenNMSRemotePoller.exe" ^
  --Startup=auto ^
  --LogPath="%OPENNMS_HOME%\logs" ^
  --LogLevel=Debug ^
  --StdOutput=auto ^
  --StdError=auto ^
  --JvmOptions="-Dopennms.home=%OPENNMS_HOME%" ^
  ++JvmOptions="-Djava.rmi.activation.port=1099" ^
  ++JvmOptions=-Xmx384m ^
  ++JvmOptions=-XX:MaxMetaspaceSize=256M ^
  ++JvmOptions="-Xdebug" ^
  ++JvmOptions="-Xnoagent" ^
  ++JvmOptions="-Djava.compiler=none" ^
  ++JvmOptions="-Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n" ^
  --Classpath="%OPENNMS_HOME%\remote-poller-${project.version}.jar" ^
  --StartMode=jvm ^
  --StartClass=com.simontuffs.onejar.Boot ^
  --StartParams=-n ^
  ++StartParams=%OPENNMS_REMOTING_USER% ^
  ++StartParams=-p ^
  ++StartParams=%OPENNMS_REMOTING_PASS% ^
  ++StartParams=-l ^
  ++StartParams=00002 ^
  ++StartParams=-u ^
  ++StartParams=%OPENNMS_REMOTING_URI% ^
  --StopMode jvm ^
  --StopClass main.Exit ^
  --StopMethod stop

:: Add these options to enable remote debugging
::  ++JvmOptions="-Xdebug" ^
::  ++JvmOptions="-Xnoagent" ^
::  ++JvmOptions="-Djava.compiler=none" ^
::  ++JvmOptions="-Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n" ^

