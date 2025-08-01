#!/usr/bin/env bash

# JDK 9+ modules we need
JAVA_MODULES=(
        java.base \
        java.compiler \
        java.datatransfer \
        java.desktop \
        java.instrument \
        java.logging \
        java.management \
        java.management.rmi \
        java.naming \
        java.prefs \
        java.rmi \
        java.scripting \
        java.security.jgss \
        java.security.sasl \
        java.sql \
        java.sql.rowset \
        java.xml \
        jdk.attach \
        jdk.httpserver \
        jdk.jdi \
        jdk.sctp \
        jdk.security.auth \
        jdk.xml.dom \
)

# JDK 9+ modules that need to read from public methods
JAVA_READS=(
    "java.xml=java.logging"
)

# JDK 9+ modules that need public methods available to reflection
JAVA_EXPORTS=(
    "java.base/sun.net.www.protocol.file=ALL-UNNAMED" \
    "java.base/sun.net.www.protocol.ftp=ALL-UNNAMED" \
    "java.base/sun.net.www.protocol.http=ALL-UNNAMED" \
    "java.base/sun.net.www.protocol.https=ALL-UNNAMED" \
    "java.base/sun.net.www.protocol.jar=ALL-UNNAMED" \
    "java.base/sun.net.www.content.text=ALL-UNNAMED" \
    "java.base/sun.security.ssl=ALL-UNNAMED" \
    "java.base/sun.security.x509=ALL-UNNAMED" \
    "jdk.xml.dom/org.w3c.dom.html=ALL-UNNAMED" \
    "jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED" \
    "java.rmi/sun.rmi.registry=ALL-UNNAMED" \
    "java.security.sasl/com.sun.security.sasl=ALL-UNNAMED"
)

# JDK 9+ modules that need public+private methods available to reflection
JAVA_OPENS=(
    "java.base/jdk.internal.loader=ALL-UNNAMED" \
    "java.base/java.io=ALL-UNNAMED" \
    "java.base/java.lang=ALL-UNNAMED" \
    "java.base/java.lang.invoke=ALL-UNNAMED" \
    "java.base/java.lang.reflect=ALL-UNNAMED" \
    "java.base/java.math=ALL-UNNAMED" \
    "java.base/java.net=ALL-UNNAMED" \
    "java.base/java.security=ALL-UNNAMED" \
    "java.base/java.text=ALL-UNNAMED" \
    "java.base/java.util=ALL-UNNAMED" \
    "java.base/java.util.concurrent=ALL-UNNAMED" \
    "java.base/java.util.regex=ALL-UNNAMED" \
    "java.desktop/java.awt.font=ALL-UNNAMED" \
    "java.desktop/java.beans=ALL-UNNAMED" \
    "java.naming/javax.naming.spi=ALL-UNNAMED" \
    "java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED" \
    "java.sql/java.sql=ALL-UNNAMED"
)

JAVA_PATCHES=(
    "java.base=${KARAF_HOME}/lib/endorsed/org.apache.karaf.specs.locator-${karafVersion}.jar" \
    "java.xml=${KARAF_HOME}/lib/endorsed/org.apache.karaf.specs.java.xml-${karafVersion}.jar"
)

# Java 9+, add required modules
OIFS="$IFS"
IFS=","
printf -- '--add-modules=%s ' "${JAVA_MODULES[*]}"
IFS="$OIFS"

for READ in "${JAVA_READS[@]}"; do
    printf -- '--add-reads %s ' "${READ}"
done
for EXPORT in "${JAVA_EXPORTS[@]}"; do
    printf -- '--add-exports %s ' "${EXPORT}"
done
for OPEN in "${JAVA_OPENS[@]}"; do
    printf -- '--add-opens %s ' "${OPEN}"
done
# this was in newer Karaf init, but I'm not convinced it works for us
#for PATCH in "${JAVA_PATCHES[@]}"; do
#    printf -- '--patch-module %s ' "${PATCH}"
#done
