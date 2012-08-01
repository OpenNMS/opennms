/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.PropertiesUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Support class to help with configuration that needs to happen in
 * integration tests before Spring attempts to do context initialization of
 * applicationContext-dao.xml.
 * In particular, this sets up system properties that are needed by Spring.
 * System properties are not set until afterPropertiesSet() is called.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DaoTestConfigBean implements InitializingBean {
    private String m_relativeHomeDirectory = null;
    private final String m_absoluteHomeDirectory = null; 
    private String m_rrdBinary = "/bin/true";
    private String m_relativeRrdBaseDirectory = "target/test/opennms-home/share/rrd";
    private final String m_relativeImporterDirectory = "target/test/opennms-home/etc/imports";
    private final String m_relativeForeignSourceDirectory = "target/test/opennms-home/etc/foreign-sources";

    /**
     * <p>Constructor for DaoTestConfigBean.</p>
     */
    public DaoTestConfigBean() {
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_relativeHomeDirectory == null || m_absoluteHomeDirectory == null, "Only one of the properties relativeHomeDirectory and absoluteHomeDirectory can be set.");

        if (System.getProperty("org.opennms.netmgt.icmp.pingerClass") == null) {
            System.setProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jna.JnaPinger");
        }

        // Load opennms.properties into the system properties
        Properties opennmsProperties = new Properties();
        try {
            opennmsProperties.load(ConfigurationTestUtils.getInputStreamForConfigFile("opennms.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // Do any necessary substitutions that are normally handled by maven
        Properties substitutions = new Properties();
        substitutions.setProperty("install.database.driver", "org.postgres.Driver");
        substitutions.setProperty("install.share.dir", "target/test/share");
        substitutions.setProperty("install.webapplogs.dir", "target/test/logs/webapp");
        for (Map.Entry<Object, Object> entry : opennmsProperties.entrySet()) {
            //System.err.println((String)entry.getKey() + " -> " + PropertiesUtils.substitute((String)entry.getValue(), substitutions));
            System.setProperty((String)entry.getKey(), PropertiesUtils.substitute((String)entry.getValue(), substitutions));
        }

        if (m_absoluteHomeDirectory != null) {
            ConfigurationTestUtils.setAbsoluteHomeDirectory(m_absoluteHomeDirectory);
        } else if (m_relativeHomeDirectory != null) {
            ConfigurationTestUtils.setRelativeHomeDirectory(m_relativeHomeDirectory);
        } else {
            ConfigurationTestUtils.setAbsoluteHomeDirectory(ConfigurationTestUtils.getDaemonEtcDirectory().getParentFile().getAbsolutePath());
        }

        // Turn off dumb SNMP4J logging which triggers our "no logging higher than INFO" checks
        System.setProperty("snmp4j.LogFactory", "org.snmp4j.log.NoLogger");

        ConfigurationTestUtils.setRrdBinary(m_rrdBinary);
        ConfigurationTestUtils.setRelativeRrdBaseDirectory(m_relativeRrdBaseDirectory);
        ConfigurationTestUtils.setRelativeImporterDirectory(m_relativeImporterDirectory);
        ConfigurationTestUtils.setRelativeForeignSourceDirectory(m_relativeForeignSourceDirectory);
    }

    /**
     * <p>getRelativeHomeDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRelativeHomeDirectory() {
        return m_relativeHomeDirectory;
    }

    /**
     * <p>setRelativeHomeDirectory</p>
     *
     * @param relativeHomeDirectory a {@link java.lang.String} object.
     */
    public void setRelativeHomeDirectory(String relativeHomeDirectory) {
        m_relativeHomeDirectory = relativeHomeDirectory;
    }

    /**
     * <p>getRelativeRrdBaseDirectory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRelativeRrdBaseDirectory() {
        return m_relativeRrdBaseDirectory;
    }

    /**
     * <p>setRelativeRrdBaseDirectory</p>
     *
     * @param rrdBaseDirectory a {@link java.lang.String} object.
     */
    public void setRelativeRrdBaseDirectory(String rrdBaseDirectory) {
        m_relativeRrdBaseDirectory = rrdBaseDirectory;
    }

    /**
     * <p>getRrdBinary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdBinary() {
        return m_rrdBinary;
    }

    /**
     * <p>setRrdBinary</p>
     *
     * @param rrdBinary a {@link java.lang.String} object.
     */
    public void setRrdBinary(String rrdBinary) {
        m_rrdBinary = rrdBinary;
    }
}
;
