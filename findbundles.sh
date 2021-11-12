#!/usr/bin/env bash
searchword=$1
xpathstr=//dependency[contains\(artifactId,\'${searchword}\'\)]
for matchingPom in `find . -name 'pom.xml' -exec grep -l "<artifactId>.*${searchword}.*</artifactId>" {} \;` 
do
    cat $matchingPom | sed 's#<project xmlns.*>#<project>#' | xmllint  --xpath "$xpathstr" - | tr '\n' ' ' | sed 's/<dependency>/\n<dependency>/g' | sed 's/>[ \t]*</></g' | sed 's#<[/]*dependency>##g' | sed 's#<groupId>#bundle:install mvn:#g' | sed 's#</groupId><artifactId>#/#g' | sed 's#</artifactId><version>#/#g' | sed 's#</version>##g'
done
echo ''
exit
