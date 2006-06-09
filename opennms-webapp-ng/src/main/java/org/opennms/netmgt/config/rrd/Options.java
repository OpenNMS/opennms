/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Options.java,v 1.2 2005/11/03 20:43:58 brozow Exp $
 */

package org.opennms.netmgt.config.rrd;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Options.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:58 $
 */
public class Options implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _anti_aliasing
     */
    private boolean _anti_aliasing;

    /**
     * keeps track of state for field: _anti_aliasing
     */
    private boolean _has_anti_aliasing;

    /**
     * Field _arrow_color
     */
    private java.lang.String _arrow_color;

    /**
     * Field _axis_color
     */
    private java.lang.String _axis_color;

    /**
     * Field _back_color
     */
    private java.lang.String _back_color;

    /**
     * Field _background
     */
    private java.lang.String _background;

    /**
     * Field _base_value
     */
    private int _base_value;

    /**
     * keeps track of state for field: _base_value
     */
    private boolean _has_base_value;

    /**
     * Field _canvas
     */
    private java.lang.String _canvas;

    /**
     * Field _left_padding
     */
    private int _left_padding;

    /**
     * keeps track of state for field: _left_padding
     */
    private boolean _has_left_padding;

    /**
     * Field _default_font
     */
    private org.opennms.netmgt.config.rrd.Default_font _default_font;

    /**
     * Field _default_font_color
     */
    private java.lang.String _default_font_color;

    /**
     * Field _frame_color
     */
    private java.lang.String _frame_color;

    /**
     * Field _front_grid
     */
    private boolean _front_grid;

    /**
     * keeps track of state for field: _front_grid
     */
    private boolean _has_front_grid;

    /**
     * Field _grid_range
     */
    private org.opennms.netmgt.config.rrd.Grid_range _grid_range;

    /**
     * Field _grid_x
     */
    private boolean _grid_x;

    /**
     * keeps track of state for field: _grid_x
     */
    private boolean _has_grid_x;

    /**
     * Field _grid_y
     */
    private boolean _grid_y;

    /**
     * keeps track of state for field: _grid_y
     */
    private boolean _has_grid_y;

    /**
     * Field _border
     */
    private org.opennms.netmgt.config.rrd.Border _border;

    /**
     * Field _major_grid_color
     */
    private java.lang.String _major_grid_color;

    /**
     * Field _major_grid_x
     */
    private boolean _major_grid_x;

    /**
     * keeps track of state for field: _major_grid_x
     */
    private boolean _has_major_grid_x;

    /**
     * Field _major_grid_y
     */
    private boolean _major_grid_y;

    /**
     * keeps track of state for field: _major_grid_y
     */
    private boolean _has_major_grid_y;

    /**
     * Field _minor_grid_color
     */
    private java.lang.String _minor_grid_color;

    /**
     * Field _minor_grid_x
     */
    private boolean _minor_grid_x;

    /**
     * keeps track of state for field: _minor_grid_x
     */
    private boolean _has_minor_grid_x;

    /**
     * Field _minor_grid_y
     */
    private boolean _minor_grid_y;

    /**
     * keeps track of state for field: _minor_grid_y
     */
    private boolean _has_minor_grid_y;

    /**
     * Field _overlay
     */
    private java.lang.String _overlay;

    /**
     * Field _show_legend
     */
    private boolean _show_legend;

    /**
     * keeps track of state for field: _show_legend
     */
    private boolean _has_show_legend;

    /**
     * Field _show_signature
     */
    private boolean _show_signature;

    /**
     * keeps track of state for field: _show_signature
     */
    private boolean _has_show_signature;

    /**
     * Field _time_axis
     */
    private org.opennms.netmgt.config.rrd.Time_axis _time_axis;

    /**
     * Field _time_axis_label
     */
    private java.lang.String _time_axis_label;

    /**
     * Field _title
     */
    private java.lang.String _title;

    /**
     * Field _title_font
     */
    private org.opennms.netmgt.config.rrd.Title_font _title_font;

    /**
     * Field _title_font_color
     */
    private java.lang.String _title_font_color;

    /**
     * Field _units_exponent
     */
    private int _units_exponent;

    /**
     * keeps track of state for field: _units_exponent
     */
    private boolean _has_units_exponent;

    /**
     * Field _value_axis
     */
    private org.opennms.netmgt.config.rrd.Value_axis _value_axis;

    /**
     * Field _vertical_label
     */
    private java.lang.String _vertical_label;


      //----------------/
     //- Constructors -/
    //----------------/

    public Options() 
     {
        super();
    } //-- org.opennms.netmgt.config.rrd.Options()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteAnti_aliasing
     * 
     */
    public void deleteAnti_aliasing()
    {
        this._has_anti_aliasing= false;
    } //-- void deleteAnti_aliasing() 

    /**
     * Method deleteBase_value
     * 
     */
    public void deleteBase_value()
    {
        this._has_base_value= false;
    } //-- void deleteBase_value() 

    /**
     * Method deleteFront_grid
     * 
     */
    public void deleteFront_grid()
    {
        this._has_front_grid= false;
    } //-- void deleteFront_grid() 

    /**
     * Method deleteGrid_x
     * 
     */
    public void deleteGrid_x()
    {
        this._has_grid_x= false;
    } //-- void deleteGrid_x() 

    /**
     * Method deleteGrid_y
     * 
     */
    public void deleteGrid_y()
    {
        this._has_grid_y= false;
    } //-- void deleteGrid_y() 

    /**
     * Method deleteLeft_padding
     * 
     */
    public void deleteLeft_padding()
    {
        this._has_left_padding= false;
    } //-- void deleteLeft_padding() 

    /**
     * Method deleteMajor_grid_x
     * 
     */
    public void deleteMajor_grid_x()
    {
        this._has_major_grid_x= false;
    } //-- void deleteMajor_grid_x() 

    /**
     * Method deleteMajor_grid_y
     * 
     */
    public void deleteMajor_grid_y()
    {
        this._has_major_grid_y= false;
    } //-- void deleteMajor_grid_y() 

    /**
     * Method deleteMinor_grid_x
     * 
     */
    public void deleteMinor_grid_x()
    {
        this._has_minor_grid_x= false;
    } //-- void deleteMinor_grid_x() 

    /**
     * Method deleteMinor_grid_y
     * 
     */
    public void deleteMinor_grid_y()
    {
        this._has_minor_grid_y= false;
    } //-- void deleteMinor_grid_y() 

    /**
     * Method deleteShow_legend
     * 
     */
    public void deleteShow_legend()
    {
        this._has_show_legend= false;
    } //-- void deleteShow_legend() 

    /**
     * Method deleteShow_signature
     * 
     */
    public void deleteShow_signature()
    {
        this._has_show_signature= false;
    } //-- void deleteShow_signature() 

    /**
     * Method deleteUnits_exponent
     * 
     */
    public void deleteUnits_exponent()
    {
        this._has_units_exponent= false;
    } //-- void deleteUnits_exponent() 

    /**
     * Returns the value of field 'anti_aliasing'.
     * 
     * @return boolean
     * @return the value of field 'anti_aliasing'.
     */
    public boolean getAnti_aliasing()
    {
        return this._anti_aliasing;
    } //-- boolean getAnti_aliasing() 

    /**
     * Returns the value of field 'arrow_color'.
     * 
     * @return String
     * @return the value of field 'arrow_color'.
     */
    public java.lang.String getArrow_color()
    {
        return this._arrow_color;
    } //-- java.lang.String getArrow_color() 

    /**
     * Returns the value of field 'axis_color'.
     * 
     * @return String
     * @return the value of field 'axis_color'.
     */
    public java.lang.String getAxis_color()
    {
        return this._axis_color;
    } //-- java.lang.String getAxis_color() 

    /**
     * Returns the value of field 'back_color'.
     * 
     * @return String
     * @return the value of field 'back_color'.
     */
    public java.lang.String getBack_color()
    {
        return this._back_color;
    } //-- java.lang.String getBack_color() 

    /**
     * Returns the value of field 'background'.
     * 
     * @return String
     * @return the value of field 'background'.
     */
    public java.lang.String getBackground()
    {
        return this._background;
    } //-- java.lang.String getBackground() 

    /**
     * Returns the value of field 'base_value'.
     * 
     * @return int
     * @return the value of field 'base_value'.
     */
    public int getBase_value()
    {
        return this._base_value;
    } //-- int getBase_value() 

    /**
     * Returns the value of field 'border'.
     * 
     * @return Border
     * @return the value of field 'border'.
     */
    public org.opennms.netmgt.config.rrd.Border getBorder()
    {
        return this._border;
    } //-- org.opennms.netmgt.config.rrd.Border getBorder() 

    /**
     * Returns the value of field 'canvas'.
     * 
     * @return String
     * @return the value of field 'canvas'.
     */
    public java.lang.String getCanvas()
    {
        return this._canvas;
    } //-- java.lang.String getCanvas() 

    /**
     * Returns the value of field 'default_font'.
     * 
     * @return Default_font
     * @return the value of field 'default_font'.
     */
    public org.opennms.netmgt.config.rrd.Default_font getDefault_font()
    {
        return this._default_font;
    } //-- org.opennms.netmgt.config.rrd.Default_font getDefault_font() 

    /**
     * Returns the value of field 'default_font_color'.
     * 
     * @return String
     * @return the value of field 'default_font_color'.
     */
    public java.lang.String getDefault_font_color()
    {
        return this._default_font_color;
    } //-- java.lang.String getDefault_font_color() 

    /**
     * Returns the value of field 'frame_color'.
     * 
     * @return String
     * @return the value of field 'frame_color'.
     */
    public java.lang.String getFrame_color()
    {
        return this._frame_color;
    } //-- java.lang.String getFrame_color() 

    /**
     * Returns the value of field 'front_grid'.
     * 
     * @return boolean
     * @return the value of field 'front_grid'.
     */
    public boolean getFront_grid()
    {
        return this._front_grid;
    } //-- boolean getFront_grid() 

    /**
     * Returns the value of field 'grid_range'.
     * 
     * @return Grid_range
     * @return the value of field 'grid_range'.
     */
    public org.opennms.netmgt.config.rrd.Grid_range getGrid_range()
    {
        return this._grid_range;
    } //-- org.opennms.netmgt.config.rrd.Grid_range getGrid_range() 

    /**
     * Returns the value of field 'grid_x'.
     * 
     * @return boolean
     * @return the value of field 'grid_x'.
     */
    public boolean getGrid_x()
    {
        return this._grid_x;
    } //-- boolean getGrid_x() 

    /**
     * Returns the value of field 'grid_y'.
     * 
     * @return boolean
     * @return the value of field 'grid_y'.
     */
    public boolean getGrid_y()
    {
        return this._grid_y;
    } //-- boolean getGrid_y() 

    /**
     * Returns the value of field 'left_padding'.
     * 
     * @return int
     * @return the value of field 'left_padding'.
     */
    public int getLeft_padding()
    {
        return this._left_padding;
    } //-- int getLeft_padding() 

    /**
     * Returns the value of field 'major_grid_color'.
     * 
     * @return String
     * @return the value of field 'major_grid_color'.
     */
    public java.lang.String getMajor_grid_color()
    {
        return this._major_grid_color;
    } //-- java.lang.String getMajor_grid_color() 

    /**
     * Returns the value of field 'major_grid_x'.
     * 
     * @return boolean
     * @return the value of field 'major_grid_x'.
     */
    public boolean getMajor_grid_x()
    {
        return this._major_grid_x;
    } //-- boolean getMajor_grid_x() 

    /**
     * Returns the value of field 'major_grid_y'.
     * 
     * @return boolean
     * @return the value of field 'major_grid_y'.
     */
    public boolean getMajor_grid_y()
    {
        return this._major_grid_y;
    } //-- boolean getMajor_grid_y() 

    /**
     * Returns the value of field 'minor_grid_color'.
     * 
     * @return String
     * @return the value of field 'minor_grid_color'.
     */
    public java.lang.String getMinor_grid_color()
    {
        return this._minor_grid_color;
    } //-- java.lang.String getMinor_grid_color() 

    /**
     * Returns the value of field 'minor_grid_x'.
     * 
     * @return boolean
     * @return the value of field 'minor_grid_x'.
     */
    public boolean getMinor_grid_x()
    {
        return this._minor_grid_x;
    } //-- boolean getMinor_grid_x() 

    /**
     * Returns the value of field 'minor_grid_y'.
     * 
     * @return boolean
     * @return the value of field 'minor_grid_y'.
     */
    public boolean getMinor_grid_y()
    {
        return this._minor_grid_y;
    } //-- boolean getMinor_grid_y() 

    /**
     * Returns the value of field 'overlay'.
     * 
     * @return String
     * @return the value of field 'overlay'.
     */
    public java.lang.String getOverlay()
    {
        return this._overlay;
    } //-- java.lang.String getOverlay() 

    /**
     * Returns the value of field 'show_legend'.
     * 
     * @return boolean
     * @return the value of field 'show_legend'.
     */
    public boolean getShow_legend()
    {
        return this._show_legend;
    } //-- boolean getShow_legend() 

    /**
     * Returns the value of field 'show_signature'.
     * 
     * @return boolean
     * @return the value of field 'show_signature'.
     */
    public boolean getShow_signature()
    {
        return this._show_signature;
    } //-- boolean getShow_signature() 

    /**
     * Returns the value of field 'time_axis'.
     * 
     * @return Time_axis
     * @return the value of field 'time_axis'.
     */
    public org.opennms.netmgt.config.rrd.Time_axis getTime_axis()
    {
        return this._time_axis;
    } //-- org.opennms.netmgt.config.rrd.Time_axis getTime_axis() 

    /**
     * Returns the value of field 'time_axis_label'.
     * 
     * @return String
     * @return the value of field 'time_axis_label'.
     */
    public java.lang.String getTime_axis_label()
    {
        return this._time_axis_label;
    } //-- java.lang.String getTime_axis_label() 

    /**
     * Returns the value of field 'title'.
     * 
     * @return String
     * @return the value of field 'title'.
     */
    public java.lang.String getTitle()
    {
        return this._title;
    } //-- java.lang.String getTitle() 

    /**
     * Returns the value of field 'title_font'.
     * 
     * @return Title_font
     * @return the value of field 'title_font'.
     */
    public org.opennms.netmgt.config.rrd.Title_font getTitle_font()
    {
        return this._title_font;
    } //-- org.opennms.netmgt.config.rrd.Title_font getTitle_font() 

    /**
     * Returns the value of field 'title_font_color'.
     * 
     * @return String
     * @return the value of field 'title_font_color'.
     */
    public java.lang.String getTitle_font_color()
    {
        return this._title_font_color;
    } //-- java.lang.String getTitle_font_color() 

    /**
     * Returns the value of field 'units_exponent'.
     * 
     * @return int
     * @return the value of field 'units_exponent'.
     */
    public int getUnits_exponent()
    {
        return this._units_exponent;
    } //-- int getUnits_exponent() 

    /**
     * Returns the value of field 'value_axis'.
     * 
     * @return Value_axis
     * @return the value of field 'value_axis'.
     */
    public org.opennms.netmgt.config.rrd.Value_axis getValue_axis()
    {
        return this._value_axis;
    } //-- org.opennms.netmgt.config.rrd.Value_axis getValue_axis() 

    /**
     * Returns the value of field 'vertical_label'.
     * 
     * @return String
     * @return the value of field 'vertical_label'.
     */
    public java.lang.String getVertical_label()
    {
        return this._vertical_label;
    } //-- java.lang.String getVertical_label() 

    /**
     * Method hasAnti_aliasing
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasAnti_aliasing()
    {
        return this._has_anti_aliasing;
    } //-- boolean hasAnti_aliasing() 

    /**
     * Method hasBase_value
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasBase_value()
    {
        return this._has_base_value;
    } //-- boolean hasBase_value() 

    /**
     * Method hasFront_grid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasFront_grid()
    {
        return this._has_front_grid;
    } //-- boolean hasFront_grid() 

    /**
     * Method hasGrid_x
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGrid_x()
    {
        return this._has_grid_x;
    } //-- boolean hasGrid_x() 

    /**
     * Method hasGrid_y
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGrid_y()
    {
        return this._has_grid_y;
    } //-- boolean hasGrid_y() 

    /**
     * Method hasLeft_padding
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasLeft_padding()
    {
        return this._has_left_padding;
    } //-- boolean hasLeft_padding() 

    /**
     * Method hasMajor_grid_x
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMajor_grid_x()
    {
        return this._has_major_grid_x;
    } //-- boolean hasMajor_grid_x() 

    /**
     * Method hasMajor_grid_y
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMajor_grid_y()
    {
        return this._has_major_grid_y;
    } //-- boolean hasMajor_grid_y() 

    /**
     * Method hasMinor_grid_x
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMinor_grid_x()
    {
        return this._has_minor_grid_x;
    } //-- boolean hasMinor_grid_x() 

    /**
     * Method hasMinor_grid_y
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMinor_grid_y()
    {
        return this._has_minor_grid_y;
    } //-- boolean hasMinor_grid_y() 

    /**
     * Method hasShow_legend
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShow_legend()
    {
        return this._has_show_legend;
    } //-- boolean hasShow_legend() 

    /**
     * Method hasShow_signature
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShow_signature()
    {
        return this._has_show_signature;
    } //-- boolean hasShow_signature() 

    /**
     * Method hasUnits_exponent
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasUnits_exponent()
    {
        return this._has_units_exponent;
    } //-- boolean hasUnits_exponent() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'anti_aliasing'.
     * 
     * @param anti_aliasing the value of field 'anti_aliasing'.
     */
    public void setAnti_aliasing(boolean anti_aliasing)
    {
        this._anti_aliasing = anti_aliasing;
        this._has_anti_aliasing = true;
    } //-- void setAnti_aliasing(boolean) 

    /**
     * Sets the value of field 'arrow_color'.
     * 
     * @param arrow_color the value of field 'arrow_color'.
     */
    public void setArrow_color(java.lang.String arrow_color)
    {
        this._arrow_color = arrow_color;
    } //-- void setArrow_color(java.lang.String) 

    /**
     * Sets the value of field 'axis_color'.
     * 
     * @param axis_color the value of field 'axis_color'.
     */
    public void setAxis_color(java.lang.String axis_color)
    {
        this._axis_color = axis_color;
    } //-- void setAxis_color(java.lang.String) 

    /**
     * Sets the value of field 'back_color'.
     * 
     * @param back_color the value of field 'back_color'.
     */
    public void setBack_color(java.lang.String back_color)
    {
        this._back_color = back_color;
    } //-- void setBack_color(java.lang.String) 

    /**
     * Sets the value of field 'background'.
     * 
     * @param background the value of field 'background'.
     */
    public void setBackground(java.lang.String background)
    {
        this._background = background;
    } //-- void setBackground(java.lang.String) 

    /**
     * Sets the value of field 'base_value'.
     * 
     * @param base_value the value of field 'base_value'.
     */
    public void setBase_value(int base_value)
    {
        this._base_value = base_value;
        this._has_base_value = true;
    } //-- void setBase_value(int) 

    /**
     * Sets the value of field 'border'.
     * 
     * @param border the value of field 'border'.
     */
    public void setBorder(org.opennms.netmgt.config.rrd.Border border)
    {
        this._border = border;
    } //-- void setBorder(org.opennms.netmgt.config.rrd.Border) 

    /**
     * Sets the value of field 'canvas'.
     * 
     * @param canvas the value of field 'canvas'.
     */
    public void setCanvas(java.lang.String canvas)
    {
        this._canvas = canvas;
    } //-- void setCanvas(java.lang.String) 

    /**
     * Sets the value of field 'default_font'.
     * 
     * @param default_font the value of field 'default_font'.
     */
    public void setDefault_font(org.opennms.netmgt.config.rrd.Default_font default_font)
    {
        this._default_font = default_font;
    } //-- void setDefault_font(org.opennms.netmgt.config.rrd.Default_font) 

    /**
     * Sets the value of field 'default_font_color'.
     * 
     * @param default_font_color the value of field
     * 'default_font_color'.
     */
    public void setDefault_font_color(java.lang.String default_font_color)
    {
        this._default_font_color = default_font_color;
    } //-- void setDefault_font_color(java.lang.String) 

    /**
     * Sets the value of field 'frame_color'.
     * 
     * @param frame_color the value of field 'frame_color'.
     */
    public void setFrame_color(java.lang.String frame_color)
    {
        this._frame_color = frame_color;
    } //-- void setFrame_color(java.lang.String) 

    /**
     * Sets the value of field 'front_grid'.
     * 
     * @param front_grid the value of field 'front_grid'.
     */
    public void setFront_grid(boolean front_grid)
    {
        this._front_grid = front_grid;
        this._has_front_grid = true;
    } //-- void setFront_grid(boolean) 

    /**
     * Sets the value of field 'grid_range'.
     * 
     * @param grid_range the value of field 'grid_range'.
     */
    public void setGrid_range(org.opennms.netmgt.config.rrd.Grid_range grid_range)
    {
        this._grid_range = grid_range;
    } //-- void setGrid_range(org.opennms.netmgt.config.rrd.Grid_range) 

    /**
     * Sets the value of field 'grid_x'.
     * 
     * @param grid_x the value of field 'grid_x'.
     */
    public void setGrid_x(boolean grid_x)
    {
        this._grid_x = grid_x;
        this._has_grid_x = true;
    } //-- void setGrid_x(boolean) 

    /**
     * Sets the value of field 'grid_y'.
     * 
     * @param grid_y the value of field 'grid_y'.
     */
    public void setGrid_y(boolean grid_y)
    {
        this._grid_y = grid_y;
        this._has_grid_y = true;
    } //-- void setGrid_y(boolean) 

    /**
     * Sets the value of field 'left_padding'.
     * 
     * @param left_padding the value of field 'left_padding'.
     */
    public void setLeft_padding(int left_padding)
    {
        this._left_padding = left_padding;
        this._has_left_padding = true;
    } //-- void setLeft_padding(int) 

    /**
     * Sets the value of field 'major_grid_color'.
     * 
     * @param major_grid_color the value of field 'major_grid_color'
     */
    public void setMajor_grid_color(java.lang.String major_grid_color)
    {
        this._major_grid_color = major_grid_color;
    } //-- void setMajor_grid_color(java.lang.String) 

    /**
     * Sets the value of field 'major_grid_x'.
     * 
     * @param major_grid_x the value of field 'major_grid_x'.
     */
    public void setMajor_grid_x(boolean major_grid_x)
    {
        this._major_grid_x = major_grid_x;
        this._has_major_grid_x = true;
    } //-- void setMajor_grid_x(boolean) 

    /**
     * Sets the value of field 'major_grid_y'.
     * 
     * @param major_grid_y the value of field 'major_grid_y'.
     */
    public void setMajor_grid_y(boolean major_grid_y)
    {
        this._major_grid_y = major_grid_y;
        this._has_major_grid_y = true;
    } //-- void setMajor_grid_y(boolean) 

    /**
     * Sets the value of field 'minor_grid_color'.
     * 
     * @param minor_grid_color the value of field 'minor_grid_color'
     */
    public void setMinor_grid_color(java.lang.String minor_grid_color)
    {
        this._minor_grid_color = minor_grid_color;
    } //-- void setMinor_grid_color(java.lang.String) 

    /**
     * Sets the value of field 'minor_grid_x'.
     * 
     * @param minor_grid_x the value of field 'minor_grid_x'.
     */
    public void setMinor_grid_x(boolean minor_grid_x)
    {
        this._minor_grid_x = minor_grid_x;
        this._has_minor_grid_x = true;
    } //-- void setMinor_grid_x(boolean) 

    /**
     * Sets the value of field 'minor_grid_y'.
     * 
     * @param minor_grid_y the value of field 'minor_grid_y'.
     */
    public void setMinor_grid_y(boolean minor_grid_y)
    {
        this._minor_grid_y = minor_grid_y;
        this._has_minor_grid_y = true;
    } //-- void setMinor_grid_y(boolean) 

    /**
     * Sets the value of field 'overlay'.
     * 
     * @param overlay the value of field 'overlay'.
     */
    public void setOverlay(java.lang.String overlay)
    {
        this._overlay = overlay;
    } //-- void setOverlay(java.lang.String) 

    /**
     * Sets the value of field 'show_legend'.
     * 
     * @param show_legend the value of field 'show_legend'.
     */
    public void setShow_legend(boolean show_legend)
    {
        this._show_legend = show_legend;
        this._has_show_legend = true;
    } //-- void setShow_legend(boolean) 

    /**
     * Sets the value of field 'show_signature'.
     * 
     * @param show_signature the value of field 'show_signature'.
     */
    public void setShow_signature(boolean show_signature)
    {
        this._show_signature = show_signature;
        this._has_show_signature = true;
    } //-- void setShow_signature(boolean) 

    /**
     * Sets the value of field 'time_axis'.
     * 
     * @param time_axis the value of field 'time_axis'.
     */
    public void setTime_axis(org.opennms.netmgt.config.rrd.Time_axis time_axis)
    {
        this._time_axis = time_axis;
    } //-- void setTime_axis(org.opennms.netmgt.config.rrd.Time_axis) 

    /**
     * Sets the value of field 'time_axis_label'.
     * 
     * @param time_axis_label the value of field 'time_axis_label'.
     */
    public void setTime_axis_label(java.lang.String time_axis_label)
    {
        this._time_axis_label = time_axis_label;
    } //-- void setTime_axis_label(java.lang.String) 

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(java.lang.String title)
    {
        this._title = title;
    } //-- void setTitle(java.lang.String) 

    /**
     * Sets the value of field 'title_font'.
     * 
     * @param title_font the value of field 'title_font'.
     */
    public void setTitle_font(org.opennms.netmgt.config.rrd.Title_font title_font)
    {
        this._title_font = title_font;
    } //-- void setTitle_font(org.opennms.netmgt.config.rrd.Title_font) 

    /**
     * Sets the value of field 'title_font_color'.
     * 
     * @param title_font_color the value of field 'title_font_color'
     */
    public void setTitle_font_color(java.lang.String title_font_color)
    {
        this._title_font_color = title_font_color;
    } //-- void setTitle_font_color(java.lang.String) 

    /**
     * Sets the value of field 'units_exponent'.
     * 
     * @param units_exponent the value of field 'units_exponent'.
     */
    public void setUnits_exponent(int units_exponent)
    {
        this._units_exponent = units_exponent;
        this._has_units_exponent = true;
    } //-- void setUnits_exponent(int) 

    /**
     * Sets the value of field 'value_axis'.
     * 
     * @param value_axis the value of field 'value_axis'.
     */
    public void setValue_axis(org.opennms.netmgt.config.rrd.Value_axis value_axis)
    {
        this._value_axis = value_axis;
    } //-- void setValue_axis(org.opennms.netmgt.config.rrd.Value_axis) 

    /**
     * Sets the value of field 'vertical_label'.
     * 
     * @param vertical_label the value of field 'vertical_label'.
     */
    public void setVertical_label(java.lang.String vertical_label)
    {
        this._vertical_label = vertical_label;
    } //-- void setVertical_label(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.opennms.netmgt.config.rrd.Options) Unmarshaller.unmarshal(org.opennms.netmgt.config.rrd.Options.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
