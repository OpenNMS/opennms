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

package org.opennms.web.category;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.webuiColors.CategoryColors;

/**
 * Provides look and feel utilities for the JSPs presenting category 
 * (real time console) information. 
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A> 
 */
public class CategoryUtil extends Object
{
    /** 
     * Specifies how the category values should look.
     * <p>Note this value is currently public, but consider this temporary.
     * To hide the implementation (so we can change it later), please call
     * {@link #formatValue formatValue} instead.</p>     
     */
    public static final DecimalFormat valueFormat = new DecimalFormat( "0.000" );
    
    /** HTML color code for the color of green we use. */
    // public static final String GREEN  = m_green;
    
    /** HTML color code for the color of yello we use. */    
    // public static final String YELLOW = m_yellow;
    
    /** HTML color code for the color of red we use. */    
    // public static final String RED    = m_red;  

    
    /** Private, empty constructor so this class will not be instantiated. */
    private CategoryUtil() {}

    
    /** Format an RTC value the way we want it. */
    public static String formatValue(double value) {
        return valueFormat.format(value);
    }
    
    
    /**
     * Determine the color to use for a given category value and thresholds.
     */    
    public static String getCategoryColor(Category category) 
            throws  IOException,
                    MarshalException,
                    ValidationException
	{
        if( category == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getCategoryColor(category.getNormalThreshold(), category.getWarningThreshold(), category.getValue());
    }    

    
    /**
     * Determine the color to use for a given value and the given category's thresholds.
     */    
    public static String getCategoryColor(Category category, double value) 
            throws  IOException,
                    MarshalException,
                    ValidationException
	{
        if( category == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getCategoryColor(category.getNormalThreshold(), category.getWarningThreshold(), value);
    }    
    
    
    /**
     * Determine the color to use for a given value and thresholds.
     */    
    public static String getCategoryColor( double normal, double warning, double value ) 
            throws  IOException,
                    MarshalException,
                    ValidationException
    {
        String m_green = null;
        String m_yellow = null;
        String m_red = null;
        CategoryColors m_colorsconfig = new CategoryColors();

    	File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.WEBUI_COLORS_FILE_NAME);
        InputStream cfgIn = new FileInputStream(cfgFile);

        m_colorsconfig = (CategoryColors) Unmarshaller.unmarshal(CategoryColors.class, new InputStreamReader(cfgIn));
        cfgIn.close();

	m_green = m_colorsconfig.getGreen();
	m_yellow = m_colorsconfig.getYellow();
	m_red = m_colorsconfig.getRed();

        String color = m_red;
        
        if( value >= normal ) {
           color = m_green;
        } 
        else if( value >= warning ) {
            color = m_yellow;
        }

        return( color );
    }    

}
