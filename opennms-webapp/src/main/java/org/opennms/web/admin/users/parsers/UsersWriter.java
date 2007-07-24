//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics and for loops. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.admin.users.parsers;

import java.util.Collection;

import org.opennms.web.parsers.XMLHeader;
import org.opennms.web.parsers.XMLWriteException;
import org.opennms.web.parsers.XMLWriter;
import org.w3c.dom.Element;

/**
 * This class is used to parse data from and save data to the users.xml file.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 */
public class UsersWriter extends XMLWriter {
    /**
     * Create a UsersWriter. Default constructor, intializes the member
     * variables
     */
    public UsersWriter(String fileName) throws XMLWriteException {
        super(fileName);
    }

    /**
     * This method saves the list of users to the specified file
     * 
     * @throws XMLWriteException
     *             if the save failed
     */
    protected void saveDocument(Collection users) throws XMLWriteException {
        Element root = m_document.createElement("userinfo");
        m_document.appendChild(root);

        // write the header
        XMLHeader header = new XMLHeader(getVersion(), m_document);
        root.appendChild(header.getHeaderElement());

        // if there are any users print them out
        if (users.size() > 0) {
            Element usersElement = addEmptyElement(root, "users");

            Object usersArray[] = users.toArray();
            for (int i = 0; i < usersArray.length; i++) {
                User curUser = (User) usersArray[i];

                Element curUserElement = addEmptyElement(usersElement, "user");

                addDataElement(curUserElement, "userID", curUser.getUserId());

                if (curUser.getFullName() != null && !curUser.getFullName().equals("")) {
                    addDataElement(curUserElement, "fullname", curUser.getFullName());
                }

                if (curUser.getUserComments() != null && !curUser.getUserComments().equals("")) {
                    addDataElement(curUserElement, "userComments", curUser.getUserComments());
                }

                addDataElement(curUserElement, "password", curUser.getPassword());

                // write out the notification info if there is any
                NotificationInfo info = curUser.getNotificationInfo();

                if (!info.getEmail().equals("")) {
                    addDataElement(curUserElement, "email", info.getEmail());
                }

                if (!info.getPagerEmail().equals("")) {
                    addDataElement(curUserElement, "pemail", info.getPagerEmail());
                }

                if (!info.getXMPPAddress().equals("")) {
                	addDataElement(curUserElement, "xmppAddress", info.getXMPPAddress());
                }
                
                if (!info.getNumericalService().equals("") || !info.getNumericalPin().equals("")) {
                    Element numElement = addEmptyElement(curUserElement, "numericService");

                    if (!info.getNumericalService().equals("")) {
                        addDataElement(numElement, "name", info.getNumericalService());
                    }
                    if (!info.getNumericalPin().equals("")) {
                        addDataElement(numElement, "pin", info.getNumericalPin());
                    }
                }

                if (!info.getTextService().equals("") || !info.getTextPin().equals("")) {
                    Element textElement = addEmptyElement(curUserElement, "textService");

                    if (!info.getTextService().equals("")) {
                        addDataElement(textElement, "name", info.getTextService());
                    }
                    if (!info.getTextPin().equals("")) {
                        addDataElement(textElement, "pin", info.getTextPin());
                    }
                }

                if (info.getDutyScheduleCount() > 0) {
                    Element dutyElement = addEmptyElement(curUserElement, "dutySchedules");

                    for(DutySchedule dutySchedule : info.getDutySchedules()) {
                        addDataElement(dutyElement, "schedule", dutySchedule.toString());
                    }
                }
            }
        }

        serializeToFile();
    }
}
