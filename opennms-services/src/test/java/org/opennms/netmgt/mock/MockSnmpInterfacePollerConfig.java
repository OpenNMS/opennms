/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import org.opennms.netmgt.config.SnmpInterfacePollerConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MockSnmpInterfacePollerConfig implements SnmpInterfacePollerConfig {
    private String m_upValues;

    private String m_downValues;

    private boolean m_useCriteriaFilters;

    public MockSnmpInterfacePollerConfig(String upValues, String downValues, boolean useCriteriaFilters) {
        super();
        m_upValues = upValues;
        m_downValues = downValues;
        m_useCriteriaFilters = useCriteriaFilters;
    }

    @Override
    public long getInterval() {
        return 300000;
    }

    @Override
    public int getThreads() {
        return 10;
    }

    @Override
    public boolean useCriteriaFilters() {
        return m_useCriteriaFilters;
    }

    @Override
    public String getUpValues() {
        return m_upValues;
    }

    @Override
    public String getDownValues() {
        return m_downValues;
    }

    @Override
    public String getService() {
        return "SNMP";
    }

    @Override
    public String[] getCriticalServiceIds() {
        return new String[0];
    }

    @Override
    public List<String> getAllPackageMatches(String ipaddr) {
        List<String> ret = new ArrayList<>(1);
        ret.add("example1");
        return ret;
    }

    @Override
    public String getPackageName(String ipaddr) {
        return "example1";
    }

    @Override
    public Set<String> getInterfaceOnPackage(String pkgName) {
        return null;
    }

    @Override
    public boolean getStatus(String pkgName, String pkgInterfaceName) {
        return true;
    }

    @Override
    public long getInterval(String pkgName, String pkgInterfaceName) {
        return 300000;
    }

    @Override
    public Optional<String> getCriteria(String pkgName, String pkgInterfaceName) {
        return Optional.empty();
    }

    @Override
    public boolean hasPort(String pkgName, String pkgInterfaceName) {
        return false;
    }

    @Override
    public Optional<Integer> getPort(String pkgName, String pkgInterfaceName) {
        return Optional.empty();
    }

    @Override
    public boolean hasTimeout(String pkgName, String pkgInterfaceName) {
        return false;
    }

    @Override
    public Optional<Integer> getTimeout(String pkgName, String pkgInterfaceName) {
        return Optional.empty();
    }

    @Override
    public boolean hasRetries(String pkgName, String pkgInterfaceName) {
        return false;
    }

    @Override
    public Optional<Integer> getRetries(String pkgName, String pkgInterfaceName) {
        return Optional.empty();
    }

    @Override
    public boolean hasMaxVarsPerPdu(String pkgName, String pkgInterfaceName) {
        return false;
    }

    @Override
    public Integer getMaxVarsPerPdu(String pkgName, String pkgInterfaceName) {
        return null;
    }

    @Override
    public String getUpValues(String pkgName, String pkgInterfaceName) {
        return m_upValues;
    }

    @Override
    public String getDownValues(String pkgName, String pkgInterfaceName) {
        return m_downValues;
    }

    @Override
    public void rebuildPackageIpListMap() {

    }

    @Override
    public void update() throws IOException {

    }
}
