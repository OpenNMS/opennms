package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FasterFilesystemForeignSourceRepositoryTest {

	@Test
	public void testForEachFile() throws Exception {
		
		File base = File.createTempFile("ffstr", "dir");
		base.delete();
		base.mkdirs();
		base.deleteOnExit();
		
		File foreignSourceDir = new File(base, "foreignSources");
		foreignSourceDir.mkdirs();
		foreignSourceDir.deleteOnExit();
		
		File foreignSource = new File(foreignSourceDir, "test.xml");
		FileUtils.touch(foreignSource);
		
		File requisitionDir = new File(base, "requisitions");
		requisitionDir.mkdirs();
		requisitionDir.deleteOnExit();
		
		FasterFilesystemForeignSourceRepository repo = new FasterFilesystemForeignSourceRepository();
		repo.setForeignSourcePath(foreignSourceDir.getAbsolutePath());
		repo.setRequisitionPath(requisitionDir.getAbsolutePath());
		
		final AtomicReference<String> found = new AtomicReference<String>(null);
		repo.forEachFile(foreignSourceDir, ".xml", new FileProcessor() {

			@Override
			public void processFile(File dir, String basename, String extension) {
				found.set(basename);
			}
			
		});
		
		assertEquals("test", found.get());
	}

}
