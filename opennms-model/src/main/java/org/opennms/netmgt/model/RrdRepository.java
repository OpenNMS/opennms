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

package org.opennms.netmgt.model;

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
        StringBuilder sb = new StringBuilder();
        sb.append(m_rrdBaseDir)
            .append('[')
            .append("Step:").append(m_step).append(',')
            .append("HeartBeat:").append(m_heartBeat).append(',')
            .append("RRAs:").append(m_rraList)
            .append(']');
        return sb.toString();
    }
}
