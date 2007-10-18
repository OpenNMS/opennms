import java.io.File;

basedir = project.basedir;

def subst(srcFile, pattern, substition) {
    File workDir = new File("${basedir}" + File.separator + "target" + File.separator + "groovy_tmp");
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

def argFile = new File("${basedir}" + File.separator + "target" + File.separator + "generated-sources" + File.separator + "castor" + File.separator + "org" + File.separator + "opennms" + File.separator + "netmgt" + File.separator + "config" + File.separator + "notificationCommands" + File.separator + "Argument.java");

subst(argFile, /@param\s+_switch\s*$/, "");
subst(argFile, /@param\s+switch/, "@param _switch");

def genDir = new File("${basedir}" + File.separator + "target" + File.separator + "generated-sources" + File.separator + "castor");

genDir.eachDirRecurse { dir ->

    dir.eachFileMatch( ~/.*Descriptor\.java/) { file ->
        subst(file, /java\.lang\.Class getJavaClass/, /java\.lang\.Class<?> getJavaClass/)
    }
    
}
