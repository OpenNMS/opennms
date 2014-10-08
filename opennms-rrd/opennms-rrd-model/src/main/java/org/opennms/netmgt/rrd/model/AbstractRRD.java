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

package org.opennms.netmgt.rrd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Abstract RRD (Round Robin Database).
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class AbstractRRD {

    /** The version of the RRD Dump. */
    private String version;

    /** The step (interval) expressed in seconds. */
    private Long step;

    /** The last update time stamp, expressed in seconds since 1970-01-01 UTC. */
    private Long lastupdate;

    /**
     * Creates the RRD.
     *
     * @return the abstract RRD
     */
    protected abstract AbstractRRD createRRD();

    /**
     * Gets the data sources.
     *
     * @return the data sources
     */
    public abstract List<? extends AbstractDS> getDataSources();

    /**
     * Gets the RRAs.
     *
     * @return the RRAs
     */
    public abstract List<? extends AbstractRRA> getRras();

    /**
     * Adds the RRA.
     *
     * @param rra the RRA
     */
    public abstract void addRRA(AbstractRRA rra);

    /**
     * Adds the data source.
     *
     * @param ds the DS
     */
    public abstract void addDataSource(AbstractDS ds);

    /**
     * Gets the data source.
     *
     * @param index the index
     * @return the data source
     */
    public abstract AbstractDS getDataSource(int index);

    /**
     * Gets the version of the RRD Dump.
     *
     * @return the version
     */
    @XmlElement(name="version")
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
     * Gets the step (interval) expressed in seconds.
     *
     * @return the step
     */
    @XmlElement(name="step")
    @XmlJavaTypeAdapter(LongAdapter.class)
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
    @XmlElement(name="lastupdate")
    @XmlJavaTypeAdapter(LongAdapter.class)
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
     * Gets the start time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA
     * @return the start time stamp (in seconds)
     */
    public Long getStartTimestamp(AbstractRRA rra) {
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
    public Long getEndTimestamp(AbstractRRA rra) {
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
    public Long findTimestampByRow(AbstractRRA rra, Row row) {
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
    public Row findRowByTimestamp(AbstractRRA rra, Long timestamp) {
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
    public void merge(AbstractRRD rrdSrc) throws IllegalArgumentException {
        if (!formatEquals(rrdSrc)) {
            throw new IllegalArgumentException("Invalid RRD format");
        }
        int rraNum = 0;
        for (AbstractRRA rra : rrdSrc.getRras()) {
            for (Row row : rra.getRows()) {
                if (!row.isNan()) {
                    Long ts = rrdSrc.findTimestampByRow(rra, row);
                    Row localRow = findRowByTimestamp(getRras().get(rraNum), ts);
                    if (localRow != null) {
                        localRow.setValues(row.getValues());
                    }
                }
            }
            rraNum++;
        }
    }

    /**
     * Merge.
     *
     * @param rrdList the RRD list
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void merge(List<? extends AbstractRRD> rrdList) throws IllegalArgumentException {
        if (rrdList.size() != getDataSources().size()) {
            throw new IllegalArgumentException("Cannot merge RRDs because the amount of RRDs doesn't match the amount of data sources.");
        }
        for (AbstractRRD arrd : rrdList) {
            if (!getVersion().equals(getVersion())) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them have a different file version.");
            }
            if (!hasEqualsRras(arrd)) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them as different RRA configuration.");
            }
            if (arrd.getDataSources().size() > 1) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them has more than one DS.");
            }
        }
        Collections.sort(rrdList, new Comparator<AbstractRRD>() {
            @Override
            public int compare(AbstractRRD a, AbstractRRD b) {
                int aInt = getIndex(a.getDataSources().get(0).getName());
                int bInt = getIndex(b.getDataSources().get(0).getName());
                return aInt - bInt;
            }
        });
        for (int i = 0; i < getRras().size(); i++) {
            AbstractRRA rra = getRras().get(i);
            for (int j = 0; j < rra.getRows().size(); j++) {
                Row row = rra.getRows().get(j);
                for (int k = 0; k < row.getValues().size(); k++) {
                    Double v = rrdList.get(k).getRras().get(i).getRows().get(j).getValues().get(0);
                    if (!v.isNaN()) {
                        row.getValues().set(k, v);
                    }
                }
            }
        }
    }

    /**
     * Split.
     * <p>If the RRD contain several data sources, it will return one RRD per DS.
     * Otherwise, it will throw an exception.</p>
     *
     * @return the RRD list
     * @throws IllegalArgumentException the illegal argument exception
     */
    public List<AbstractRRD> split() throws IllegalArgumentException {
        if (getDataSources().size() <= 1) {
            throw new IllegalArgumentException("Cannot split an RRD composed by 1 or less data-sources.");
        }
        List<AbstractRRD> rrds = new ArrayList<AbstractRRD>();
        for (int i = 0; i < getDataSources().size(); i++) {
            AbstractRRD rrd = createRRD();
            rrd.addDataSource(getDataSource(i));
            for (int j = 0; j < getRras().size(); j++) {
                AbstractRRA currentRra = getRras().get(j);
                AbstractRRA rra = currentRra.createSingleRRA(i);
                for (Row currentRow : currentRra.getRows()) {
                    Row row = new Row();
                    row.getValues().add(currentRow.getValues().get(i));
                    rra.getRows().add(row);
                }
                rrd.addRRA(rra);
            }
            rrds.add(rrd);
        }
        return rrds;
    }

    /**
     * Format equals.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean formatEquals(AbstractRRD rrd) {
        if (this.step != null) {
            if (rrd.step == null) return false;
            else if (!(this.step.equals(rrd.step))) 
                return false;
        }
        else if (rrd.step != null)
            return false;

        if (this.getDataSources() != null) {
            if (rrd.getDataSources() == null) return false;
            else if (!(this.getDataSources().size() == rrd.getDataSources().size())) 
                return false;
        }
        else if (rrd.getDataSources() != null)
            return false;

        for (int i = 0; i < getDataSources().size(); i++) {
            if (!getDataSources().get(i).formatEquals(rrd.getDataSources().get(i)))
                return false;
        }

        if (!hasEqualsRras(rrd)) {
            return false;
        }

        return true;
    }

    /**
     * Checks for equals RRAs.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean hasEqualsRras(AbstractRRD rrd) {
        if (this.getRras() != null) {
            if (rrd.getRras() == null) return false;
            else if (!(this.getRras().size() == rrd.getRras().size())) 
                return false;
        }
        else if (rrd.getRras() != null)
            return false;

        for (int i = 0; i < getRras().size(); i++) {
            if (!getRras().get(i).formatEquals(rrd.getRras().get(i)))
                return false;
        }

        return true;
    }

    /**
     * Gets the index.
     *
     * @param dsName the DS name
     * @return the index
     */
    protected int getIndex(String dsName) {
        if (getDataSources() == null) {
            return -1;
        }
        for (int i=0; i < getDataSources().size(); i++) {
            if (getDataSources().get(i).getName().equals(dsName)) {
                return i;
            }
        }
        return -1;
    }

}
