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
package org.opennms.netmgt.rrd;

import java.io.File;
import java.util.List;

/**
 * <p>RrdRepository class.</p>
 */
public class RrdRepository {

    private List<String> m_rraList;
    private int m_step;
    private int m_heartBeat;
    private File m_rrdBaseDir;

    /**
     * <p>getRrdBaseDir</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdBaseDir() {
        return m_rrdBaseDir;
    }

    /**
     * <p>setRrdBaseDir</p>
     *
     * @param rrdBaseDir a {@link java.io.File} object.
     */
    public void setRrdBaseDir(File rrdBaseDir) {
        m_rrdBaseDir = rrdBaseDir;
    }

    /**
     * <p>getRraList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getRraList() {
        return m_rraList;
    }
    
    /**
     * <p>setRraList</p>
     *
     * @param rraList a {@link java.util.List} object.
     */
    public void setRraList(List<String> rraList) {
        m_rraList = rraList;
    }

    /**
     * <p>getStep</p>
     *
     * @return a int.
     */
    public int getStep() {
        return m_step;
    }
    
    /**
     * <p>setStep</p>
     *
     * @param step a int.
     */
    public void setStep(int step) {
        m_step = step;
    }

    /**
     * <p>getHeartBeat</p>
     *
     * @return a int.
     */
    public int getHeartBeat() {
        return m_heartBeat;
    }

    /**
     * <p>setHeartBeat</p>
     *
     * @param heartBeat a int.
     */
    public void setHeartBeat(int heartBeat) {
        m_heartBeat = heartBeat;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(m_rrdBaseDir)
            .append('[')
            .append("Step:").append(m_step).append(',')
            .append("HeartBeat:").append(m_heartBeat).append(',')
            .append("RRAs:").append(m_rraList)
            .append(']');
        return sb.toString();
    }
}
