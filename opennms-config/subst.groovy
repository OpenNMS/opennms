import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import com.twmacinta.util.MD5;

basedir = project.basedir;

def subst(srcFile, pattern, substition) {
    File workDir = new File("${basedir}" + File.separator + "target" + File.separator + "groovy_tmp");
    workDir.mkdirs();
    
    File tmpFile = File.createTempFile("subst", "tmp", workDir);
    FileUtils.copyFile(srcFile, tmpFile);

    tmpFile.withPrintWriter { out ->
        def lineNo = 0;
        srcFile.eachLine { line ->
            lineNo++;
            def newline = line.replaceAll(pattern, substition);
            if (newline != line) {
                log.debug("${tmpFile}");
                // log.info("${tmpFile.name}: Replaced line ${lineNo} >${line}< with >${newline}<");
            }
            out.println(newline);
        }
    }

	before = MD5.asHex(MD5.getHash(srcFile));
	after = MD5.asHex(MD5.getHash(tmpFile));
	
	if (before != after) {
		FileUtils.copyFile(tmpFile, srcFile);
	} else {
		log.debug("skipping ${srcFile}: file is unchanged");
	}
	
	tmpFile.delete();    
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
