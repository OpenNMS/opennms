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
package org.opennms.netmgt.dao.jaxb;

import java.util.Collections;
import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
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
public class DefaultTl1ConfigurationDao extends AbstractJaxbConfigDao<Tl1dConfiguration, Tl1dConfiguration>implements Tl1ConfigurationDao {

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
        return Collections.unmodifiableList(getContainer().getObject().getTl1Elements());
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
    public Tl1dConfiguration translateConfig(Tl1dConfiguration config) {
        return config;
    }

}
