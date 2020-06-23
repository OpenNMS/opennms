/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
