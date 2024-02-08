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
package org.opennms.netmgt.rrd.model;

import java.util.Date;
import java.util.List;

/**
 * The Class Sample.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RrdSample extends Row implements Comparable<RrdSample> {

    /** The timestamp. */
    private long timestamp;

    /**
     * Instantiates a new sample.
     *
     * @param timestamp the timestamp in seconds
     * @param values the values
     */
    public RrdSample(long timestamp, List<Double> values) {
        setTimestamp(timestamp * 1000);
        setValues(values);
    }

    /**
     * Gets the timestamp in milliseconds.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp in milliseconds.
     *
     * @param timestamp the new timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Sample[timestamp=" + new Date(getTimestamp()) + " (" + getTimestamp() + "), values=" + getValues() + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RrdSample other = (RrdSample) obj;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(RrdSample o) {
        if (this.timestamp < o.timestamp)
            return -1;
        if (this.timestamp == o.timestamp)
            return 0;
        return 1;
    }

}
