package org.opennms.web.category;

import java.text.DecimalFormat;


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
    public static final DecimalFormat valueFormat = new DecimalFormat( ".000" );
    
    /** HTML color code for the color of green we use. */
    public static final String GREEN  = "green";
    
    /** HTML color code for the color of yello we use. */    
    public static final String YELLOW = "#ffff33";
    
    /** HTML color code for the color of red we use. */    
    public static final String RED    = "#ff3333";  

    
    /** Private, empty constructor so this class will not be instantiated. */
    private CategoryUtil() {}

    
    /** Format an RTC value the way we want it. */
    public static String formatValue(double value) {
        return valueFormat.format(value);
    }
    
    
    /**
     * Determine the color to use for a given category value and thresholds.
     */    
    public static String getCategoryColor(Category category) {
        if( category == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getCategoryColor(category.getNormalThreshold(), category.getWarningThreshold(), category.getValue());
    }    

    
    /**
     * Determine the color to use for a given value and the given category's thresholds.
     */    
    public static String getCategoryColor(Category category, double value) {
        if( category == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getCategoryColor(category.getNormalThreshold(), category.getWarningThreshold(), value);
    }    
    
    
    /**
     * Determine the color to use for a given value and thresholds.
     */    
    public static String getCategoryColor( double normal, double warning, double value ) {
        String color = RED;
        
        if( value >= normal ) {
           color = GREEN;
        } 
        else if( value >= warning ) {
            color = YELLOW;
        }

        return( color );
    }    
}
