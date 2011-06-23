/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.eventconf;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Event.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Event implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The event mask which helps to uniquely identify an
     *  event
     */
    private org.opennms.netmgt.xml.eventconf.Mask _mask;

    /**
     * The Universal Event Identifier
     */
    private java.lang.String _uei;

    /**
     * A human readable name used to identify an event in
     *  the web ui
     */
    private java.lang.String _eventLabel;

    /**
     * The snmp information from the trap
     */
    private org.opennms.netmgt.xml.eventconf.Snmp _snmp;

    /**
     * The event description
     */
    private java.lang.String _descr;

    /**
     * The event logmsg
     */
    private org.opennms.netmgt.xml.eventconf.Logmsg _logmsg;

    /**
     * The event severity
     */
    private java.lang.String _severity;

    /**
     * The event correlation information
     */
    private org.opennms.netmgt.xml.eventconf.Correlation _correlation;

    /**
     * The operator instruction for this
     *  event
     */
    private java.lang.String _operinstruct;

    /**
     * The automatic action to occur when this event
     *  occurs
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Autoaction> _autoactionList;

    /**
     * The varbind decoding tag used to decode value 
     *  into a string
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Varbindsdecode> _varbindsdecodeList;

    /**
     * The operator action to be taken when this event
     *  occurs
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Operaction> _operactionList;

    /**
     * The autoacknowledge information for the
     *  user
     */
    private org.opennms.netmgt.xml.eventconf.Autoacknowledge _autoacknowledge;

    /**
     * A logical group with which to associate this
     *  event
     */
    private java.util.List<java.lang.String> _loggroupList;

    /**
     * The trouble ticket info
     */
    private org.opennms.netmgt.xml.eventconf.Tticket _tticket;

    /**
     * The forwarding information for this
     *  event
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Forward> _forwardList;

    /**
     * The script information for this
     *  event
     */
    private java.util.List<org.opennms.netmgt.xml.eventconf.Script> _scriptList;

    /**
     * The text to be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     */
    private java.lang.String _mouseovertext;

    /**
     * Data used to create an event.
     */
    private org.opennms.netmgt.xml.eventconf.AlarmData _alarmData;


      //----------------/
     //- Constructors -/
    //----------------/

    public Event() {
        super();
        this._autoactionList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Autoaction>();
        this._varbindsdecodeList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Varbindsdecode>();
        this._operactionList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Operaction>();
        this._loggroupList = new java.util.ArrayList<java.lang.String>();
        this._forwardList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Forward>();
        this._scriptList = new java.util.ArrayList<org.opennms.netmgt.xml.eventconf.Script>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vAutoaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAutoaction(
            final org.opennms.netmgt.xml.eventconf.Autoaction vAutoaction)
    throws java.lang.IndexOutOfBoundsException {
        this._autoactionList.add(vAutoaction);
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAutoaction(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Autoaction vAutoaction)
    throws java.lang.IndexOutOfBoundsException {
        this._autoactionList.add(index, vAutoaction);
    }

    /**
     * 
     * 
     * @param vForward
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addForward(
            final org.opennms.netmgt.xml.eventconf.Forward vForward)
    throws java.lang.IndexOutOfBoundsException {
        this._forwardList.add(vForward);
    }

    /**
     * 
     * 
     * @param index
     * @param vForward
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addForward(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Forward vForward)
    throws java.lang.IndexOutOfBoundsException {
        this._forwardList.add(index, vForward);
    }

    /**
     * 
     * 
     * @param vLoggroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLoggroup(
            final java.lang.String vLoggroup)
    throws java.lang.IndexOutOfBoundsException {
        this._loggroupList.add(vLoggroup);
    }

    /**
     * 
     * 
     * @param index
     * @param vLoggroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLoggroup(
            final int index,
            final java.lang.String vLoggroup)
    throws java.lang.IndexOutOfBoundsException {
        this._loggroupList.add(index, vLoggroup);
    }

    /**
     * 
     * 
     * @param vOperaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOperaction(
            final org.opennms.netmgt.xml.eventconf.Operaction vOperaction)
    throws java.lang.IndexOutOfBoundsException {
        this._operactionList.add(vOperaction);
    }

    /**
     * 
     * 
     * @param index
     * @param vOperaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOperaction(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Operaction vOperaction)
    throws java.lang.IndexOutOfBoundsException {
        this._operactionList.add(index, vOperaction);
    }

    /**
     * 
     * 
     * @param vScript
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addScript(
            final org.opennms.netmgt.xml.eventconf.Script vScript)
    throws java.lang.IndexOutOfBoundsException {
        this._scriptList.add(vScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vScript
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addScript(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Script vScript)
    throws java.lang.IndexOutOfBoundsException {
        this._scriptList.add(index, vScript);
    }

    /**
     * 
     * 
     * @param vVarbindsdecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbindsdecode(
            final org.opennms.netmgt.xml.eventconf.Varbindsdecode vVarbindsdecode)
    throws java.lang.IndexOutOfBoundsException {
        this._varbindsdecodeList.add(vVarbindsdecode);
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbindsdecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbindsdecode(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Varbindsdecode vVarbindsdecode)
    throws java.lang.IndexOutOfBoundsException {
        this._varbindsdecodeList.add(index, vVarbindsdecode);
    }

    /**
     * Method enumerateAutoaction.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Autoaction> enumerateAutoaction(
    ) {
        return java.util.Collections.enumeration(this._autoactionList);
    }

    /**
     * Method enumerateForward.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Forward> enumerateForward(
    ) {
        return java.util.Collections.enumeration(this._forwardList);
    }

    /**
     * Method enumerateLoggroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<java.lang.String> enumerateLoggroup(
    ) {
        return java.util.Collections.enumeration(this._loggroupList);
    }

    /**
     * Method enumerateOperaction.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Operaction> enumerateOperaction(
    ) {
        return java.util.Collections.enumeration(this._operactionList);
    }

    /**
     * Method enumerateScript.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Script> enumerateScript(
    ) {
        return java.util.Collections.enumeration(this._scriptList);
    }

    /**
     * Method enumerateVarbindsdecode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<org.opennms.netmgt.xml.eventconf.Varbindsdecode> enumerateVarbindsdecode(
    ) {
        return java.util.Collections.enumeration(this._varbindsdecodeList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Event) {
        
            Event temp = (Event)obj;
            if (this._mask != null) {
                if (temp._mask == null) return false;
                else if (!(this._mask.equals(temp._mask))) 
                    return false;
            }
            else if (temp._mask != null)
                return false;
            if (this._uei != null) {
                if (temp._uei == null) return false;
                else if (!(this._uei.equals(temp._uei))) 
                    return false;
            }
            else if (temp._uei != null)
                return false;
            if (this._eventLabel != null) {
                if (temp._eventLabel == null) return false;
                else if (!(this._eventLabel.equals(temp._eventLabel))) 
                    return false;
            }
            else if (temp._eventLabel != null)
                return false;
            if (this._snmp != null) {
                if (temp._snmp == null) return false;
                else if (!(this._snmp.equals(temp._snmp))) 
                    return false;
            }
            else if (temp._snmp != null)
                return false;
            if (this._descr != null) {
                if (temp._descr == null) return false;
                else if (!(this._descr.equals(temp._descr))) 
                    return false;
            }
            else if (temp._descr != null)
                return false;
            if (this._logmsg != null) {
                if (temp._logmsg == null) return false;
                else if (!(this._logmsg.equals(temp._logmsg))) 
                    return false;
            }
            else if (temp._logmsg != null)
                return false;
            if (this._severity != null) {
                if (temp._severity == null) return false;
                else if (!(this._severity.equals(temp._severity))) 
                    return false;
            }
            else if (temp._severity != null)
                return false;
            if (this._correlation != null) {
                if (temp._correlation == null) return false;
                else if (!(this._correlation.equals(temp._correlation))) 
                    return false;
            }
            else if (temp._correlation != null)
                return false;
            if (this._operinstruct != null) {
                if (temp._operinstruct == null) return false;
                else if (!(this._operinstruct.equals(temp._operinstruct))) 
                    return false;
            }
            else if (temp._operinstruct != null)
                return false;
            if (this._autoactionList != null) {
                if (temp._autoactionList == null) return false;
                else if (!(this._autoactionList.equals(temp._autoactionList))) 
                    return false;
            }
            else if (temp._autoactionList != null)
                return false;
            if (this._varbindsdecodeList != null) {
                if (temp._varbindsdecodeList == null) return false;
                else if (!(this._varbindsdecodeList.equals(temp._varbindsdecodeList))) 
                    return false;
            }
            else if (temp._varbindsdecodeList != null)
                return false;
            if (this._operactionList != null) {
                if (temp._operactionList == null) return false;
                else if (!(this._operactionList.equals(temp._operactionList))) 
                    return false;
            }
            else if (temp._operactionList != null)
                return false;
            if (this._autoacknowledge != null) {
                if (temp._autoacknowledge == null) return false;
                else if (!(this._autoacknowledge.equals(temp._autoacknowledge))) 
                    return false;
            }
            else if (temp._autoacknowledge != null)
                return false;
            if (this._loggroupList != null) {
                if (temp._loggroupList == null) return false;
                else if (!(this._loggroupList.equals(temp._loggroupList))) 
                    return false;
            }
            else if (temp._loggroupList != null)
                return false;
            if (this._tticket != null) {
                if (temp._tticket == null) return false;
                else if (!(this._tticket.equals(temp._tticket))) 
                    return false;
            }
            else if (temp._tticket != null)
                return false;
            if (this._forwardList != null) {
                if (temp._forwardList == null) return false;
                else if (!(this._forwardList.equals(temp._forwardList))) 
                    return false;
            }
            else if (temp._forwardList != null)
                return false;
            if (this._scriptList != null) {
                if (temp._scriptList == null) return false;
                else if (!(this._scriptList.equals(temp._scriptList))) 
                    return false;
            }
            else if (temp._scriptList != null)
                return false;
            if (this._mouseovertext != null) {
                if (temp._mouseovertext == null) return false;
                else if (!(this._mouseovertext.equals(temp._mouseovertext))) 
                    return false;
            }
            else if (temp._mouseovertext != null)
                return false;
            if (this._alarmData != null) {
                if (temp._alarmData == null) return false;
                else if (!(this._alarmData.equals(temp._alarmData))) 
                    return false;
            }
            else if (temp._alarmData != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'alarmData'. The field
     * 'alarmData' has the following description: Data used to
     * create an event.
     * 
     * @return the value of field 'AlarmData'.
     */
    public org.opennms.netmgt.xml.eventconf.AlarmData getAlarmData(
    ) {
        return this._alarmData;
    }

    /**
     * Returns the value of field 'autoacknowledge'. The field
     * 'autoacknowledge' has the following description: The
     * autoacknowledge information for the
     *  user
     * 
     * @return the value of field 'Autoacknowledge'.
     */
    public org.opennms.netmgt.xml.eventconf.Autoacknowledge getAutoacknowledge(
    ) {
        return this._autoacknowledge;
    }

    /**
     * Method getAutoaction.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Autoaction at the given inde
     */
    public org.opennms.netmgt.xml.eventconf.Autoaction getAutoaction(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._autoactionList.size()) {
            throw new IndexOutOfBoundsException("getAutoaction: Index value '" + index + "' not in range [0.." + (this._autoactionList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Autoaction) _autoactionList.get(index);
    }

    /**
     * Method getAutoaction.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Autoaction[] getAutoaction(
    ) {
        org.opennms.netmgt.xml.eventconf.Autoaction[] array = new org.opennms.netmgt.xml.eventconf.Autoaction[0];
        return (org.opennms.netmgt.xml.eventconf.Autoaction[]) this._autoactionList.toArray(array);
    }

    /**
     * Method getAutoactionCollection.Returns a reference to
     * '_autoactionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Autoaction> getAutoactionCollection(
    ) {
        return this._autoactionList;
    }

    /**
     * Method getAutoactionCount.
     * 
     * @return the size of this collection
     */
    public int getAutoactionCount(
    ) {
        return this._autoactionList.size();
    }

    /**
     * Returns the value of field 'correlation'. The field
     * 'correlation' has the following description: The event
     * correlation information
     * 
     * @return the value of field 'Correlation'.
     */
    public org.opennms.netmgt.xml.eventconf.Correlation getCorrelation(
    ) {
        return this._correlation;
    }

    /**
     * Returns the value of field 'descr'. The field 'descr' has
     * the following description: The event description
     * 
     * @return the value of field 'Descr'.
     */
    public java.lang.String getDescr(
    ) {
        return this._descr;
    }

    /**
     * Returns the value of field 'eventLabel'. The field
     * 'eventLabel' has the following description: A human readable
     * name used to identify an event in
     *  the web ui
     * 
     * @return the value of field 'EventLabel'.
     */
    public java.lang.String getEventLabel(
    ) {
        return this._eventLabel;
    }

    /**
     * Method getForward.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Forward at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Forward getForward(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._forwardList.size()) {
            throw new IndexOutOfBoundsException("getForward: Index value '" + index + "' not in range [0.." + (this._forwardList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Forward) _forwardList.get(index);
    }

    /**
     * Method getForward.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Forward[] getForward(
    ) {
        org.opennms.netmgt.xml.eventconf.Forward[] array = new org.opennms.netmgt.xml.eventconf.Forward[0];
        return (org.opennms.netmgt.xml.eventconf.Forward[]) this._forwardList.toArray(array);
    }

    /**
     * Method getForwardCollection.Returns a reference to
     * '_forwardList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Forward> getForwardCollection(
    ) {
        return this._forwardList;
    }

    /**
     * Method getForwardCount.
     * 
     * @return the size of this collection
     */
    public int getForwardCount(
    ) {
        return this._forwardList.size();
    }

    /**
     * Method getLoggroup.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the java.lang.String at the given index
     */
    public java.lang.String getLoggroup(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._loggroupList.size()) {
            throw new IndexOutOfBoundsException("getLoggroup: Index value '" + index + "' not in range [0.." + (this._loggroupList.size() - 1) + "]");
        }
        
        return (java.lang.String) _loggroupList.get(index);
    }

    /**
     * Method getLoggroup.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public java.lang.String[] getLoggroup(
    ) {
        java.lang.String[] array = new java.lang.String[0];
        return (java.lang.String[]) this._loggroupList.toArray(array);
    }

    /**
     * Method getLoggroupCollection.Returns a reference to
     * '_loggroupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<java.lang.String> getLoggroupCollection(
    ) {
        return this._loggroupList;
    }

    /**
     * Method getLoggroupCount.
     * 
     * @return the size of this collection
     */
    public int getLoggroupCount(
    ) {
        return this._loggroupList.size();
    }

    /**
     * Returns the value of field 'logmsg'. The field 'logmsg' has
     * the following description: The event logmsg
     * 
     * @return the value of field 'Logmsg'.
     */
    public org.opennms.netmgt.xml.eventconf.Logmsg getLogmsg(
    ) {
        return this._logmsg;
    }

    /**
     * Returns the value of field 'mask'. The field 'mask' has the
     * following description: The event mask which helps to
     * uniquely identify an
     *  event
     * 
     * @return the value of field 'Mask'.
     */
    public org.opennms.netmgt.xml.eventconf.Mask getMask(
    ) {
        return this._mask;
    }

    /**
     * Returns the value of field 'mouseovertext'. The field
     * 'mouseovertext' has the following description: The text to
     * be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     * 
     * @return the value of field 'Mouseovertext'.
     */
    public java.lang.String getMouseovertext(
    ) {
        return this._mouseovertext;
    }

    /**
     * Method getOperaction.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Operaction at the given inde
     */
    public org.opennms.netmgt.xml.eventconf.Operaction getOperaction(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._operactionList.size()) {
            throw new IndexOutOfBoundsException("getOperaction: Index value '" + index + "' not in range [0.." + (this._operactionList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Operaction) _operactionList.get(index);
    }

    /**
     * Method getOperaction.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Operaction[] getOperaction(
    ) {
        org.opennms.netmgt.xml.eventconf.Operaction[] array = new org.opennms.netmgt.xml.eventconf.Operaction[0];
        return (org.opennms.netmgt.xml.eventconf.Operaction[]) this._operactionList.toArray(array);
    }

    /**
     * Method getOperactionCollection.Returns a reference to
     * '_operactionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Operaction> getOperactionCollection(
    ) {
        return this._operactionList;
    }

    /**
     * Method getOperactionCount.
     * 
     * @return the size of this collection
     */
    public int getOperactionCount(
    ) {
        return this._operactionList.size();
    }

    /**
     * Returns the value of field 'operinstruct'. The field
     * 'operinstruct' has the following description: The operator
     * instruction for this
     *  event
     * 
     * @return the value of field 'Operinstruct'.
     */
    public java.lang.String getOperinstruct(
    ) {
        return this._operinstruct;
    }

    /**
     * Method getScript.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Script at the given index
     */
    public org.opennms.netmgt.xml.eventconf.Script getScript(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._scriptList.size()) {
            throw new IndexOutOfBoundsException("getScript: Index value '" + index + "' not in range [0.." + (this._scriptList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Script) _scriptList.get(index);
    }

    /**
     * Method getScript.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Script[] getScript(
    ) {
        org.opennms.netmgt.xml.eventconf.Script[] array = new org.opennms.netmgt.xml.eventconf.Script[0];
        return (org.opennms.netmgt.xml.eventconf.Script[]) this._scriptList.toArray(array);
    }

    /**
     * Method getScriptCollection.Returns a reference to
     * '_scriptList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Script> getScriptCollection(
    ) {
        return this._scriptList;
    }

    /**
     * Method getScriptCount.
     * 
     * @return the size of this collection
     */
    public int getScriptCount(
    ) {
        return this._scriptList.size();
    }

    /**
     * Returns the value of field 'severity'. The field 'severity'
     * has the following description: The event severity
     * 
     * @return the value of field 'Severity'.
     */
    public java.lang.String getSeverity(
    ) {
        return this._severity;
    }

    /**
     * Returns the value of field 'snmp'. The field 'snmp' has the
     * following description: The snmp information from the trap
     * 
     * @return the value of field 'Snmp'.
     */
    public org.opennms.netmgt.xml.eventconf.Snmp getSnmp(
    ) {
        return this._snmp;
    }

    /**
     * Returns the value of field 'tticket'. The field 'tticket'
     * has the following description: The trouble ticket info
     * 
     * @return the value of field 'Tticket'.
     */
    public org.opennms.netmgt.xml.eventconf.Tticket getTticket(
    ) {
        return this._tticket;
    }

    /**
     * Returns the value of field 'uei'. The field 'uei' has the
     * following description: The Universal Event Identifier
     * 
     * @return the value of field 'Uei'.
     */
    public java.lang.String getUei(
    ) {
        return this._uei;
    }

    /**
     * Method getVarbindsdecode.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.xml.eventconf.Varbindsdecode at the given
     * index
     */
    public org.opennms.netmgt.xml.eventconf.Varbindsdecode getVarbindsdecode(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._varbindsdecodeList.size()) {
            throw new IndexOutOfBoundsException("getVarbindsdecode: Index value '" + index + "' not in range [0.." + (this._varbindsdecodeList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.xml.eventconf.Varbindsdecode) _varbindsdecodeList.get(index);
    }

    /**
     * Method getVarbindsdecode.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.xml.eventconf.Varbindsdecode[] getVarbindsdecode(
    ) {
        org.opennms.netmgt.xml.eventconf.Varbindsdecode[] array = new org.opennms.netmgt.xml.eventconf.Varbindsdecode[0];
        return (org.opennms.netmgt.xml.eventconf.Varbindsdecode[]) this._varbindsdecodeList.toArray(array);
    }

    /**
     * Method getVarbindsdecodeCollection.Returns a reference to
     * '_varbindsdecodeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.xml.eventconf.Varbindsdecode> getVarbindsdecodeCollection(
    ) {
        return this._varbindsdecodeList;
    }

    /**
     * Method getVarbindsdecodeCount.
     * 
     * @return the size of this collection
     */
    public int getVarbindsdecodeCount(
    ) {
        return this._varbindsdecodeList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        long tmp;
        if (_mask != null) {
           result = 37 * result + _mask.hashCode();
        }
        if (_uei != null) {
           result = 37 * result + _uei.hashCode();
        }
        if (_eventLabel != null) {
           result = 37 * result + _eventLabel.hashCode();
        }
        if (_snmp != null) {
           result = 37 * result + _snmp.hashCode();
        }
        if (_descr != null) {
           result = 37 * result + _descr.hashCode();
        }
        if (_logmsg != null) {
           result = 37 * result + _logmsg.hashCode();
        }
        if (_severity != null) {
           result = 37 * result + _severity.hashCode();
        }
        if (_correlation != null) {
           result = 37 * result + _correlation.hashCode();
        }
        if (_operinstruct != null) {
           result = 37 * result + _operinstruct.hashCode();
        }
        if (_autoactionList != null) {
           result = 37 * result + _autoactionList.hashCode();
        }
        if (_varbindsdecodeList != null) {
           result = 37 * result + _varbindsdecodeList.hashCode();
        }
        if (_operactionList != null) {
           result = 37 * result + _operactionList.hashCode();
        }
        if (_autoacknowledge != null) {
           result = 37 * result + _autoacknowledge.hashCode();
        }
        if (_loggroupList != null) {
           result = 37 * result + _loggroupList.hashCode();
        }
        if (_tticket != null) {
           result = 37 * result + _tticket.hashCode();
        }
        if (_forwardList != null) {
           result = 37 * result + _forwardList.hashCode();
        }
        if (_scriptList != null) {
           result = 37 * result + _scriptList.hashCode();
        }
        if (_mouseovertext != null) {
           result = 37 * result + _mouseovertext.hashCode();
        }
        if (_alarmData != null) {
           result = 37 * result + _alarmData.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateAutoaction.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Autoaction> iterateAutoaction(
    ) {
        return this._autoactionList.iterator();
    }

    /**
     * Method iterateForward.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Forward> iterateForward(
    ) {
        return this._forwardList.iterator();
    }

    /**
     * Method iterateLoggroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<java.lang.String> iterateLoggroup(
    ) {
        return this._loggroupList.iterator();
    }

    /**
     * Method iterateOperaction.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Operaction> iterateOperaction(
    ) {
        return this._operactionList.iterator();
    }

    /**
     * Method iterateScript.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Script> iterateScript(
    ) {
        return this._scriptList.iterator();
    }

    /**
     * Method iterateVarbindsdecode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<org.opennms.netmgt.xml.eventconf.Varbindsdecode> iterateVarbindsdecode(
    ) {
        return this._varbindsdecodeList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllAutoaction(
    ) {
        this._autoactionList.clear();
    }

    /**
     */
    public void removeAllForward(
    ) {
        this._forwardList.clear();
    }

    /**
     */
    public void removeAllLoggroup(
    ) {
        this._loggroupList.clear();
    }

    /**
     */
    public void removeAllOperaction(
    ) {
        this._operactionList.clear();
    }

    /**
     */
    public void removeAllScript(
    ) {
        this._scriptList.clear();
    }

    /**
     */
    public void removeAllVarbindsdecode(
    ) {
        this._varbindsdecodeList.clear();
    }

    /**
     * Method removeAutoaction.
     * 
     * @param vAutoaction
     * @return true if the object was removed from the collection.
     */
    public boolean removeAutoaction(
            final org.opennms.netmgt.xml.eventconf.Autoaction vAutoaction) {
        boolean removed = _autoactionList.remove(vAutoaction);
        return removed;
    }

    /**
     * Method removeAutoactionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Autoaction removeAutoactionAt(
            final int index) {
        java.lang.Object obj = this._autoactionList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Autoaction) obj;
    }

    /**
     * Method removeForward.
     * 
     * @param vForward
     * @return true if the object was removed from the collection.
     */
    public boolean removeForward(
            final org.opennms.netmgt.xml.eventconf.Forward vForward) {
        boolean removed = _forwardList.remove(vForward);
        return removed;
    }

    /**
     * Method removeForwardAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Forward removeForwardAt(
            final int index) {
        java.lang.Object obj = this._forwardList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Forward) obj;
    }

    /**
     * Method removeLoggroup.
     * 
     * @param vLoggroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeLoggroup(
            final java.lang.String vLoggroup) {
        boolean removed = _loggroupList.remove(vLoggroup);
        return removed;
    }

    /**
     * Method removeLoggroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public java.lang.String removeLoggroupAt(
            final int index) {
        java.lang.Object obj = this._loggroupList.remove(index);
        return (java.lang.String) obj;
    }

    /**
     * Method removeOperaction.
     * 
     * @param vOperaction
     * @return true if the object was removed from the collection.
     */
    public boolean removeOperaction(
            final org.opennms.netmgt.xml.eventconf.Operaction vOperaction) {
        boolean removed = _operactionList.remove(vOperaction);
        return removed;
    }

    /**
     * Method removeOperactionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Operaction removeOperactionAt(
            final int index) {
        java.lang.Object obj = this._operactionList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Operaction) obj;
    }

    /**
     * Method removeScript.
     * 
     * @param vScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeScript(
            final org.opennms.netmgt.xml.eventconf.Script vScript) {
        boolean removed = _scriptList.remove(vScript);
        return removed;
    }

    /**
     * Method removeScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Script removeScriptAt(
            final int index) {
        java.lang.Object obj = this._scriptList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Script) obj;
    }

    /**
     * Method removeVarbindsdecode.
     * 
     * @param vVarbindsdecode
     * @return true if the object was removed from the collection.
     */
    public boolean removeVarbindsdecode(
            final org.opennms.netmgt.xml.eventconf.Varbindsdecode vVarbindsdecode) {
        boolean removed = _varbindsdecodeList.remove(vVarbindsdecode);
        return removed;
    }

    /**
     * Method removeVarbindsdecodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.xml.eventconf.Varbindsdecode removeVarbindsdecodeAt(
            final int index) {
        java.lang.Object obj = this._varbindsdecodeList.remove(index);
        return (org.opennms.netmgt.xml.eventconf.Varbindsdecode) obj;
    }

    /**
     * Sets the value of field 'alarmData'. The field 'alarmData'
     * has the following description: Data used to create an event.
     * 
     * @param alarmData the value of field 'alarmData'.
     */
    public void setAlarmData(
            final org.opennms.netmgt.xml.eventconf.AlarmData alarmData) {
        this._alarmData = alarmData;
    }

    /**
     * Sets the value of field 'autoacknowledge'. The field
     * 'autoacknowledge' has the following description: The
     * autoacknowledge information for the
     *  user
     * 
     * @param autoacknowledge the value of field 'autoacknowledge'.
     */
    public void setAutoacknowledge(
            final org.opennms.netmgt.xml.eventconf.Autoacknowledge autoacknowledge) {
        this._autoacknowledge = autoacknowledge;
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setAutoaction(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Autoaction vAutoaction)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._autoactionList.size()) {
            throw new IndexOutOfBoundsException("setAutoaction: Index value '" + index + "' not in range [0.." + (this._autoactionList.size() - 1) + "]");
        }
        
        this._autoactionList.set(index, vAutoaction);
    }

    /**
     * 
     * 
     * @param vAutoactionArray
     */
    public void setAutoaction(
            final org.opennms.netmgt.xml.eventconf.Autoaction[] vAutoactionArray) {
        //-- copy array
        _autoactionList.clear();
        
        for (int i = 0; i < vAutoactionArray.length; i++) {
                this._autoactionList.add(vAutoactionArray[i]);
        }
    }

    /**
     * Sets the value of '_autoactionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vAutoactionList the Vector to copy.
     */
    public void setAutoaction(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Autoaction> vAutoactionList) {
        // copy vector
        this._autoactionList.clear();
        
        this._autoactionList.addAll(vAutoactionList);
    }

    /**
     * Sets the value of '_autoactionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param autoactionList the Vector to set.
     */
    public void setAutoactionCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Autoaction> autoactionList) {
        this._autoactionList = autoactionList;
    }

    /**
     * Sets the value of field 'correlation'. The field
     * 'correlation' has the following description: The event
     * correlation information
     * 
     * @param correlation the value of field 'correlation'.
     */
    public void setCorrelation(
            final org.opennms.netmgt.xml.eventconf.Correlation correlation) {
        this._correlation = correlation;
    }

    /**
     * Sets the value of field 'descr'. The field 'descr' has the
     * following description: The event description
     * 
     * @param descr the value of field 'descr'.
     */
    public void setDescr(
            final java.lang.String descr) {
        this._descr = descr;
    }

    /**
     * Sets the value of field 'eventLabel'. The field 'eventLabel'
     * has the following description: A human readable name used to
     * identify an event in
     *  the web ui
     * 
     * @param eventLabel the value of field 'eventLabel'.
     */
    public void setEventLabel(
            final java.lang.String eventLabel) {
        this._eventLabel = eventLabel;
    }

    /**
     * 
     * 
     * @param index
     * @param vForward
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setForward(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Forward vForward)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._forwardList.size()) {
            throw new IndexOutOfBoundsException("setForward: Index value '" + index + "' not in range [0.." + (this._forwardList.size() - 1) + "]");
        }
        
        this._forwardList.set(index, vForward);
    }

    /**
     * 
     * 
     * @param vForwardArray
     */
    public void setForward(
            final org.opennms.netmgt.xml.eventconf.Forward[] vForwardArray) {
        //-- copy array
        _forwardList.clear();
        
        for (int i = 0; i < vForwardArray.length; i++) {
                this._forwardList.add(vForwardArray[i]);
        }
    }

    /**
     * Sets the value of '_forwardList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vForwardList the Vector to copy.
     */
    public void setForward(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Forward> vForwardList) {
        // copy vector
        this._forwardList.clear();
        
        this._forwardList.addAll(vForwardList);
    }

    /**
     * Sets the value of '_forwardList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param forwardList the Vector to set.
     */
    public void setForwardCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Forward> forwardList) {
        this._forwardList = forwardList;
    }

    /**
     * 
     * 
     * @param index
     * @param vLoggroup
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setLoggroup(
            final int index,
            final java.lang.String vLoggroup)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._loggroupList.size()) {
            throw new IndexOutOfBoundsException("setLoggroup: Index value '" + index + "' not in range [0.." + (this._loggroupList.size() - 1) + "]");
        }
        
        this._loggroupList.set(index, vLoggroup);
    }

    /**
     * 
     * 
     * @param vLoggroupArray
     */
    public void setLoggroup(
            final java.lang.String[] vLoggroupArray) {
        //-- copy array
        _loggroupList.clear();
        
        for (int i = 0; i < vLoggroupArray.length; i++) {
                this._loggroupList.add(vLoggroupArray[i]);
        }
    }

    /**
     * Sets the value of '_loggroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vLoggroupList the Vector to copy.
     */
    public void setLoggroup(
            final java.util.List<java.lang.String> vLoggroupList) {
        // copy vector
        this._loggroupList.clear();
        
        this._loggroupList.addAll(vLoggroupList);
    }

    /**
     * Sets the value of '_loggroupList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param loggroupList the Vector to set.
     */
    public void setLoggroupCollection(
            final java.util.List<java.lang.String> loggroupList) {
        this._loggroupList = loggroupList;
    }

    /**
     * Sets the value of field 'logmsg'. The field 'logmsg' has the
     * following description: The event logmsg
     * 
     * @param logmsg the value of field 'logmsg'.
     */
    public void setLogmsg(
            final org.opennms.netmgt.xml.eventconf.Logmsg logmsg) {
        this._logmsg = logmsg;
    }

    /**
     * Sets the value of field 'mask'. The field 'mask' has the
     * following description: The event mask which helps to
     * uniquely identify an
     *  event
     * 
     * @param mask the value of field 'mask'.
     */
    public void setMask(
            final org.opennms.netmgt.xml.eventconf.Mask mask) {
        this._mask = mask;
    }

    /**
     * Sets the value of field 'mouseovertext'. The field
     * 'mouseovertext' has the following description: The text to
     * be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     * 
     * @param mouseovertext the value of field 'mouseovertext'.
     */
    public void setMouseovertext(
            final java.lang.String mouseovertext) {
        this._mouseovertext = mouseovertext;
    }

    /**
     * 
     * 
     * @param index
     * @param vOperaction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOperaction(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Operaction vOperaction)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._operactionList.size()) {
            throw new IndexOutOfBoundsException("setOperaction: Index value '" + index + "' not in range [0.." + (this._operactionList.size() - 1) + "]");
        }
        
        this._operactionList.set(index, vOperaction);
    }

    /**
     * 
     * 
     * @param vOperactionArray
     */
    public void setOperaction(
            final org.opennms.netmgt.xml.eventconf.Operaction[] vOperactionArray) {
        //-- copy array
        _operactionList.clear();
        
        for (int i = 0; i < vOperactionArray.length; i++) {
                this._operactionList.add(vOperactionArray[i]);
        }
    }

    /**
     * Sets the value of '_operactionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vOperactionList the Vector to copy.
     */
    public void setOperaction(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Operaction> vOperactionList) {
        // copy vector
        this._operactionList.clear();
        
        this._operactionList.addAll(vOperactionList);
    }

    /**
     * Sets the value of '_operactionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param operactionList the Vector to set.
     */
    public void setOperactionCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Operaction> operactionList) {
        this._operactionList = operactionList;
    }

    /**
     * Sets the value of field 'operinstruct'. The field
     * 'operinstruct' has the following description: The operator
     * instruction for this
     *  event
     * 
     * @param operinstruct the value of field 'operinstruct'.
     */
    public void setOperinstruct(
            final java.lang.String operinstruct) {
        this._operinstruct = operinstruct;
    }

    /**
     * 
     * 
     * @param index
     * @param vScript
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setScript(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Script vScript)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._scriptList.size()) {
            throw new IndexOutOfBoundsException("setScript: Index value '" + index + "' not in range [0.." + (this._scriptList.size() - 1) + "]");
        }
        
        this._scriptList.set(index, vScript);
    }

    /**
     * 
     * 
     * @param vScriptArray
     */
    public void setScript(
            final org.opennms.netmgt.xml.eventconf.Script[] vScriptArray) {
        //-- copy array
        _scriptList.clear();
        
        for (int i = 0; i < vScriptArray.length; i++) {
                this._scriptList.add(vScriptArray[i]);
        }
    }

    /**
     * Sets the value of '_scriptList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vScriptList the Vector to copy.
     */
    public void setScript(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Script> vScriptList) {
        // copy vector
        this._scriptList.clear();
        
        this._scriptList.addAll(vScriptList);
    }

    /**
     * Sets the value of '_scriptList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param scriptList the Vector to set.
     */
    public void setScriptCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Script> scriptList) {
        this._scriptList = scriptList;
    }

    /**
     * Sets the value of field 'severity'. The field 'severity' has
     * the following description: The event severity
     * 
     * @param severity the value of field 'severity'.
     */
    public void setSeverity(
            final java.lang.String severity) {
        this._severity = severity;
    }

    /**
     * Sets the value of field 'snmp'. The field 'snmp' has the
     * following description: The snmp information from the trap
     * 
     * @param snmp the value of field 'snmp'.
     */
    public void setSnmp(
            final org.opennms.netmgt.xml.eventconf.Snmp snmp) {
        this._snmp = snmp;
    }

    /**
     * Sets the value of field 'tticket'. The field 'tticket' has
     * the following description: The trouble ticket info
     * 
     * @param tticket the value of field 'tticket'.
     */
    public void setTticket(
            final org.opennms.netmgt.xml.eventconf.Tticket tticket) {
        this._tticket = tticket;
    }

    /**
     * Sets the value of field 'uei'. The field 'uei' has the
     * following description: The Universal Event Identifier
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(
            final java.lang.String uei) {
        this._uei = uei;
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbindsdecode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVarbindsdecode(
            final int index,
            final org.opennms.netmgt.xml.eventconf.Varbindsdecode vVarbindsdecode)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._varbindsdecodeList.size()) {
            throw new IndexOutOfBoundsException("setVarbindsdecode: Index value '" + index + "' not in range [0.." + (this._varbindsdecodeList.size() - 1) + "]");
        }
        
        this._varbindsdecodeList.set(index, vVarbindsdecode);
    }

    /**
     * 
     * 
     * @param vVarbindsdecodeArray
     */
    public void setVarbindsdecode(
            final org.opennms.netmgt.xml.eventconf.Varbindsdecode[] vVarbindsdecodeArray) {
        //-- copy array
        _varbindsdecodeList.clear();
        
        for (int i = 0; i < vVarbindsdecodeArray.length; i++) {
                this._varbindsdecodeList.add(vVarbindsdecodeArray[i]);
        }
    }

    /**
     * Sets the value of '_varbindsdecodeList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVarbindsdecodeList the Vector to copy.
     */
    public void setVarbindsdecode(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Varbindsdecode> vVarbindsdecodeList) {
        // copy vector
        this._varbindsdecodeList.clear();
        
        this._varbindsdecodeList.addAll(vVarbindsdecodeList);
    }

    /**
     * Sets the value of '_varbindsdecodeList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param varbindsdecodeList the Vector to set.
     */
    public void setVarbindsdecodeCollection(
            final java.util.List<org.opennms.netmgt.xml.eventconf.Varbindsdecode> varbindsdecodeList) {
        this._varbindsdecodeList = varbindsdecodeList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.netmgt.xml.eventconf.Even
     */
    public static org.opennms.netmgt.xml.eventconf.Event unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.xml.eventconf.Event) Unmarshaller.unmarshal(org.opennms.netmgt.xml.eventconf.Event.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
