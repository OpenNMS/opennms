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
