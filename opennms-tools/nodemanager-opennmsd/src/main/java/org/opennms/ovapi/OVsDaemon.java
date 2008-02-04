/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
¯ * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.ovapi;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.sun.jna.ptr.IntByReference;

public abstract class OVsDaemon implements Runnable {
    
    protected abstract String onInit();
	
    protected abstract String onStop();

    public static void main(String[] args) {
	try {
            log("starting");
	    execute();
	} catch(Throwable t) {
	    log("an exception was caught!", t);
	}
    }	
    public static void execute() {
	OVsPMD ovspmd = OVsPMD.INSTANCE;

	IntByReference sp = new IntByReference();

	if (ovspmd.OVsInit(sp) < 0) {
	    log("error calling OVsInit");
	}

	if (ovspmd.OVsInitComplete(OVsPMD.OVS_RSP_SUCCESS, "opennmsd has initialized successfully!") < 0) {
	    log("error calling OVsInitComplete");
	}

	long start = System.currentTimeMillis();
	long end = start;
	while(end - start < 60000)  {
            log("opennms has been running for "+(end-start)/1000.0+" seconds.");
	    try { Thread.sleep(1000); } catch (InterruptedException e) {}
	    if (ovspmd.OVsResponse(OVsPMD.OVS_RSP_LAST_MSG, "opennmsd has been running for "+(end-start)/1000.0+" seconds.") < 0) {
		log("error calling OVsResponse");
	    }
            end = System.currentTimeMillis();
	}

	if (ovspmd.OVsDone("opennmsd if finished") < 0) {
	    log("error occurred calling OVsDone");
	}
    }

    public static void log(String msg) {
	log(msg, null);
    }

    public synchronized static void log(String msg, Throwable t) {
        System.err.println(msg);
	if (t != null) t.printStackTrace();
	PrintWriter out = null;
	try {
	    out = new PrintWriter(new FileWriter("/tmp/ov.out", true));
	    out.println(msg);
	    if (t != null) {
		t.printStackTrace(out);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Error logggin!", e);
	} finally {
	    try { if (out != null) out.close(); } catch (Exception e) {}
	}

    }

}
