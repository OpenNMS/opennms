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
//      http://www.blast.com/
//

package org.opennms.web.admin.groups.parsers;

import java.util.Collection;
import java.util.List;

import org.opennms.web.parsers.XMLHeader;
import org.opennms.web.parsers.XMLWriteException;
import org.opennms.web.parsers.XMLWriter;
import org.w3c.dom.Element;


/**This class loads and saves information from the groups.xml file
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public class GroupsWriter extends XMLWriter
{
	/**Default constructor.
	*/
	public GroupsWriter(String fileName)
		throws XMLWriteException
	{
		super(fileName);
	}
	
	/**Creates a new DOM tree document that represents the data
	   in the collection. This document will be serialized to a file to 
	   save the configuration.
	   @param groups the information to save
	   @throws XMLWriteException if cannot handle the xml
	*/
	protected void saveDocument(Collection groups) 
		throws XMLWriteException
	{
		Element root = m_document.createElement("groupinfo");
		m_document.appendChild(root);
		
		//write the header
		XMLHeader header = new XMLHeader(getVersion(), m_document);
		root.appendChild(header.getHeaderElement());
		
		if (groups.size() > 0)
		{
			Element groupsElement = addEmptyElement(root, "groups");
			
			Object groupsArray[] = groups.toArray();
			for (int i = 0; i < groupsArray.length; i++)
			{
				Group curGroup = (Group)groupsArray[i];
				
				Element curGroupElement = addEmptyElement(groupsElement, "group");
				
				addDataElement(curGroupElement, "groupName", curGroup.getGroupName());
				
				if (curGroup.getGroupComments() != null && !curGroup.getGroupComments().equals(""))
				{
					addDataElement(curGroupElement, "groupComments", curGroup.getGroupComments());
				}
				
				if (curGroup.getUserCount() > 0)
				{
					Element membersElement = addEmptyElement(curGroupElement, "userMembers");
					
					List users = curGroup.getUsers();
					for(int j = 0; j < users.size(); j++)
					{
						addDataElement(membersElement, "userID", (String)users.get(j));
					}
				}
			}
		}
		
		serializeToFile();
	}
}
