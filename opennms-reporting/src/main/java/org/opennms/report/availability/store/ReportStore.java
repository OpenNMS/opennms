package org.opennms.report.availability.store;

import java.io.File;

public interface ReportStore {

	public abstract File newFile();

	public abstract void store();

	public abstract void delete();

	public abstract String getBaseDir();

	public abstract void setBaseDir(String baseDir);

	public abstract String getFileName();

	public abstract void setFileName(String fileName);

	public abstract File getStoreFile();

	public abstract void setStoreFile(File file);

}