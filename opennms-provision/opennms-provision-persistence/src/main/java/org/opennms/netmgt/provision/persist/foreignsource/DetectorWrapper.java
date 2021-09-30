/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>DetectorWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="detector")
public class DetectorWrapper extends PluginConfig {
    /**
     * 
     */
    private static final long serialVersionUID = -6956786613597618966L;

    /**
     * <p>Constructor for DetectorWrapper.</p>
     */
    public DetectorWrapper() {
    }
    
    /**
     * <p>Constructor for DetectorWrapper.</p>
     *
     * @param pc a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public DetectorWrapper(PluginConfig pc) {
        super(pc);
    }

}

