/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.netmgt.collectd.jmxhelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
public class JmxTest implements JmxTestMBean {

    private static Logger logger = LoggerFactory.getLogger(JmxTest.class);

    String name = "Jmx Test";

    int x = 42;

    int writable = 0;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public Integer getInteger() {
        return new Integer(42);
    }

    @Override
    public Long getLong() {
        return new Long(42);
    }

    @Override
    public void setWritableY(int writable) {
        this.writable = writable;
    }

    @Override
    public int getWritableY() {
        return writable;
    }

    @Override
    public String getNullString() {
        return null;
    }
}
