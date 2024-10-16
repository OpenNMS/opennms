#!/usr/bin/env JAVA_HOME= groovy

import java.nio.file.*
import java.util.stream.*

jarCacheRoot = Path.of(System.properties["user.home"], ".manifest-cache")
if (!jarCacheRoot.toFile().exists()) {
  Files.createDirectories(jarCacheRoot)
}

printExports = false;

for (arg : args) {
  jarPath = Path.of(arg).toAbsolutePath()
  jarFileName = jarPath.getFileName().toString()
  jarCachePath = Paths.get(jarCacheRoot.toString(), jarPath.getParent().toString(), jarFileName + ".txt")
  if (!jarFileName.contains("-SNAPSHOT") && jarCachePath.toFile().exists()) {
    println("[$arg] (cached)")
    Files.readAllLines(jarCachePath).forEach(line -> println(line));
  } else {
    println("[$arg]")
    jarURL = new java.net.URL("jar:file:" + jarPath + "!/")
    text = "no manifest found"

    m = jarURL.openConnection().getManifest()
    if (m != null) {
      text = m.getMainAttributes()
        .entrySet()
        .collect { e -> {
            println("$e.key = $e.value");
            return "$e.key = $e.value";
          }
        }.join("\n")
    }

    Files.createDirectories(jarCachePath.getParent())
    Files.writeString(jarCachePath, text)
  }
}
