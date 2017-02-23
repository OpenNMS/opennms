/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.Resource;

/**
 * <p>ForeignSourceRepository interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
public interface ForeignSourceRepository {

    /**
     * <p>getActiveForeignSourceNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    Set<String> getActiveForeignSourceNames();
    
    /**
     * <p>getForeignSourceCount</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    int getForeignSourceCount() throws ForeignSourceRepositoryException;
    /**
     * <p>getForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException;
    /**
     * <p>getForeignSource</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    ForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException;
    /**
     * <p>save</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException;
    /**
     * <p>delete</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException;

    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException;
    /**
     * <p>putDefaultForeignSource</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void putDefaultForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException;
    /**
     * <p>resetDefaultForeignSource</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void resetDefaultForeignSource() throws ForeignSourceRepositoryException;

    /**
     * <p>importResourceRequisition</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    Requisition importResourceRequisition(Resource resource) throws ForeignSourceRepositoryException;
    /**
     * <p>getRequisitions</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException;
    /**
     * <p>getRequisition</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    Requisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException;
    /**
     * <p>getRequisition</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException;

    /**
     * <p>getRequisitionDate</p>
     * @param foreignSource the Foreign Source name of the requisition.
     * @return The date-stamp of the requisition, as a {@link java.util.Date}
     */
    Date getRequisitionDate(String foreignSource);

    /**
     * <p>getRequisitionURL</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link java.net.URL} object.
     */
    URL getRequisitionURL(String foreignSource);
    /**
     * <p>save</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void save(Requisition requisition) throws ForeignSourceRepositoryException;
    /**
     * <p>delete</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    void delete(Requisition requisition) throws ForeignSourceRepositoryException;
    
    /**
     * <p>getNodeRequisition</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.OnmsNodeRequisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException;
    
    void validate(ForeignSource foreignSource) throws ForeignSourceRepositoryException;
    
    void validate(Requisition requisition) throws ForeignSourceRepositoryException;
    
    /**
     * For performance reasons, a get after a save on a ForeignSourceRepository is not guaranteed to
     * return the latest saved data unless you flush first.
     * 
     * @throws ForeignSourceRepositoryException
     */
    void flush() throws ForeignSourceRepositoryException;

    /**
     * Delete all requisitions and foreign source definitions and return to defaults.
     *
     * @throws ForeignSourceRepositoryException
     */
    void clear() throws ForeignSourceRepositoryException;
}
