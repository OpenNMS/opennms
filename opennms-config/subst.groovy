import java.io.File;

basedir = project.basedir;

def subst(srcFile, pattern, substition) {
    File workDir = new File("${basedir}/target/groovy_tmp");
    workDir.mkdirs();
    
    File tmpFile = File.createTempFile("subst", "tmp", workDir);
    if (!srcFile.renameTo(tmpFile)) {
        fail("Unable to rename "+srcFile+" to "+tmpFile+" Aborting!");
    }
    
    srcFile.withPrintWriter { out ->
        def lineNo = 0;
        tmpFile.eachLine { line ->
            lineNo++;
            def newline = line.replaceAll(pattern, substition);
            if (newline != line) {
                log.debug("${srcFile}");
                log.info("${srcFile.name}: Replaced line ${lineNo} >${line}< with >${newline}<");
            }
            out.println(newline);
        }
    }
    
    
}

def argFile = new File("${basedir}/target/generated-sources/castor/org/opennms/netmgt/config/notificationCommands/Argument.java")

subst(argFile, /@param\s+_switch\s*$/, "");
subst(argFile, /@param\s+switch/, "@param _switch");

def genDir = new File("${basedir}/target/generated-sources/castor");

genDir.eachDirRecurse { dir ->

    dir.eachFileMatch( ~/.*Descriptor\.java/) { file ->
        subst(file, /java\.lang\.Class getJavaClass/, /java\.lang\.Class<?> getJavaClass/)
    }
    
}
