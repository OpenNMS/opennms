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

import org.opennms.ovapi.CLibrary.fd_set;
import org.opennms.ovapi.CLibrary.timeval;
import org.opennms.ovapi.OVsPMD.OVsPMDCommand;

import com.sun.jna.ptr.IntByReference;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;

public abstract class OVsDaemon implements Callable {
    
    private static class DefaultOVsDaemon extends OVsDaemon {

        protected String onInit() {
            return "DefaultOVsDaemon has finished initializing.";
        }

        protected String onStop() {
            return "stop called";
        }

        public Object XXcall() throws Exception {
            
            long start = System.currentTimeMillis();
            long end = start;
            while ( (end - start) < 120000 ) {
                setStatus("DefaultOVsDaemon has been running for "  + (end - start) + " ms");
                end = System.currentTimeMillis();
            }
            
            return "DefaultOVsDaemon has finished and is now exiting.";
        }
        
        public Object call() throws Exception {
            long start = System.currentTimeMillis();
            long end = start;
            int code = OVsPMD.OVS_CMD_NOOP; 
            
            CLibrary clib = CLibrary.INSTANCE;
            
            fd_set fdset = new fd_set();
            timeval tm = new timeval();

            boolean finished = false;
            while (!finished) {
                fdset.zero();
                fdset.set(getPmdFd());
            
                if (!tm.isSet()) {
                    tm.setTimeInMillis(1000);
                }
            
                long selectStart = System.currentTimeMillis();
                int fds = clib.select(getPmdFd()+1, fdset, null, null, tm);
                long selectEnd = System.currentTimeMillis();
                end = selectEnd;
                
                setStatus("Select returned after "+(selectEnd - selectStart)+ " millis.  pmd " + 
                        (fdset.isSet(getPmdFd()) ? "is" : "is not") + " set in readfds, return val is "+fds+", elapsed time = "+(end - start)+ "ms");
                
                code = OVsPMD.OVS_CMD_NOOP;
                if (fdset.isSet(getPmdFd())) {
                    code = readPmdCmd();
                    setStatus("Received cmd code "+code+" from pmd");
                }
                
                if (code == OVsPMD.OVS_CMD_EXIT) {
                    finished = true;
                }
            }     
            
            Thread.sleep(5000);
            end = System.currentTimeMillis();
            return "DefaultOVsDaemon has finished and is now exiting after "+(end-start)+" ms. code was "+code+" pmdfs set is "+fdset.isSet(getPmdFd());
        }
        
    }
    
    private OVsPMD m_ovspmd = OVsPMD.INSTANCE;
    private int m_ovspmdFd;

    protected abstract String onInit();

    protected abstract String onStop();

    public static void main(String[] args) {
        try {
            log("starting");
            OVsDaemon daemon = new DefaultOVsDaemon();
            daemon.execute();
        } catch (Throwable t) {
            log("an exception was caught!", t);
        }
    }
    
    public int getPmdFd() {
        return m_ovspmdFd;
    }

    public void execute() {

        IntByReference sp = new IntByReference();

        if (m_ovspmd.OVsInit(sp) < 0) {
            log("error calling OVsInit");
        }
        
        m_ovspmdFd = sp.getValue();

        String initResponse = "";
        int success = OVsPMD.OVS_RSP_FAILURE;
        try {
        
            initResponse = onInit();
            success = OVsPMD.OVS_RSP_SUCCESS;
            
        } catch (Throwable t) {
            initResponse = "Exception occurred initializing "+this+": "+t;
            log(initResponse, t);
        }

        if (m_ovspmd.OVsInitComplete(success, initResponse) < 0) {
            log("error calling OVsInitComplete");
        }
        
        String callmsg;
        try {
            callmsg = (String)call();
        } catch (Throwable t) {
            callmsg = "Exception occurred calling "+this+": "+t;
            log(callmsg, t);
        }
        
        if (m_ovspmd.OVsDone(callmsg) < 0) {
            log("error occurred calling OVsDone");
        }
    }
    
    public void setStatus(String message) {
        if (m_ovspmd.OVsResponse(OVsPMD.OVS_RSP_LAST_MSG, message) < 0) {
            log("error calling OVsResponse");
        }
    }

    public int readPmdCmd() {
        OVsPMDCommand command = new OVsPMDCommand();
        
        if (m_ovspmd.OVsReceive(command) < 0) {
            log("error calling OVsReceive");
        }
        
        return command.code;
    }

    public static void log(String msg) {
        log(msg, null);
    }

    public synchronized static void log(String msg, Throwable t) {
        System.err.println(msg);
        if (t != null)
            t.printStackTrace();
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
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
            }
        }

    }

}
