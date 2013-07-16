/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.opennms.netmgt.config.tl1d.Tl1dConfiguration;
import org.opennms.netmgt.dao.api.Tl1ConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DefaultTl1ConfigurationDao
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultTl1ConfigurationDao extends AbstractCastorConfigDao<Tl1dConfiguration, Tl1dConfiguration>implements Tl1ConfigurationDao {

    /**
     * <p>Constructor for DefaultTl1ConfigurationDao.</p>
     */
    public DefaultTl1ConfigurationDao() {
        super(Tl1dConfiguration.class, "TL1d configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.Tl1ConfigurationDao#getElements()
     */
    /**
     * <p>getElements</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Tl1Element> getElements() {
        return Collections.unmodifiableList(getContainer().getObject().getTl1ElementCollection());
    }

    /**
     * <p>update</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void update() throws DataAccessResourceFailureException {
        getContainer().reload();
    }

    /** {@inheritDoc} */
    @Override
    public Tl1dConfiguration translateConfig(Tl1dConfiguration castorConfig) {
        return castorConfig;
    }

}
