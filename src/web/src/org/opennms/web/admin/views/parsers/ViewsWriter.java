//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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

package org.opennms.web.admin.views.parsers;

import java.util.Collection;
import java.util.List;

import org.opennms.web.parsers.XMLHeader;
import org.opennms.web.parsers.XMLWriteException;
import org.opennms.web.parsers.XMLWriter;
import org.w3c.dom.Element;


/**
*/
public class ViewsWriter extends XMLWriter
{
	/**
	*/
	public ViewsWriter(String fileName)
		throws XMLWriteException
	{
		super(fileName);
	}
	
	/**This method saves a list of views to the specified file
	   @exception XMLWriteException
	*/
	protected void saveDocument(Collection views)
		throws XMLWriteException
	{
		Element root = m_document.createElement("viewinfo");
		m_document.appendChild(root);
		
		//write the header
		XMLHeader header = new XMLHeader(getVersion(), m_document);
		root.appendChild(header.getHeaderElement());
		
		if (views.size() > 0)
		{
			Element viewsElement = addEmptyElement(root, "views");
			
			Object viewsArray[] = views.toArray();
			for (int i = 0; i < viewsArray.length; i++)
			{
				View curView = (View)viewsArray[i];
				
				Element curViewElement = addEmptyElement(viewsElement, "view");
				
				addDataElement(curViewElement, "name", curView.getViewName());
				
				if (curView.getViewTitle() != null && !curView.getViewTitle().equals(""))
				{
					addDataElement(curViewElement, "title", curView.getViewTitle());
				}
				
				if (curView.getViewComments() != null && !curView.getViewComments().equals(""))
				{
					addDataElement(curViewElement, "comment", curView.getViewComments());
				}
				
				Element commonElement = addEmptyElement(curViewElement, "common");
				addCDataElement(commonElement, "rule", curView.getCommon());
				
				List categoryList = curView.getCategories();
					
				//if no categories don't save them
				if (categoryList.size() > 0)
				{
					Element categoryElement = addEmptyElement(curViewElement, "categories");
					
					for(int j = 0; j < categoryList.size(); j++)
					{
						Category curCategory = (Category)categoryList.get(j);
						
						Element curCategoryElement = addEmptyElement(categoryElement, "category");
						
						addCDataElement(curCategoryElement, "label", curCategory.getLabel());
						
						if (curCategory.getComments() != null && !curCategory.getComments().trim().equals(""))
						{
							addCDataElement(curCategoryElement, "categoryComment", curCategory.getComments());
						}
						
						addDataElement(curCategoryElement, "normal", curCategory.getNormal());
						addDataElement(curCategoryElement, "warning", curCategory.getWarning());
						
						//write out any services for the category
						List services = curCategory.getServices();
						for (int m = 0; m < services.size(); m++)
						{
							addDataElement(curCategoryElement, "service", (String)services.get(m));
						}
						
						addCDataElement(curCategoryElement, "rule", curCategory.getRule());
					}
				}
				
				//write out the members
				List userMembers = curView.getUserMembers();
				List groupMembers = curView.getGroupMembers();
				
				if (userMembers.size() > 0 || groupMembers.size() > 0)
				{
					Element membershipElement = addEmptyElement(curViewElement, "membership");
					
					for(int k = 0; k < userMembers.size(); k++)
					{
						Element userMemberElement = addDataElement(membershipElement, "member", (String)userMembers.get(k));
						userMemberElement.setAttribute("type", "user");
					}
					
					for(int l = 0; l < groupMembers.size(); l++)
					{
						Element groupMemberElement = addDataElement(membershipElement, "member", (String)groupMembers.get(l));
						groupMemberElement.setAttribute("type", "group");
					}
				}
			}
		}
		
		serializeToFile();
	}
}
