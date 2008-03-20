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
// 2008 Mar 20: Remove commented-out System.err.println. - dj@opennms.org
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
// Modification History:
//
// September 22 2000 - Sowmya
//	1. Changed BBParser to parse input streams also
//	2. Delinked the parse() from the constructor to make the flow more logical
//

package org.opennms.web.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Bluebird parser for XML.
 * 
 * BBParser provides basic XML parsing functionality to parse XML from a file or
 * an input stream.
 * 
 * BBParser creates a DOMParser which creates a DOM tree of the XML being
 * parsed. BBParser provides the error handling routines for the DOM parser and
 * provides some methods to traverse the elements of the DOM tree created as a
 * result of the parse.
 * 
 * The DOMParser created throws an IOException if the xml file is not found or
 * if the xml does not conform to its DTD. In addition to this, BBParser and its
 * sub-classes can set their own exception messages and throw exceptions if the
 * values read are not what is expected.
 * 
 * Sub-classes will need to override the 'processElement()' method and implement
 * their own storage and data getter methods specific to the XML that is being
 * processed
 * 
 * 
 * @author Sowmya
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * @deprecated Use a Castor-based parser instead.
 */
public class BBParser {
    /**
     * Error number set when a typical parm block does not have both the parm
     * name and value pair as expected
     */
    protected final int ATTRIB_VALUE_PAIR_ERR = -1000;

    /**
     * Error number set when the value read is null
     */
    protected final int NULL_VALUE_ERR = -1001;

    /**
     * Error number set by default when there is an exception
     */
    protected final int EXCEPTION = -1003;

    /**
     * Error message set when the ATTRIB_VALUE_PAIR error occurs
     */
    protected final String ATTRIB_VALUE_PAIR_ERR_STR = "Attribute-value should occur in pairs";

    /**
     * Error message set when the value read is null
     */
    protected final String NULL_VALUE_ERR_STR = "Values cannot be null";

    /**
     * Element currently being processed - will need to be set as the sub-class
     * goes through the DOM tree
     */
    protected StringBuffer m_curElement;

    /**
     * The error message set when an error is encountered
     */
    protected String m_exceptionMsg;

    /**
     * The error number set when an error is encountered
     */
    protected int m_errNum = 0;

    /**
     * DOM parser that builds the DOM tree
     */
    protected DOMParser m_parser;

    /**
     * Input Stream being parsed
     */
    protected InputSource m_inpSource = null;

    /**
     * The error handler.
     * 
     * Class BBErrorHandler implements the org.xml.sax.ErrorHandler interface -
     * this is then set as the error handler for the DOMParser to track errors
     */
    private class BBErrorHandler implements ErrorHandler {
        public void warning(SAXParseException ex) {
            handle(ex, "[Warning]");
        }

        public void error(SAXParseException ex) {
            handle(ex, "[Error]");
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            handle(ex, "[Fatal Error]");
        }

        public void handle(SAXParseException ex, String type) {
            m_exceptionMsg = ex.getMessage();
        }
    }

    /**
     * Process the 'Document' object or the root of the DOM tree
     * 
     * @return true if the tree traversal is successful, false otherwise
     */
    protected boolean processDocument(Document doc) {
        Element root = doc.getDocumentElement();
        if (null != root) {
            return processElement(root, true);
        }

        return false;
    }

    /**
     * Processes a 'Node' object of the DOM tree depending upon the node type
     * 
     * @return true if the node is read succefully
     */
    protected boolean processNode(Node node) {
        boolean bRet = false;

        switch (node.getNodeType()) {
        case Node.DOCUMENT_NODE:
            bRet = processDocument((Document) node);
            break;

        /*
         * If not any of the types below, warn!
         */

        case Node.ATTRIBUTE_NODE:
        case Node.CDATA_SECTION_NODE:
        case Node.COMMENT_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
        case Node.ENTITY_NODE:
        case Node.ENTITY_REFERENCE_NODE:
        case Node.NOTATION_NODE:
        case Node.PROCESSING_INSTRUCTION_NODE:
        case Node.TEXT_NODE:

            bRet = true;
            break;

        case Node.ELEMENT_NODE:
            bRet = processElement((Element) node, false);
            break;

        default:
            bRet = false;
        }

        return bRet;
    }

