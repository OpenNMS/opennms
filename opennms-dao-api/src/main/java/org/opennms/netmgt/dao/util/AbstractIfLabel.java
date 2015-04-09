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

package org.opennms.netmgt.dao.util;

import org.opennms.core.utils.AlphaNumeric;
import org.opennms.netmgt.dao.api.IfLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class for methods to encode/decode ifLabel descriptions for
 * storing SNMP data in an RRD file.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 */
public abstract class AbstractIfLabel implements IfLabel {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIfLabel.class);

    /**
     * <p>getIfLabel</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param descr a {@link java.lang.String} object.
     * @param physAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getIfLabel(String name, String descr, String physAddr) {
        // If available ifName is used to generate the label
        // since it is guaranteed to be unique. Otherwise
        // ifDescr is used. In either case, all non
        // alpha numeric characters are converted to
        // underscores to ensure that the resulting string
        // will make a decent file name and that RRD
        // won't have any problems using it
        //
        String label = null;

        if (name != null) {
            label = AlphaNumeric.parseAndReplace(name, '_');
        } else if (descr != null) {
            label = AlphaNumeric.parseAndReplace(descr, '_');
        } else {
            throw new IllegalArgumentException("Both name and descr are null, but at least one cannot be.");
        }

        // In order to assure the uniqueness of the
        // RRD file names we now append the MAC/physical
        // address to the end of label if it is available.
        // 
        if (physAddr != null) {
            physAddr = AlphaNumeric.parseAndTrim(physAddr);
            if (physAddr.length() == 12) {
                label = label + "-" + physAddr;
            } else {
            	LOG.debug("initialize: physical address len is NOT 12, physAddr={}", physAddr);
            }
        }

        return label;
    }
}
