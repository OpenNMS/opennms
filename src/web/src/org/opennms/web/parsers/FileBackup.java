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

package org.opennms.web.parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;


/**This class makes a backup of an xml file. The backup should be 
   made before any new configuration xml is saved. The class will
   try to determine what directory to backup to based on properties
   in the bluebird property file. Failing this, a default will be 
   used that is the current directory where the call was made from.
   A default ".backup" extension will be placed on this file.
*/
public class FileBackup
{
	/**The property string to look for in bluebird property file for the backup directory
	*/
	public static final String BACKUP_DIR_PROP = "org.opennms.bluebird.file.backupDir";
	
	/**This method attempts to make a backup of an xml file. The first parameter
	   should be the full path to the original file at least relative to where the
	   method is running from, or the absolute path to the file. The second parameter
	   is the just the file name that will identify the backup. A default extension will
	   be added to this filename.
	   @param original a path and file name to the original file
	   @param fileName the name to give the backup
	   @throws XMLWriteException if the backup was not able to be made
	*/
	public static void makeBackup(String original, String fileName)
		throws IOException
	{
		String backupDir = null;
		String backupExtension = ".backup";
		
		//see if we can find where the user wants to save this bad boy
		if (System.getProperty(BACKUP_DIR_PROP) == null)
		{
			Properties props = PropertyLoader.load(PropertyLoader.BLUEBIRD_PROP, PropertyLoader.LOCAL_ONLY);
			backupDir = props.getProperty(BACKUP_DIR_PROP);
		}
		else
		{
			backupDir = System.getProperty(BACKUP_DIR_PROP);
		}
		
		//all else fails save to whatever directory we are running from
		if (backupDir == null || backupDir.trim().equals(""))
		{
			backupDir = ".";
		}
		
		backupDir += System.getProperty("file.separator");
		
		//check to make sure the backup directory exists
		File backupFileDir = new File(backupDir);
		if(!backupFileDir.exists())
		{
			backupFileDir.mkdirs();
		}
		
		//lets try to backup the file
		try
		{
			//I doubt that this is the best way to make a backup of a file, but...
			
			//open up the original file
			BufferedReader originalBuffer = new BufferedReader(new FileReader(original));
			
			//open the backup file
			BufferedWriter backupBuffer = new BufferedWriter(new FileWriter(backupDir + fileName + backupExtension));
			
			//read each line from original, write it to backup
			String input;
			while((input = originalBuffer.readLine()) != null)
			{
				backupBuffer.write(input);
				backupBuffer.newLine();
			}
			
			//close everything off
			originalBuffer.close();
			backupBuffer.flush();
			backupBuffer.close();
		}
		catch(Exception e)
		{
			throw new IOException("Error backing up " + fileName + " to " + backupDir + "\n" + e.getMessage());
		}
	}
}
