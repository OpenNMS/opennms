//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.report.availability.render;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

/**
 * @author jsartin
 * 
 */
public class PDFReportRenderer implements ReportRenderer {

	private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	private Resource inputResource;
	
	private Resource outputResource;
	
	private Resource xsltResource;

	private Resource fopResource;

	public void render() throws ReportRenderException {
		
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(PDFReportRenderer.class);
		Logger avalonLogger = new Log4JLogger(log);
		
		try {
            if (log.isInfoEnabled())
                log.info("XSL File " + xsltResource.getDescription());
            
            Reader xsl = new FileReader(xsltResource.getFile());
            
            if (log.isInfoEnabled())
                log.info("input File : " + inputResource.getDescription());
            
			Reader xml = new FileReader(inputResource.getFile());

			if (log.isInfoEnabled())
	                log.info("fot File : " + fopResource.getDescription());
			
			FileWriter fotWriter = new FileWriter(fopResource.getFile());
			           
            if (log.isInfoEnabled())
                log.info("output File : " + outputResource.getDescription());
                           
            FileOutputStream pdfOutputStream = new FileOutputStream(outputResource.getFile());
                      
            TransformerFactory tfact = TransformerFactory.newInstance();
            Transformer processor = tfact.newTransformer(new StreamSource(xsl));
            processor.transform(new StreamSource(xml), new StreamResult(fotWriter));

            xml = null;
            
			Logger nullLogger = new NullLogger();
			MessageHandler.setScreenLogger(nullLogger);
            MessageHandler.setOutputMethod(MessageHandler.NONE);

            fotWriter.close();
            
            Reader fotReader = new FileReader(fopResource.getFile());
            InputSource fotDS = new InputSource(fotReader);

            org.apache.fop.apps.Driver fopDriver = new org.apache.fop.apps.Driver(fotDS, pdfOutputStream);
			fopDriver.setLogger(avalonLogger);
			fopDriver.setRenderer(org.apache.fop.apps.Driver.RENDER_PDF);
            fopDriver.run();
                                  
            fopResource.getFile().delete();
                                    
        }  catch (IOException ioe) {
			log.fatal("IOException ", ioe);
			throw new ReportRenderException(ioe);
		} catch (TransformerConfigurationException tce) {
			log.fatal("ransformerConfigurationException ", tce);
			throw new ReportRenderException(tce);
		} catch (TransformerException te) {
			log.fatal("TransformerException ", te);
			throw new ReportRenderException(te);
		} catch (FOPException fope) {
			log.fatal("FOP Exception ", fope);
			throw new ReportRenderException(fope);
			
		}
 	}


	
	public void setxsltResource(Resource xsltResource) {
		this.xsltResource = xsltResource;
	}


	public void setFopResource(Resource fopResource) {
		this.fopResource = fopResource;
	}


	public void setInputResource(Resource inputResource) {
		this.inputResource = inputResource;
	}


	public void setOutputResource(Resource outputResource) {
		this.outputResource = outputResource;
	}

}
