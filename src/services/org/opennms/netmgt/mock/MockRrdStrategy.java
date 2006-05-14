//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;

public class MockRrdStrategy implements RrdStrategy {

    public void initialize() throws Exception {
        // TODO Auto-generated method stub

    }

    public void graphicsInitialize() throws Exception {
        // TODO Auto-generated method stub

    }

    public Object createDefinition(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List rraList) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void createFile(Object rrdDef) throws Exception {
        // TODO Auto-generated method stub

    }

    public Object openFile(String fileName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateFile(Object rrd, String owner, String data) throws Exception {
        // TODO Auto-generated method stub

    }

    public void closeFile(Object rrd) throws Exception {
        // TODO Auto-generated method stub

    }

    public Double fetchLastValue(String rrdFile, int interval) throws NumberFormatException, RrdException {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStats() {
        // TODO Auto-generated method stub
        return null;
    }

	public Double fetchLastValueInRange(String rrdFile, int interval, int range) throws NumberFormatException, RrdException {
		// TODO Auto-generated method stub
		return null;
	}

    public Object createDefinition(String creator, String directory, String rrdName, int step, List dataSources, List rraList) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
