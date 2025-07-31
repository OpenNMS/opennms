#!/bin/bash

# Configuration
ORG="opennms"
MAVEN_REPO="classic"

# Get version from POM file
ROOT_VERSION=$(~/project/.circleci/scripts/pom2version.sh ~/project/pom.xml || echo "0.0.0")

echo "🔍 Detected version from pom.xml: '$ROOT_VERSION'"

cd ~/project/deploy/target/central-publishing || exit 1

cp central-bundle.zip "maven-bundle-$ROOT_VERSION.zip"

echo "📦 Uploading: maven-bundle-$ROOT_VERSION.zip"
cloudsmith push raw --version "$ROOT_VERSION" --name "Maven bundle $ROOT_VERSION"  --no-wait-for-sync --republish --description "Zip file for $ROOT_VERSION" "$ORG/$MAVEN_REPO" "maven-bundle-$ROOT_VERSION.zip"

echo "🧩 Extracting central-bundle.zip"
unzip central-bundle.zip

# Find and process each .jar file 
find org -type f -name "*.jar" ! -name "*-sources.jar" ! -name "*-tests.jar" ! -name "*-xsds.jar" | while read -r JAR_FILE; do
  DIR=$(dirname "$JAR_FILE")
  BASENAME=$(basename "$JAR_FILE" .jar)
  POM_FILE="$DIR/$BASENAME.pom"
  JAVADOC_FILE="$DIR/$BASENAME-javadoc.jar"

  # Extract Maven coordinates from path
  RELATIVE_PATH="${DIR#org/}"
  IFS='/' read -r -a PARTS <<< "$RELATIVE_PATH"
  VERSION="${PARTS[-1]}"
  ARTIFACT_ID="${PARTS[-2]}"
  GROUP_ID=$(IFS=.; echo "${PARTS[@]:0:${#PARTS[@]}-2}")

  # Override version with root version
  # In case we get a "bad version" we should use the version in root pom file
  if [[ "$VERSION" != "$ROOT_VERSION" ]]; then
    echo "🔁 Overriding version '$VERSION' with canonical version '$ROOT_VERSION'"
    VERSION="$ROOT_VERSION"
  fi

  echo "📦 Uploading: $GROUP_ID:$ARTIFACT_ID:$VERSION"

  if [[ ! -f "$POM_FILE" ]]; then
    echo "⚠️  Skipping $JAR_FILE — missing $POM_FILE"
    continue
  fi

  # Build upload command
  CMD=(cloudsmith push maven --no-wait-for-sync --republish --pom-file "$POM_FILE" )

  # Add javadoc if available
  [[ -f "$JAVADOC_FILE" ]] && CMD+=(--javadoc-file "$JAVADOC_FILE")

  CMD+=("$ORG/$MAVEN_REPO"  "$JAR_FILE")

  "${CMD[@]}"
  echo "✅ Uploaded $ARTIFACT_ID:$VERSION"
done

# Upload -xsds.jar files
find org -type f -name "*-xsds.jar" | while read -r XSDS_FILE; do
  DIR=$(dirname "$XSDS_FILE")
  BASENAME=$(basename "$XSDS_FILE" .jar)
  POM_FILE="$DIR/$BASENAME.pom"

  RELATIVE_PATH="${DIR#org/}"
  IFS='/' read -r -a PARTS <<< "$RELATIVE_PATH"
  VERSION="${PARTS[-1]}"
  ARTIFACT_ID="${PARTS[-2]}"
  GROUP_ID=$(IFS=.; echo "${PARTS[@]:0:${#PARTS[@]}-2}")

  if [[ "$VERSION" != "$ROOT_VERSION" ]]; then
    echo "🔁 Overriding version '$VERSION' with canonical version '$ROOT_VERSION'"
    VERSION="$ROOT_VERSION"
  fi

  echo "📦 Uploading xsds artifact: $GROUP_ID:$ARTIFACT_ID:$VERSION"

  if [[ ! -f "$POM_FILE" ]]; then
    echo "✅🤷 Uploading $XSDS_FILE even though $POM_FILE missing"
    cloudsmith push maven --no-wait-for-sync --republish --version "$VERSION"  "$ORG/$MAVEN_REPO" "$XSDS_FILE" 
    continue
  fi

  cloudsmith push maven --no-wait-for-sync --republish --version "$VERSION" --pom-file "$POM_FILE" "$ORG/$MAVEN_REPO" "$XSDS_FILE" 
  echo "✅ Uploaded $ARTIFACT_ID:$VERSION (xsds)"
done

# Upload any other non-Maven files
echo "🔍 Scanning for non-Maven files..."
find org -type f ! -name "*.jar" ! -name "*.pom" ! -name "*.sha1" ! -name "*.sha256" ! -name "*.sha512" ! -name "*.md5" ! -name "*.asc"  | while read -r FILE; do
  echo "📁 Uploading raw file: $FILE"
  cloudsmith push raw --no-wait-for-sync --republish --version "$ROOT_VERSION" "$ORG/$MAVEN_REPO" "$FILE"
done
