/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceMapper.toPersistenceModel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.ForeignSourceDao;
import org.opennms.netmgt.dao.api.OnmsRequisitionDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessResourceFailureException;


public class DatabaseForeignSourceRepository implements ForeignSourceRepository {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private ForeignSourceDao foreignSourceDao;

    @Autowired
    private OnmsRequisitionDao requisitionDao;

    @Autowired
    private EventProxy eventProxy;

    // TODO MVR there is no need for this anymore, can be removed
    @Override
    public Set<String> getActiveForeignSourceNames() {
        // TODO MVR this may be optimized for performance reasons
        return foreignSourceDao.findAll().stream().map(fs -> fs.getName()).collect(Collectors.toSet());
    }

    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return foreignSourceDao.countAll();
    }

    @Override
    public Set<OnmsForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        return new HashSet<>(foreignSourceDao.findAll());
    }

    @Override
    public OnmsForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException {
        final OnmsForeignSource foreignSource = foreignSourceDao.get(foreignSourceName);
        if (foreignSource == null) {
            final OnmsForeignSource clonedForeignSource = new OnmsForeignSource(getDefaultForeignSource());
            clonedForeignSource.setName(foreignSourceName);
            return clonedForeignSource;
        }
        return foreignSource;
    }

    @Override
    public void save(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        validate(foreignSource);
        foreignSource.updateDateStamp();
        foreignSource.getPlugins().forEach(p -> p.setForeignSource(foreignSource));
        foreignSourceDao.save(foreignSource);
    }

    @Override
    public void delete(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        foreignSourceDao.delete(foreignSource);
    }

    @Override
    public OnmsForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        OnmsForeignSource defaultForeignSource = foreignSourceDao.get(DEFAULT_FOREIGNSOURCE_NAME);
        if (defaultForeignSource != null) {
            return defaultForeignSource;
        }
        // No default foreign source exists in the database, load from disk
        ForeignSource foreignSource = JaxbUtils.unmarshal(ForeignSource.class, new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml"));
        foreignSource.setDefault(true);
        return toPersistenceModel(foreignSource);
    }

    @Override
    public void putDefaultForeignSource(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        foreignSource.setName(DEFAULT_FOREIGNSOURCE_NAME); // overwrite name
        foreignSource.setDefault(true);
        save(foreignSource);
    }

    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        foreignSourceDao.delete(DEFAULT_FOREIGNSOURCE_NAME);
    }

    @Override
    public Set<OnmsRequisition> getRequisitions() throws ForeignSourceRepositoryException {
        return new HashSet<>(requisitionDao.findAll());
    }

    @Override
    public OnmsRequisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        return requisitionDao.get(foreignSourceName);
    }

    @Override
    public void save(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        validate(requisition);
        requisition.updateLastUpdated();
        requisitionDao.saveOrUpdate(requisition);
    }

    @Override
    public void delete(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        requisitionDao.delete(requisition.getName());
    }

    @Override
    public void validate(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        // TODO MVR
//        throw new UnsupportedOperationException("TODO MVR implement me");
    }

    @Override
    public void validate(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        // TODO MVR
//        throw new UnsupportedOperationException("TODO MVR implement me");
    }

    @Override
    public void triggerImport(ImportRequest importRequest) {
        Objects.requireNonNull(importRequest);
        LOG.debug("importRequisition: Sending import event for {}", importRequest);
        try {
            getEventProxy().send(importRequest.toReloadEvent());
        } catch (final EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event ", e);
        }
    }

    private EventProxy getEventProxy() {
        return eventProxy;
    }
}
