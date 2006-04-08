package org.opennms.netmgt.model;

import java.io.File;
import java.util.List;

public class OnmsRepository {
	
	/** the length of an interval between collections in seconds */
	private int m_step;
	
	/** The base directory to use for data storgate */
	private File m_dataStorageDir;
	
	/** Describes data collection storage parameters for each stored data attribute */
	private List m_dataStorageSpecs;

	public File getDataStorageDir() {
		return m_dataStorageDir;
	}

	public void setDataStorageDir(File dataStorageDir) {
		m_dataStorageDir = dataStorageDir;
	}

	public List getDataStorageSpecs() {
		return m_dataStorageSpecs;
	}

	public void setDataStorageSpecs(List dataStorageSpecs) {
		m_dataStorageSpecs = dataStorageSpecs;
	}

	public int getStep() {
		return m_step;
	}

	public void setStep(int step) {
		m_step = step;
	}



}
