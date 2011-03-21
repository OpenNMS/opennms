package org.opennms.netmgt.config.tester.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.ConfigFileConstants;

public class ExcludeUeisProperties {
	private String[] m_ueis;

	public String[] getUeis() {
		return m_ueis;
	}

	public void setUeis(String[] ueis) {
		m_ueis = ueis;
	}

	public ExcludeUeisProperties() throws FileNotFoundException, IOException {
		Properties excludeProperties = new Properties();
		excludeProperties.load( new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME)));
		m_ueis = BundleLists.parseBundleList(excludeProperties.getProperty("excludes"));
	}
}
