/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2004, 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.vulnscand;

import org.opennms.netmgt.EventConstants;

/**
 * Class that holds the return values when parsing a description field from
 * Nessus.
 */
public class DescrValues {
    String descr;

    String cveEntry;

    int severity;

    public DescrValues() {
        descr = null;
        cveEntry = null;
        severity = -1;
    }

    public void useDefaults() {
        descr = "";
        severity = EventConstants.SEV_INDETERMINATE;
    }

    public boolean isValid() {
        if ((descr != null) && (severity > 0))
            return true;
        else
            return false;
    }

    public String toString() {
        return ("descr: " + descr + "\ncveEntry: " + cveEntry + "\nseverity: " + severity + "\n");
    }
}
