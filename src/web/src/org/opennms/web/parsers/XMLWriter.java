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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//

package org.opennms.web.parsers;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.apache.xerces.parsers.*;
import javax.xml.parsers.*;

import org.opennms.core.resource.Vault;
import org.opennms.web.*;
import org.opennms.web.parsers.*;


/**This class is used to parse data from and save data to the
 * users.xml file.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public abstract class XMLWriter
{
	/**The header information parsed from the file
	*/
	protected static XMLHeader m_headerInfo;
	
	/**
	*/
	protected Document m_document;
	
	/**
	*/
	protected DocumentBuilder m_docBuilder;
	
	/**
	*/
	protected String m_fileName;
	
	/**
	*/
	protected String m_baseName;
	
	/**
	*/
	protected String m_xmlPath;
	
	/**
	*/
	protected String m_dtdPath;
	
	/**
	*/
	protected String m_dtd;
	
	/**
	*/
	protected static final String XML_EXTENSION = ".xml";
	
	/**
	*/
	protected static final String DTD_EXTENSION = ".dtd";
	
	/**Default constructor, intializes the member variables
	*/
	public XMLWriter(String fileName)
		throws XMLWriteException
	{
		super();
		
		m_fileName = fileName;
		m_xmlPath = Vault.getProperty("opennms.home") + 
			    File.separator +
			    "etc" + 
			    File.separator;
		
		m_dtdPath = Vault.getProperty("opennms.home") + 
			    File.separator +
			    "etc" + 
			    File.separator;
		
		if (m_fileName.endsWith(XML_EXTENSION))
		{
			m_baseName = m_fileName.substring(0, m_fileName.length() - XML_EXTENSION.length());
		}
		else
		{
			throw new XMLWriteException(m_fileName + " has an invalid extension for an xml file.");
		}
		
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			m_docBuilder = docFactory.newDocumentBuilder();
			
			m_document = m_docBuilder.newDocument();
		}
		catch(Exception e)
		{
			throw new XMLWriteException(e.getMessage());
		}
	}
	
	/**
	*/
	protected void loadDTD(String dtdFileName)
		throws FileNotFoundException, IOException
	{
		StringBuffer dtdBuffer = new StringBuffer();
		
		//open up the original file
		BufferedReader originalBuffer = new BufferedReader(new FileReader(dtdFileName));
		String input;
		while((input = originalBuffer.readLine()) != null)
		{
			dtdBuffer.append(input).append("\n");
		}
		
		m_dtd = dtdBuffer.toString();
	}
	
	/**
	*/
	public String getDTD(String dtdFileName)
		throws FileNotFoundException, IOException
	{
		String dtd = null;
		
		if (m_dtd != null)
		{
			dtd = m_dtd;
		}
		else
		{
			loadDTD(dtdFileName);
			
			if (m_dtd != null)
				dtd = m_dtd;
		}
		
		return dtd;
	}
	
	/**
	*/
	public String getVersion()
	{
		String version = "?";
		String revTag = "$Revision:";
		
		try
		{
			String dtd = getDTD(m_dtdPath + m_baseName + DTD_EXTENSION);
			
			//find the $Revision comment
			int revStart = dtd.indexOf(revTag);
			int revEnd = dtd.indexOf("$", revStart + revTag.length());
			version = dtd.substring(revStart + revTag.length(), revEnd);
		}
		catch (Exception e)
		{
			//do nothing, a default value will be returned
		}
		
		return version.trim();
	}
	
	/**This method attempts to make a backup of the xml file. It is recommended
	   that this method is called prior to making the call to save(), so that the
	   original config file will be recoverable if the writting of the new config
	   file fails.
	*/
	public void backup()
		throws XMLWriteException
	{
		try
		{
			FileBackup.makeBackup(m_xmlPath + m_fileName, m_baseName);
		}
		catch (IOException e)
		{
			throw new XMLWriteException(e.getMessage());
		}
	}
	
	/**
	*/
	protected void serializeToFile()
		throws XMLWriteException
	{
		try
		{
			FileWriter writer = new FileWriter(m_xmlPath + m_fileName);
			
			OutputFormat format = new OutputFormat(m_document);
			format.setOmitXMLDeclaration(true);
			format.setIndenting(true);
			
			XMLSerializer serializer = new XMLSerializer(writer, format);
			serializer.asDOMSerializer();
			
			//write the DTD, it has the XML Declare line in it
			writer.write(getDTD(m_dtdPath + m_baseName + DTD_EXTENSION));
			writer.write("\n");
			
			//write the DOM tree
			serializer.serialize(m_document);
			
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			throw new XMLWriteException(e.getMessage());
		}
	}
	
	/**
	*/
	public Element addEmptyElement(Element parent, String elementName)
	{
		Element newElement = m_document.createElement(elementName);
		parent.appendChild(newElement);
		
		return newElement;
	}
	
	/**
	*/
	public Element addDataElement(Element parent, String elementName, String data)
	{
		Element newElement = addEmptyElement(parent, elementName);
		
		newElement.appendChild(m_document.createTextNode(data));
		
		return newElement;
	}
	
	/**
	*/
	public Element addCDataElement(Element parent, String elementName, String data)
		throws XMLWriteException
	{
		Element newElement = addEmptyElement(parent, elementName);
		
		try
		{
			newElement.appendChild(m_document.createCDATASection(data));
		}
		catch (DOMException e)
		{
			throw new XMLWriteException(e.getMessage());
		}
		
		return newElement;
	}
	
	/**
	*/
	public void save(Collection list)
	 	throws XMLWriteException
	{
		//the saveDocument method rebuilds the xml document node by node, so 
		//we need an empty document
		m_document = m_docBuilder.newDocument();
		
		saveDocument(list);
	}
	
	/**
	*/
	abstract protected void saveDocument(Collection list)
		throws XMLWriteException;
}
