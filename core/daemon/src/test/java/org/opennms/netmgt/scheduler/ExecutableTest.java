/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.scheduler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutableTest extends Executable {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutableTest.class);

    private final String m_name;

    public ExecutableTest(String name, int priority) {
        super(priority);
        m_name=name;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public void runExecutable() {
        LOG.info("Started: {}", m_name);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Ended: {}", m_name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExecutableTest that = (ExecutableTest) o;

        return Objects.equals(m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecutableTest{" +
                "m_name='" + m_name + '\'' +
                " m_priority=" + getPriority()
                +
                '}';
    }

    @Override
    public boolean isReady() {
        return true;
    }

}


