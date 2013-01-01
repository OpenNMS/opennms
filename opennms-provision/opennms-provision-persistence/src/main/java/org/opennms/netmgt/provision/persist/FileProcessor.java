package org.opennms.netmgt.provision.persist;

import java.io.File;

public interface FileProcessor {
	
	public void processFile(File file, String basename, String extension);

}
