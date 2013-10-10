/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.rrdtool;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Class RRD.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RRD {

    /** The version. */
    @XmlElement
    private String version;

    /** The step. */
    @XmlElement
    private Long step;

    /** The lastupdate. */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long lastupdate;

    /** The data sources. */
    @XmlElement(name="ds")
    private List<DS> dataSources = new ArrayList<DS>();

    /** The RRAs. */
    @XmlElement(name="rra")
    private List<Rra> rras = new ArrayList<Rra>();

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the step.
     *
     * @return the step
     */
    public Long getStep() {
        return step;
    }

    /**
     * Sets the step.
     *
     * @param step the new step
     */
    public void setStep(Long step) {
        this.step = step;
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    public Long getLastUpdate() {
        return lastupdate;
    }

    /**
     * Sets the last update.
     *
     * @param lastUpdate the new last update
     */
    public void setLastUpdate(Long lastUpdate) {
        this.lastupdate = lastUpdate;
    }

    /**
     * Gets the data sources.
     *
     * @return the data sources
     */
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

    /**
     * Gets the RRAs.
     *
     * @return the RRAs
     */
    public List<Rra> getRras() {
        return rras;
    }

    /**
     * Sets the RRAs.
     *
     * @param rras the new RRAs
     */
    public void setRras(List<Rra> rras) {
        this.rras = rras;
    }

    /**
     * Gets the start timestamp.
     *
     * @param rra the RRA
     * @return the start timestamp
     */
    public Long getStartTimestamp(Rra rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getEndTimestamp(rra) - getStep() * rra.getPdpPerRow() * rra.getRows().size();
    }
    
    /**
     * Gets the end timestamp.
     *
     * @param rra the RRA
     * @return the end timestamp
     */
    public Long getEndTimestamp(Rra rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getLastUpdate() - getLastUpdate() % (getStep() * rra.getPdpPerRow()) + (getStep() * rra.getPdpPerRow());
    }

    /**
     * Format equals.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean formatEquals(RRD rrd) {
        if (this.step != null) {
            if (rrd.step == null) return false;
            else if (!(this.step.equals(rrd.step))) 
                return false;
        }
        else if (rrd.step != null)
            return false;

        if (this.dataSources != null) {
            if (rrd.dataSources == null) return false;
            else if (!(this.dataSources.size() == rrd.dataSources.size())) 
                return false;
        }
        else if (rrd.dataSources != null)
            return false;

        for (int i = 0; i < dataSources.size(); i++) {
            if (!dataSources.get(i).formatEquals(rrd.dataSources.get(i)))
                return false;
        }

        if (this.rras != null) {
            if (rrd.rras == null) return false;
            else if (!(this.rras.size() == rrd.rras.size())) 
                return false;
        }
        else if (rrd.rras != null)
            return false;

        for (int i = 0; i < rras.size(); i++) {
            if (!rras.get(i).formatEquals(rrd.rras.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Merge.
     * <p>Merge the content of rrdSrc into this RRD.</p>
     * <p>The format must be equal in order to perform the merge operation.</p>
     * 
     * @param rrdSrc the RRD source
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void merge(RRD rrdSrc) throws IllegalArgumentException {
        if (!formatEquals(rrdSrc)) {
            throw new IllegalArgumentException("Invalid RRD format");
        }
        for (int i=0; i < rras.size(); i++) {
            for (int j=0; j < rras.get(i).getRows().size(); j++) {
                Row local  = rras.get(i).getRows().get(j);
                Row source = rrdSrc.getRras().get(i).getRows().get(j);
                for (int k=0; k < local.getValues().size(); k++) {
                    Double v = source.getValues().get(k);
                    if (!v.isNaN()) {
                        local.getValues().set(k, v);
                    }
                }
            }
        }
    }

}
