//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 04: Java 5 generics. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.model;

import java.io.File;
import java.util.List;

public class RrdRepository {

    private List<String> m_rraList;
    private int m_step;
    private int m_heartBeat;
    private File m_rrdBaseDir;

    public File getRrdBaseDir() {
        return m_rrdBaseDir;
    }

    public void setRrdBaseDir(File rrdBaseDir) {
        m_rrdBaseDir = rrdBaseDir;
    }

    public List<String> getRraList() {
        return m_rraList;
    }
    
    public void setRraList(List<String> rraList) {
        m_rraList = rraList;
    }

    public int getStep() {
        return m_step;
    }
    
    public void setStep(int step) {
        m_step = step;
    }

    public int getHeartBeat() {
        return m_heartBeat;
    }

    public void setHeartBeat(int heartBeat) {
        m_heartBeat = heartBeat;
    }

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
