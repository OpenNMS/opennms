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

package org.opennms.netmgt.provision.persist;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;

/**
 * <p>ForeignSourceService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ForeignSourceService {

    /**
     * <p>setDeployedForeignSourceRepository</p>
     *
     * @param repo a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    void setDeployedForeignSourceRepository(ForeignSourceRepository repo);
    /**
     * <p>setPendingForeignSourceRepository</p>
     *
     * @param repo a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    void setPendingForeignSourceRepository(ForeignSourceRepository repo);

    /**
     * <p>getAllForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     */
    Set<ForeignSource> getAllForeignSources();

    /**
     * <p>getForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource getForeignSource(String name);
    /**
     * <p>saveForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param fs a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource saveForeignSource(String name, ForeignSource fs);
    /**
     * <p>cloneForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param target a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource cloneForeignSource(String name, String target);
    /**
     * <p>deleteForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    void          deleteForeignSource(String name);

    /**
     * <p>deletePath</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @param dataPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deletePath(String foreignSourceName, String dataPath);
    /**
     * <p>addParameter</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @param dataPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addParameter(String foreignSourceName, String dataPath);

    /**
     * <p>addDetectorToForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addDetectorToForeignSource(String foreignSource, String name);
    /**
     * <p>deleteDetector</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deleteDetector(String foreignSource, String name);

    /**
     * <p>addPolicyToForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addPolicyToForeignSource(String foreignSource, String name);
    /**
     * <p>deletePolicy</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deletePolicy(String foreignSource, String name);

    /**
     * <p>getDetectorTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,String> getDetectorTypes();
    /**
     * <p>getPolicyTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,String> getPolicyTypes();
    /**
     * <p>getWrappers</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,PluginWrapper> getWrappers();
    
}
