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
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.Date;
import java.util.StringTokenizer;

public class Tl1MessageProcessorImpl implements Tl1MessageProcessor {

    public Tl1Message proccessMessage(String rawMessage) {

        Tl1Message message = Tl1GenericMessage.create(rawMessage);
        StringTokenizer toColon = new StringTokenizer(rawMessage, ";");
        String reptAlarm = toColon.nextToken();
        StringTokenizer getQuote = new StringTokenizer(reptAlarm,"\"");
        String R1 = getQuote.nextToken();
        String R2 = getQuote.nextToken();
        StringTokenizer parseR1 =  new StringTokenizer(R1);

        //TODO: this needs better tokenization, for now just setting to Date()
        String tid = parseR1.nextToken();
        String almDate = parseR1.nextToken();
        String almTime = parseR1.nextToken();
        message.setTimeStamp(new Date());

        StringTokenizer parseR2 =  new StringTokenizer(R2);
        String[] R2Parts = R2.split(":");
        String equip = R2Parts[0];
        message.setEquipment(equip);

        String parms = R2Parts[1];
        message.setAdditonalParms(parms);

        StringTokenizer parmParser = new StringTokenizer(parms,",");
        message.setSeverity(parmParser.nextToken().split("=")[1]);
        return message;

    }


}
