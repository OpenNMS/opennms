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
package org.opennms.report.availability.store;

import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class SimpleReportStore implements ReportStore {
	
	private String baseDir;
	
	private String fileName;
	
	private File file;
	
	private static final String LOG4J_CATEGORY = "OpenNMS.Report";
	
	private Category log;
	
	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#newFile()
	 */
	
	public SimpleReportStore() {
		
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Using SimpleReport Store in ");
		
	}
	
	public File newFile() {
		
		if (log.isDebugEnabled())
			log.debug("Using FileName: " + baseDir + fileName);
		file = new File(baseDir, fileName);
		return file;
		
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#store()
	 */
	public void store() {
		// placeholder
		// SimpleReportStore does not put the file anywhere.
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#delete()
	 */
	public void delete() {
		file.delete();
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#getBaseDir()
	 */
	public String getBaseDir() {
		return baseDir;
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#setBaseDir(java.lang.String)
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#getFileName()
	 */
	public String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#setFileName(java.lang.String)
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#getStoreFile()
	 */
	public File getStoreFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.opennms.report.availability.store.ReportStore#setStoreFile(java.io.File)
	 */
	public void setStoreFile(File file) {
		this.file = file;
	}
	

}
