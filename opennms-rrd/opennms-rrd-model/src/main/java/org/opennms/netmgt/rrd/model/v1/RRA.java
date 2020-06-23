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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.rrd.model.AbstractRRA;
import org.opennms.netmgt.rrd.model.DoubleAdapter;

/**
 * The Class RRA (Round Robin Archives).
 * <p>Warning: This representation doesn't support Aberrant Behavior Detection with Holt-Winters Forecasting</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="rra")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RRA extends AbstractRRA {

    /** The consolidation function. */
    private CFType consolidationFunction;

    /** The CDP Data. */
    private List<RRADS> dataSources = new ArrayList<>();

    /** The XFF. */
    private Double xff = 0.5;

    /**
     * Gets the consolidation function.
     *
     * @return the consolidation function
     */
    @XmlElement(name="cf")
    @XmlJavaTypeAdapter(CFTypeAdapter.class)
    public CFType getConsolidationFunction() {
        return consolidationFunction;
    }

    /**
     * Sets the consolidation function.
     *
     * @param consolidationFunction the new consolidation function
     */
    public void setConsolidationFunction(CFType consolidationFunction) {
        this.consolidationFunction = consolidationFunction;
    }

    /**
     * Sets the consolidation function.
     *
     * @param consolidationFunction the new consolidation function
     */
    public void setConsolidationFunction(String consolidationFunction) {
        this.consolidationFunction = CFType.fromValue(consolidationFunction);
    }

    /**
     * Gets the data sources.
     *
     * @return the data sources
     */
    @XmlElement(name="ds")
    @XmlElementWrapper(name="cdp_prep")
    public List<RRADS> getDataSources() {
        return dataSources;
    }

    /**
     * Sets the data sources.
     *
     * @param dataSources the new data sources
     */
    public void setDataSources(List<RRADS> dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * Gets the XFF.
     * 
     * <p>XFF The xfiles factor defines what part of a consolidation interval may be made up from *UNKNOWN* data while the consolidated
     * value is still regarded as known. It is given as the ratio of allowed *UNKNOWN* PDPs to the number of PDPs in the interval.
     * Thus, it ranges from 0 to 1 (exclusive).</p>
     *
     * @return the XFF
     */
    @XmlElement(name="xff")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    public Double getXff() {
        return xff;
    }

    /**
     * Sets the XFF.
     *
     * @param xff the new XFF
     */
    public void setXff(Double xff) {
        this.xff = xff;
    }

    /**
     * Format equals.
     * 
     * @param rra the RRA object
     * @return true, if successful
     */
    public boolean formatEquals(RRA rra) {
        if (this.consolidationFunction != null) {
            if (rra.consolidationFunction == null) return false;
            else if (!(this.consolidationFunction.equals(rra.consolidationFunction))) 
                return false;
        }
        else if (rra.consolidationFunction != null)
            return false;

        if (this.xff != null) {
            if (rra.xff == null) return false;
            else if (!(this.xff.equals(rra.xff))) 
                return false;
        }
        else if (rra.xff != null)
            return false;

        return super.formatEquals(rra);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRA#createSingleRRA(int)
     */
    @Override
    protected AbstractRRA createSingleRRA(int dsIndex) throws IllegalArgumentException {
        RRA clone = new RRA();
        clone.setConsolidationFunction(getConsolidationFunction());
        clone.setPdpPerRow(getPdpPerRow());
        clone.getDataSources().add(getDataSources().get(dsIndex));
        clone.setXff(getXff());
        return clone;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRA#formatMergeable(org.opennms.netmgt.rrd.model.AbstractRRA)
     */
    @Override
    public boolean formatMergeable(AbstractRRA sourceRra) {
        if (sourceRra == null || sourceRra instanceof RRA == false)
            return false;
        if (!getPdpPerRow().equals(sourceRra.getPdpPerRow()))
            return false;
        RRA rra = (RRA) sourceRra;
        if (this.consolidationFunction != null) {
            if (rra.consolidationFunction == null) return false;
            else if (!(this.consolidationFunction.equals(rra.consolidationFunction))) 
                return false;
        }
        else if (rra.consolidationFunction != null)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.model.AbstractRRA#hasAverageAsCF()
     */
    @Override
    public boolean hasAverageAsCF() {
        return consolidationFunction.equals(CFType.AVERAGE);
    }

}
