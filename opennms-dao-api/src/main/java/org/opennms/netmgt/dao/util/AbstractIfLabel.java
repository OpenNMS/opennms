/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
