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
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;

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
        snmpCollectionEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return snmpCollectionEditor.isReadOnly();
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
