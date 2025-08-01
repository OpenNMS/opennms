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
package org.opennms.netmgt.config.collectd.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Objects;

@XmlRootElement(name = "jmx-datacollection-config")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class JmxDatacollectionConfig implements java.io.Serializable {

    @XmlAttribute(name = "rrdRepository", required = true)
    private String _rrdRepository;

    @XmlElement(name = "jmx-collection", required = true)
    private java.util.List<JmxCollection> _jmxCollectionList = new java.util.ArrayList<>();

    public void addJmxCollection(final JmxCollection jmxCollection) {
        this._jmxCollectionList.add(jmxCollection);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof JmxDatacollectionConfig) {
            JmxDatacollectionConfig temp = (JmxDatacollectionConfig) obj;
            boolean equals = Objects.equals(_rrdRepository, temp._rrdRepository)
                    && Objects.equals(_jmxCollectionList, temp._jmxCollectionList);
            return equals;
        }
        return false;
    }

    /**
     * Method getJmxCollection.
     *
     * @param index
     * @return the value of the
     * org.opennms.netmgt.config.collectd.jmx.JmxCollection at the
     * given index
     * @throws IndexOutOfBoundsException if the index
     *                                   given is outside the bounds of the collection
     */
    public JmxCollection getJmxCollection(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._jmxCollectionList.size()) {
            throw new IndexOutOfBoundsException("getJmxCollection: Index value '" + index + "' not in range [0.." + (this._jmxCollectionList.size() - 1) + "]");
        }

        return (JmxCollection) _jmxCollectionList.get(index);
    }

    public JmxCollection getJmxCollection(final String collectionName) {
        for (JmxCollection eachCollection : _jmxCollectionList) {
            if (eachCollection != null 
                    && eachCollection.getName() != null
                    && eachCollection.getName().equals(collectionName)) {
                return eachCollection;
            }
        }
        return null;
    }

    public int getJmxCollectionCount() {
        return this._jmxCollectionList.size();
    }

    public String getRrdRepository() {
        return this._rrdRepository;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_rrdRepository, _jmxCollectionList);
    }

    public boolean removeJmxCollection(final JmxCollection vJmxCollection) {
        boolean removed = _jmxCollectionList.remove(vJmxCollection);
        return removed;
    }

    public void setJmxCollectionList(final java.util.List<JmxCollection> jmxCollectionList) {
        this._jmxCollectionList.clear();
        this._jmxCollectionList.addAll(jmxCollectionList);
    }

    public java.util.List<JmxCollection> getJmxCollectionList() {
        return _jmxCollectionList;
    }

    public void setRrdRepository(final String rrdRepository) {
        this._rrdRepository = rrdRepository;
    }

    public JmxDatacollectionConfig merge(JmxDatacollectionConfig other) {
        if (other == null) {
            return this;
        }
        // Overwrite the rrdRepository iff it's null
        if (_rrdRepository == null && other._rrdRepository != null) {
            _rrdRepository = other._rrdRepository;
        }

        // Merge the lists of collections
        getJmxCollectionList().addAll(other.getJmxCollectionList());
        return this;
    }
}
