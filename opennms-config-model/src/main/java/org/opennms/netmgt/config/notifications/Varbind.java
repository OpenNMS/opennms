/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notifications;

import org.opennms.netmgt.config.utils.ConfigUtils;

import java.util.Objects;

/**
 * The varbind element
 */
public class Varbind implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The varbind element number
     */
    private String vbname;

    /**
     * The varbind element value
     */
    private String vbvalue;

    public String getVbname() {
        return this.vbname;
    }

    public void setVbname(final String vbname) {
        this.vbname = ConfigUtils.assertNotEmpty(vbname, "vbname");
    }

    public String getVbvalue() {
        return this.vbvalue;
    }

    public void setVbvalue(final String vbvalue) {
        this.vbvalue = ConfigUtils.assertNotEmpty(vbvalue, "vbvalue");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vbname, this.vbvalue);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Varbind) {
            final Varbind that = (Varbind)obj;
            return Objects.equals(this.vbname, that.vbname)
                    && Objects.equals(this.vbvalue, that.vbvalue);
        }
        return false;
    }
}
