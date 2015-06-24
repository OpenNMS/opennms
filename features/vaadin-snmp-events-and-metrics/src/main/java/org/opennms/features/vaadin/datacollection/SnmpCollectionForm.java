/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * The Class SNMP Collection Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SnmpCollectionForm extends CustomComponent {

    /** The name. */
    final TextField name = new TextField("SNMP Collection Name");

    /** The SNMP storage flag. */
    final ComboBox snmpStorageFlag = new ComboBox("SNMP Storage Flag");

    /** The RRD. */
    final RrdField rrd = new RrdField("RRD");

    /** The include collections. */
    final IncludeCollectionField includeCollections;

    /** The Event editor. */
    final BeanFieldGroup<SnmpCollection> snmpCollectionEditor = new BeanFieldGroup<SnmpCollection>(SnmpCollection.class);

    /** The event layout. */
    final FormLayout snmpCollectionLayout = new FormLayout();

    /**
     * Instantiates a new SNMP collection form.
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public SnmpCollectionForm(final DataCollectionConfigDao dataCollectionConfigDao) {
        setCaption("SNMP Collection Detail");
        snmpCollectionLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        snmpCollectionLayout.addComponent(name);

        snmpStorageFlag.setRequired(true);
        snmpStorageFlag.addItem("select");
        snmpStorageFlag.addItem("all");
        snmpCollectionLayout.addComponent(snmpStorageFlag);

        rrd.setRequired(true);
        rrd.setWidth("100%");
        snmpCollectionLayout.addComponent(rrd);

        includeCollections = new IncludeCollectionField(dataCollectionConfigDao);
        snmpCollectionLayout.addComponent(includeCollections);

        setSnmpCollection(createBasicSnmpCollection());

        snmpCollectionEditor.bind(name, "name");
        snmpCollectionEditor.bind(snmpStorageFlag, "snmpStorageFlag");
        snmpCollectionEditor.bind(rrd, "rrd");
        snmpCollectionEditor.bind(includeCollections, "includeCollections");

        setCompositionRoot(snmpCollectionLayout);
    }

    /**
     * Gets the SNMP Collection.
     *
     * @return the SNMP Collection
     */
    public SnmpCollection getSnmpCollection() {
        return snmpCollectionEditor.getItemDataSource().getBean();
    }

    /**
     * Sets the SNMP Collection.
     *
     * @param snmpCollection the new SNMP collection
     */
    public void setSnmpCollection(SnmpCollection snmpCollection) {
        snmpCollectionEditor.setItemDataSource(snmpCollection);
    }

    /**
     * Creates the basic SNMP collection.
     *
     * @return the basic example SNMP collection
     */
    public SnmpCollection createBasicSnmpCollection() {
        SnmpCollection collection = new SnmpCollection();
        collection.setName("New Collection");
        collection.setSnmpStorageFlag("select");
        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.addRra("RRA:AVERAGE:0.5:1:2016");
        rrd.addRra("RRA:AVERAGE:0.5:12:1488");
        rrd.addRra("RRA:AVERAGE:0.5:288:366");
        rrd.addRra("RRA:MAX:0.5:288:366");
        rrd.addRra("RRA:MIN:0.5:288:366");
        collection.setRrd(rrd);
        return collection;
    }

    /**
     * Discard.
     */
    public void discard() {
        snmpCollectionEditor.discard();
    }

    /**
     * Commit.
     *
     * @throws CommitException the commit exception
     */
    public void commit() throws CommitException {
        snmpCollectionEditor.commit();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        snmpCollectionEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && snmpCollectionEditor.isReadOnly();
    }

    /**
     * Gets the SNMP collection name.
     *
     * @return the SNMP collection name
     */
    public String getSnmpCollectionName() {
        return name.getValue();
    }

    /**
     * Gets the RRD step.
     *
     * @return the RRD step
     */
    public Integer getRrdStep() {
        return rrd.getStepValue();
    }
}