    /**
     * Process an Element. This is the method to be overridden by sub-classes to
     * branch off and go through the DOM tree to handle elements specific to the
     * XML they are parsing - does nothing here
     * 
     * @return true if processed sucessfully, false otherwise
     */
    protected boolean processElement(Element el, boolean isRoot) {
        // do nothing - loop thru'
        return true;
    }

    /**
     * Returns the value from a " <value>2s </value>" type node
     * 
     * @return the value from a " <value>2s </value>" type node
     */
    protected String processParmValue(Node parmValueNode) {
        String value = null;

        int numChildren = parmValueNode.getChildNodes().getLength();
        if (numChildren < 1)
            return null;

        Node temp = parmValueNode.getChildNodes().item(0);

        if (temp.getNodeType() == Node.TEXT_NODE)
            value = ((Text) temp).getData();
        else if (temp.getNodeType() == Node.CDATA_SECTION_NODE)
            value = ((CDATASection) temp).getData();

        if (value != null)
            return value.trim();
        else
            return value;
    }

    /**
     * Default constructor - creates the DOMParser
     */
    public BBParser() {
        m_curElement = new StringBuffer(10);

        BBErrorHandler errors = new BBErrorHandler();

        m_parser = new DOMParser();
        m_parser.setErrorHandler(errors);
    }

    /**
     * Parse the already set 'm_inpSource'
     * 
     * @throws IOException
     *             if parse fails for any reason
     */
    public synchronized void parse() throws IOException {
        if (m_inpSource == null) {
            throw new IOException("No Input stream to parse!");
        }

        parse(m_inpSource);
    }

    /**
     * Parse the fileName passed
     * 
     * @param fileName
     *            file to be parsed
     * 
     * @throws IOException
     *             if parse fails for any reason
     */
    public synchronized void parse(String fileName) throws IOException {
        parse(new InputSource(new FileInputStream(fileName)));
    }

    /**
     * Parses the input stream passed
     * 
     * @param inpStream
     *            the input stream that contains the XML to be parsed
     * 
     * @throws IOException
     *             if parse fails for any reason
     */
    public synchronized void parse(InputStream inpStream) throws IOException {
        parse(new InputSource(inpStream));
    }

    /**
     * Parses the input stream passed
     * 
     * @param inpSource
     *            the input source that contains the XML to be parsed
     * 
     * @throws IOException
     *             if parse fails for any reason
     */
    public synchronized void parse(InputSource inpSource) throws IOException {
        m_inpSource = inpSource;

        boolean bRet = false;
        try {
            m_parser.parse(m_inpSource);

            Document doc = m_parser.getDocument();

            bRet = processDocument(doc);
        } catch (Exception e) {
            m_errNum = -1;
        }

        if (!bRet || m_errNum != 0) {
            StringBuffer temp = new StringBuffer();

            if (m_curElement != null && m_curElement.length() > 0) {
                temp.append("Input stream failed parse while processing \'" + m_curElement + "\'");

            }

            temp.append("\n\t" + getErrorMessage() + "\n");
            temp.append("Please check the format");

            throw (new IOException(temp.toString()));
        }

    }

    /**
     * Returns the error number for the parse
     * 
     * @return zero if no error, otherwise the error number
     */
    public int getErrorNumber() {
        return m_errNum;
    }

    /**
     * Returns the error message for the parse
     * 
     * @return the error message if an error occurred while parsing
     */
    public String getErrorMessage() {
        if (m_errNum == ATTRIB_VALUE_PAIR_ERR)
            return ATTRIB_VALUE_PAIR_ERR_STR;

        else if (m_errNum == NULL_VALUE_ERR)
            return NULL_VALUE_ERR_STR;

        else if (m_errNum == EXCEPTION)
            return m_exceptionMsg;

        else
            return "Unknown Error";
    }
}
