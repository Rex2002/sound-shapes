# Script adapted from [ArtInLines](https://github.com/ArtInLines)' version for [SoundInvestments](https://github.com/Rex2002/sound-investments)
#
javaPath=/opt/homebrew/Cellar/openjdk/21.0.2
javafxPath=/Users/malterichert/Dev/javafx-sdk-21
jarName=SoundShapes.jar
mavenJarPath=./target/sound-shapes-1.0-SNAPSHOT.jar
#
# Build clean dist directory
if [ -d "./dist" ]; then
	rm -rf "./dist"
fi
mkdir "./dist"

# Build Fat-Jar
echo "Building JAR..."
mvn clean package >/dev/null
cp "$mavenJarPath" "./dist/$jarName" >/dev/null

# Build Java Runtime
echo "Building Runtime..."
cp -r "$javafxPath" "./dist/javafx" >/dev/null
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path "$javaPath/jmods" --add-modules java.base,jdk.localedata,java.desktop --module-path "./dist/javafx/lib" --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics --bind-services --output dist/java
cp -r "./dist/javafx/lib/" "./dist/java/bin" >/dev/null
rm -rf "./dist/javafx/lib"     >/dev/null
rm -rf "./dist/javafx/legal"   >/dev/null
rm     "./dist/javafx/src.zip" >/dev/null

# Copy resources into the distributable
echo "Copying Resources..."
distResourcesPath=./dist/src/main
mkdir -p "$distResourcesPath"
cp -r "./src/main/resources" "$distResourcesPath" >/dev/null

# Write executable into the distributable
echo "Writing Script..."
echo "#! /usr/bin/env bash
cd -- \$(dirname \$BASH_SOURCE)
java --module-path javafx/lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics -jar $jarName" >dist/SoundInvestments.command
chmod a+x dist/SoundInvestments.command

echo "Done"