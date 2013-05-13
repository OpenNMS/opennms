/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.provision.adapters.link.config.dao.DefaultLinkAdapterConfigurationDao;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>DefaultLinkMatchResolverImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultLinkMatchResolverImpl implements LinkMatchResolver, InitializingBean {
    @Autowired
    private DefaultLinkAdapterConfigurationDao m_configDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getAssociatedEndPoint(String endPoint) {
        if (m_configDao != null) {
            for (LinkPattern p : m_configDao.getPatterns()) {
                String endPointResolvedTemplate = p.resolveTemplate(endPoint);
                if (endPointResolvedTemplate != null) {
                    return endPointResolvedTemplate;
                }
            }
        }

        return null;
    }
}
