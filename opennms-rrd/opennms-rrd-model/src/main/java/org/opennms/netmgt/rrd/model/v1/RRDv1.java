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
package org.opennms.netmgt.rrd.model.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.rrd.model.AbstractDS;
import org.opennms.netmgt.rrd.model.AbstractRRA;
import org.opennms.netmgt.rrd.model.AbstractRRD;

/**
 * The Class RRD (Round Robin Database) supports version 1 only (JRobin).
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RRDv1 extends AbstractRRD {

    /** The RRAs. */
    public List<RRA> rras = new ArrayList<>();

    /** The data sources. */
    public List<DS> dataSources = new ArrayList<>();

    /**
     * Instantiates a new RRDv1.
     */
    public RRDv1() {
        super();
        setVersion("0001");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#getRras()
     */
    @XmlElement(name="rra")
    public List<RRA> getRras() {
        return rras;
    }

    /**
     * Sets the RRAs.
     *
     * @param rras the new RRAs
     */
    public void setRras(List<RRA> rras) {
        this.rras = rras;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#getDataSources()
     */
    @XmlElement(name="ds")
    public List<DS> getDataSources() {
        return dataSources;
    }

    /**
     * Sets the data sources.
     *
     * @param dataSources the new data sources
     */
    public void setDataSources(List<DS> dataSources) {
        this.dataSources = dataSources;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#createRRD()
     */
    @Override
    protected AbstractRRD createRRD() {
        RRDv1 clone = new RRDv1();
        clone.setLastUpdate(getLastUpdate());
        clone.setStep(getStep());
        return clone;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#addDataSource(org.opennms.netmgt.rrd.model.AbstractDS)
     */
    @Override
    public void addDataSource(AbstractDS ds) {
        dataSources.add((DS)ds);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#getDataSource(int)
     */
    @Override
    public AbstractDS getDataSource(int index) {
        return dataSources.get(index);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRD#addRRA(org.opennms.netmgt.rrd.model.AbstractRRA)
     */
    @Override
    public void addRRA(AbstractRRA rra) {
        rras.add((RRA)rra);
    }

}
