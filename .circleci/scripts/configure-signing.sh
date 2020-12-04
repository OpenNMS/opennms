#!/bin/bash

#### Java code signing configuration
#### Expects $JAVA_KEYSTORE, $JAVA_STOREPASS, $JAVA_KEYALIAS, and $JAVA_KEYPASS to be set

if [ -n "$JAVA_KEYSTORE" ]; then
  install -d -m 700 "$HOME/.m2"
  echo "JAVA_KEYSTORE is set, configuring ~/.m2/settings.xml"
  echo "* extracting keystore"
  printf '%s' "$JAVA_KEYSTORE" | base64 --decode > "$HOME/.m2/cert.keystore" 2>/dev/null

  echo "* extracting keystore password"
  MY_STOREPASS="$(printf '%s' "$JAVA_STOREPASS" | base64 --decode 2>/dev/null)"
  echo "* extracting key password"
  MY_KEYPASS="$(printf '%s' "$JAVA_KEYPASS" | base64 --decode 2>/dev/null)"

  cat <<END >"$HOME/.m2/settings.xml"
<settings>
 <mirrors>
  <mirror>
   <id>central-https</id>
   <mirrorOf>central</mirrorOf>
   <name>Maven Central Override</name>
   <url>https://repo1.maven.org/maven2/</url>
  </mirror>
 </mirrors>
 <profiles>
  <profile>
   <id>codesign</id>
   <properties>
    <webstart.keystore>${HOME}/.m2/cert.keystore</webstart.keystore>
    <webstart.keypass>${MY_KEYPASS}</webstart.keypass>
    <webstart.storepass>${MY_STOREPASS}</webstart.storepass>
    <webstart.keyalias>${JAVA_KEYALIAS}</webstart.keyalias>
    <webstart.keystore.delete>false</webstart.keystore.delete>
    <webstart.keygen>false</webstart.keygen>
    <webstart.dnameCn>\${user.name}</webstart.dnameCn>
    <webstart.dnameL></webstart.dnameL>
    <webstart.dnameSt></webstart.dnameSt>
    <webstart.dnameC></webstart.dnameC>
   </properties>
  </profile>
 </profiles>
</settings>
END
else
  echo "JAVA_KEYSTORE is NOT set, skipping jar-signing configuration"
fi
