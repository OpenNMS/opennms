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
 * GNU General Public License for more details.
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

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public interface OVsPMD extends Library {

    public static final OVsPMD INSTANCE = (OVsPMD)Native.synchronizedLibrary((OVsPMD)Native.loadLibrary("ov", OVsPMD.class));

    public static class OVsPMDCommand extends Structure {
	public int code;
	public String message;
    };
	
    public static final int OVS_NO_CODE                  = 0;
    public static final int OVS_RSP_SUCCESS              = 1;
    public static final int OVS_RSP_FAILURE              = 2;
    public static final int OVS_RSP_DONE                 = 3;
    public static final int OVS_RSP_STATUS               = 4;
    public static final int OVS_CMD_NOOP                 = 5;
    public static final int OVS_CMD_EXIT                 = 6;
    public static final int OVS_CMD_DEPENDENCY_FAILED    = 7;
    public static final int OVS_REQ_START                = 8;
    public static final int OVS_REQ_STOP                 = 9;
    public static final int OVS_REQ_STATUS               = 10;
    public static final int OVS_REQ_ABORT                = 11;
    public static final int OVS_RSP_ERROR                = 12;

    public static final int OVS_CMD_PAUSE                = 15;
    public static final int OVS_CMD_RESUME               = 16;
    public static final int OVS_RSP_PAUSE_ACK            = 17;
    public static final int OVS_RSP_RESUME_ACK           = 18;
    public static final int OVS_RSP_PAUSE_NACK           = 19;
    public static final int OVS_RSP_RESUME_NACK          = 20;

    public static final int OVS_RSP_VERBOSE_MSG          = 21;
    public static final int OVS_RSP_LAST_MSG             = 22;

    public static final int OVS_STATE_RUNNING            = 23;
    public static final int OVS_STATE_PAUSED             = 24;
    public static final int OVS_STATE_ALL_STOPPED        = 25;
    public static final int OVS_STATE_START_COMPLETE     = 26;

    // int OVsInit(int* sp)
    public int OVsInit(IntByReference sp);
    
    // int OVsInitComplete(OVsCodeType code, char* message)
    public int OVsInitComplete(int code, String message);
    
    // int OVsReceive(OVsPMDCommand* command)
    public int OVsReceive(OVsPMDCommand command);
    
    // int OVsDone(char *message)
    public int OVsDone(String response);
    
    // int OVsResponse(OVsCodeType code, char* message)
    public int OVsResponse(int code, String message);

}
