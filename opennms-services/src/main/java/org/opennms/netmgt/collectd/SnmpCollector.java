/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.Map;

import org.opennms.netmgt.collection.api.AbstractServiceCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switch to choose between old and new implementation. Will be removed once we are happy with the refactoring.
 *
 */
public class SnmpCollector extends AbstractServiceCollector {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollector.class);

    /**
     * Name of monitored service.
     */
    static final String SERVICE_NAME = "SNMP";

    /**
     * The character to replace non-alphanumeric characters in Strings where
     * needed.
     */
    static final char nonAnRepl = '_';

    /**
     * The String of characters which are exceptions for
     * AlphaNumeric.parseAndReplaceExcept in if Aliases
     */
    static final String AnReplEx = "-._";

    /**
     * Value of MIB-II ifAlias oid
     */
    static final String IFALIAS_OID = ".1.3.6.1.2.1.31.1.1.1.18";

    /**
     * Object identifier used to retrieve interface count. This is the MIB-II
     * interfaces.ifNumber value.
     */
    static final String INTERFACES_IFNUMBER = ".1.3.6.1.2.1.2.1";

    /**
     * Object identifier used to retrieve system uptime. This is the MIB-II
     * system.sysUpTime value.
     */
    static final String NODE_SYSUPTIME = ".1.3.6.1.2.1.1.3";

    /**
     * Valid values for the 'snmpStorageFlag' attribute in datacollection-config
     * XML file. "primary" = only primary SNMP interface should be collected and
     * stored "all" = all primary SNMP interfaces should be collected and stored
     */
    public static final String SNMP_STORAGE_PRIMARY = "primary";

    static final String SNMP_STORAGE_SELECT = "select";

    private SnmpCollectorAbstract delegate;

    public SnmpCollector(){
        boolean useOld = false;
        if(useOld) {
            delegate = new SnmpCollectorOld();
        } else {
            delegate = new SnmpCollectorNew();
        }
        LOG.info("will use %s implementation", delegate.getClass().getSimpleName());
    }

    /**
     * Returns the name of the service that the plug-in collects ("SNMP").
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return delegate.serviceName();
    }

    @Override
    public void initialize() throws CollectionInitializationException {
    	delegate.initialize();
    }

    @Override
    public void validateAgent(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        delegate.validateAgent(agent, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * Perform data collection.
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        return delegate.collect(agent, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return delegate.getRrdRepository(collectionName);
    }
}
