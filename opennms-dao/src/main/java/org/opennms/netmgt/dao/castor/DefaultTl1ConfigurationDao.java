/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.opennms.netmgt.config.tl1d.Tl1dConfiguration;
import org.opennms.netmgt.dao.Tl1ConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DefaultTl1ConfigurationDao
 *
 * @author brozow
 */
public class DefaultTl1ConfigurationDao extends AbstractCastorConfigDao<Tl1dConfiguration, Tl1dConfiguration>implements Tl1ConfigurationDao {

    public DefaultTl1ConfigurationDao() {
        super(Tl1dConfiguration.class, "TL1d configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.Tl1ConfigurationDao#getElements()
     */
    public List<Tl1Element> getElements() {
        return Collections.unmodifiableList(getContainer().getObject().getTl1ElementCollection());
    }

    public void update() throws DataAccessResourceFailureException {
        getContainer().reload();
    }

    @Override
    public Tl1dConfiguration translateConfig(Tl1dConfiguration castorConfig) {
        return castorConfig;
    }

}
