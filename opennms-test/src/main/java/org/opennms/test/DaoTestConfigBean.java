/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Add the ability to set an absolute home directory and
 *              have a sane default if neither is set. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.test;

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
 */
public class DaoTestConfigBean implements InitializingBean {
    private String m_relativeHomeDirectory = null;
    private final String m_absoluteHomeDirectory = null; 
    private String m_rrdBinary = "/bin/true";
    private String m_relativeRrdBaseDirectory = "target/test/opennms-home/share/rrd";
    private final String m_relativeImporterDirectory = "target/test/opennms-home/etc/imports";
    private final String m_relativeForeignSourceDirectory = "target/test/opennms-home/etc/foreign-sources";

    public DaoTestConfigBean() {
    }

    public void afterPropertiesSet() {
        Assert.state(m_relativeHomeDirectory == null || m_absoluteHomeDirectory == null, "Only one of the properties relativeHomeDirectory and absoluteHomeDirectory can be set.");
        
        if (m_absoluteHomeDirectory != null) {
            ConfigurationTestUtils.setAbsoluteHomeDirectory(m_absoluteHomeDirectory);
        } else if (m_relativeHomeDirectory != null) {
            ConfigurationTestUtils.setRelativeHomeDirectory(m_relativeHomeDirectory);
        } else {
            ConfigurationTestUtils.setAbsoluteHomeDirectory(ConfigurationTestUtils.getDaemonEtcDirectory().getParentFile().getAbsolutePath());
        }
        
        ConfigurationTestUtils.setRrdBinary(m_rrdBinary);
        ConfigurationTestUtils.setRelativeRrdBaseDirectory(m_relativeRrdBaseDirectory);
        ConfigurationTestUtils.setRelativeImporterDirectory(m_relativeImporterDirectory);
        ConfigurationTestUtils.setRelativeForeignSourceDirectory(m_relativeForeignSourceDirectory);
    }

    public String getRelativeHomeDirectory() {
        return m_relativeHomeDirectory;
    }

    public void setRelativeHomeDirectory(String relativeHomeDirectory) {
        m_relativeHomeDirectory = relativeHomeDirectory;
    }

    public String getRelativeRrdBaseDirectory() {
        return m_relativeRrdBaseDirectory;
    }

    public void setRelativeRrdBaseDirectory(String rrdBaseDirectory) {
        m_relativeRrdBaseDirectory = rrdBaseDirectory;
    }

    public String getRrdBinary() {
        return m_rrdBinary;
    }

    public void setRrdBinary(String rrdBinary) {
        m_rrdBinary = rrdBinary;
    }
}
;