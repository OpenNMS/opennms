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
package org.opennms.rrdtool.old;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.rrdtool.DS;
import org.opennms.rrdtool.LongAdapter;
import org.opennms.rrdtool.Row;

/**
 * The Class RRD (Round Robin Database).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RrdOld {

    /** The version of the RRD Dump. */
    @XmlElement
    private String version;

    /** The step (interval) expressed in seconds. */
    @XmlElement
    private Long step;

    /** The last update time stamp, expressed in seconds since 1970-01-01 UTC. */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    private Long lastupdate;

    /** The data sources. */
    @XmlElement(name="ds")
    private List<DS> dataSources = new ArrayList<DS>();

    /** The RRAs. */
    @XmlElement(name="rra")
    private List<RraOld> rras = new ArrayList<RraOld>();

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
    public List<RraOld> getRras() {
        return rras;
    }

    /**
     * Sets the RRAs.
     *
     * @param rras the new RRAs
     */
    public void setRras(List<RraOld> rras) {
        this.rras = rras;
    }

    /**
     * Gets the start time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA
     * @return the start time stamp (in seconds)
     */
    public Long getStartTimestamp(RraOld rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getEndTimestamp(rra) - getStep() * rra.getPdpPerRow() * rra.getRows().size();
    }

    /**
     * Gets the end time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA
     * @return the end time stamp (in seconds)
     */
    public Long getEndTimestamp(RraOld rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getLastUpdate() - getLastUpdate() % (getStep() * rra.getPdpPerRow()) + (getStep() * rra.getPdpPerRow());
    }

    /**
     * Finds the row time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA object
     * @param row the Row object
     * @return the long
     */
    public Long findTimestampByRow(RraOld rra, Row row) {
        int rowNumber = rra.getRows().indexOf(row);
        if (rowNumber < 0) {
            return null;
        }
        return getStartTimestamp(rra) + rowNumber * rra.getPdpPerRow() * getStep();
    }

    /**
     * Gets the row that corresponds to a specific time stamp (expressed in seconds since 1970-01-01 UTC).
     *
     * @param rra the RRA
     * @param timestamp the row time stamp
     * @return the row object
     */
    public Row findRowByTimestamp(RraOld rra, Long timestamp) {
        try {
            Long n = (timestamp - getStartTimestamp(rra)) / (rra.getPdpPerRow() * getStep());
            return rra.getRows().get(n.intValue());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Merge.
     * <p>Merge the content of rrdSrc into this RRD.</p>
     * <p>The format must be equal in order to perform the merge operation.</p>
     * 
     * @param rrdSrc the RRD source
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void merge(RrdOld rrdSrc) throws IllegalArgumentException {
        if (!formatEquals(rrdSrc)) {
            throw new IllegalArgumentException("Invalid RRD format");
        }
        int rraNum = 0;
        for (RraOld rra : rrdSrc.getRras()) {
            for (Row row : rra.getRows()) {
                if (!row.isNan()) {
                    Long ts = rrdSrc.findTimestampByRow(rra, row);
                    Row localRow = findRowByTimestamp(rras.get(rraNum), ts);
                    if (localRow != null) {
                        localRow.setValues(row.getValues());
                    }
                }
            }
            rraNum++;
        }
    }

    /**
     * Format equals.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean formatEquals(RrdOld rrd) {
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

}
