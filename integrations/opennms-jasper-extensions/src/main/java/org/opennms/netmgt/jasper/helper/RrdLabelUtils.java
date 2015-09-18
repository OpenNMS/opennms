/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.helper;

import org.opennms.core.utils.AlphaNumeric;

/**
 * This class is a copy of {@link org.opennms.core.utils.RrdLabelUtils}.
 * We did that to keep the dependencies down. The helper methods here should not be used anymore.
 * Any changes made here, should also be applied in the original class {@link org.opennms.core.utils.RrdLabelUtils}
 *
 * @deprecated  Do not use anymore.
 */
@Deprecated
class RrdLabelUtils {

    public static String computeNameForRRD(String ifname, String ifdescr) {
        String label = null;
        if (ifname != null && !"".equals(ifname)) {
            label = AlphaNumeric.parseAndReplace(ifname, '_');
        } else if (ifdescr != null && !"".equals(ifdescr)) {
            label = AlphaNumeric.parseAndReplace(ifdescr, '_');
        }
        return label;

    }

    public static String computePhysAddrForRRD(String physaddr) {
        String physAddrForRRD = null;

        if (physaddr != null && !physaddr.equals("")) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(physaddr);
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            }
        }

        return physAddrForRRD;

    }

    public static String computeLabelForRRD(String ifname, String ifdescr, String physaddr) {
        String name = computeNameForRRD(ifname, ifdescr);
        String physAddrForRRD = computePhysAddrForRRD(physaddr);
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }
}
