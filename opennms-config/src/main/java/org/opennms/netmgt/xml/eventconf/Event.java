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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Event.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="event")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class Event implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The event mask which helps to uniquely identify an
     *  event
     */
	@XmlElement(name="mask",required=false)
    private Mask m_mask;

    /**
     * The Universal Event Identifier
     */
	@XmlElement(name="uei", required=true)
    private String m_uei;

    /**
     * A human readable name used to identify an event in
     *  the web ui
     */
	@XmlElement(name="event-label", required=true)
    private String m_eventLabel;

    /**
     * The snmp information from the trap
     */
	@XmlElement(name="snmp", required=false)
    private Snmp m_snmp;

    /**
     * The event description
     */
	@XmlElement(name="descr", required=true)
    private String m_descr;

    /**
     * The event logmsg
     */
	@XmlElement(name="logmsg", required=true)
    private Logmsg m_logmsg;

    /**
     * The event severity
     */
	@XmlElement(name="severity", required=true)
    private String m_severity;

    /**
     * The event correlation information
     */
	@XmlElement(name="correlation", required=false)
    private Correlation m_correlation;

    /**
     * The operator instruction for this
     *  event
     */
	@XmlElement(name="operinstruct", required=false)
    private String m_operinstruct;

    /**
     * The automatic action to occur when this event
     *  occurs
     */
	@XmlElement(name="autoaction", required=false)
    private List<Autoaction> m_autoactionList;

    /**
     * The varbind decoding tag used to decode value 
     *  into a string
     */
	@XmlElement(name="varbindsdecode", required=false)
    private List<Varbindsdecode> m_varbindsdecodeList;

    /**
     * The operator action to be taken when this event
     *  occurs
     */
	@XmlElement(name="operaction", required=false)
    private List<Operaction> m_operactionList;

    /**
     * The autoacknowledge information for the
     *  user
     */
	@XmlElement(name="autoacknowledge", required=false)
    private Autoacknowledge m_autoacknowledge;

    /**
     * A logical group with which to associate this
     *  event
     */
	@XmlElement(name="loggroup", required=false)
    private List<String> m_loggroupList;

    /**
     * The trouble ticket info
     */
	@XmlElement(name="tticket", required=false)
    private Tticket m_tticket;

    /**
     * The forwarding information for this
     *  event
     */
	@XmlElement(name="forward", required=false)
    private List<Forward> m_forwardList;

    /**
     * The script information for this
     *  event
     */
	@XmlElement(name="script", required=false)
    private List<Script> m_scriptList;

    /**
     * The text to be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     */
	@XmlElement(name="mouseovertext", required=false)
    private String m_mouseovertext;

    /**
     * Data used to create an event.
     */
	@XmlElement(name="alarm-data", required=false)
    private AlarmData m_alarmData;


      //----------------/
     //- Constructors -/
    //----------------/

    public Event() {
        super();
        this.m_autoactionList = new ArrayList<Autoaction>();
        this.m_varbindsdecodeList = new ArrayList<Varbindsdecode>();
        this.m_operactionList = new ArrayList<Operaction>();
        this.m_loggroupList = new ArrayList<String>();
        this.m_forwardList = new ArrayList<Forward>();
        this.m_scriptList = new ArrayList<Script>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vAutoaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAutoaction(
            final Autoaction vAutoaction)
    throws IndexOutOfBoundsException {
        this.m_autoactionList.add(vAutoaction);
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAutoaction(
            final int index,
            final Autoaction vAutoaction)
    throws IndexOutOfBoundsException {
        this.m_autoactionList.add(index, vAutoaction);
    }

    /**
     * 
     * 
     * @param vForward
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addForward(
            final Forward vForward)
    throws IndexOutOfBoundsException {
        this.m_forwardList.add(vForward);
    }

    /**
     * 
     * 
     * @param index
     * @param vForward
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addForward(
            final int index,
            final Forward vForward)
    throws IndexOutOfBoundsException {
        this.m_forwardList.add(index, vForward);
    }

    /**
     * 
     * 
     * @param vLoggroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLoggroup(
            final String vLoggroup)
    throws IndexOutOfBoundsException {
        this.m_loggroupList.add(vLoggroup);
    }

    /**
     * 
     * 
     * @param index
     * @param vLoggroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLoggroup(
            final int index,
            final String vLoggroup)
    throws IndexOutOfBoundsException {
        this.m_loggroupList.add(index, vLoggroup);
    }

    /**
     * 
     * 
     * @param vOperaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOperaction(
            final Operaction vOperaction)
    throws IndexOutOfBoundsException {
        this.m_operactionList.add(vOperaction);
    }

    /**
     * 
     * 
     * @param index
     * @param vOperaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOperaction(
            final int index,
            final Operaction vOperaction)
    throws IndexOutOfBoundsException {
        this.m_operactionList.add(index, vOperaction);
    }

    /**
     * 
     * 
     * @param vScript
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addScript(
            final Script vScript)
    throws IndexOutOfBoundsException {
        this.m_scriptList.add(vScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vScript
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addScript(
            final int index,
            final Script vScript)
    throws IndexOutOfBoundsException {
        this.m_scriptList.add(index, vScript);
    }

    /**
     * 
     * 
     * @param vVarbindsdecode
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbindsdecode(
            final Varbindsdecode vVarbindsdecode)
    throws IndexOutOfBoundsException {
        this.m_varbindsdecodeList.add(vVarbindsdecode);
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbindsdecode
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVarbindsdecode(
            final int index,
            final Varbindsdecode vVarbindsdecode)
    throws IndexOutOfBoundsException {
        this.m_varbindsdecodeList.add(index, vVarbindsdecode);
    }

    /**
     * Method enumerateAutoaction.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Autoaction> enumerateAutoaction(
    ) {
        return Collections.enumeration(this.m_autoactionList);
    }

    /**
     * Method enumerateForward.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Forward> enumerateForward(
    ) {
        return Collections.enumeration(this.m_forwardList);
    }

    /**
     * Method enumerateLoggroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateLoggroup(
    ) {
        return Collections.enumeration(this.m_loggroupList);
    }

    /**
     * Method enumerateOperaction.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Operaction> enumerateOperaction(
    ) {
        return Collections.enumeration(this.m_operactionList);
    }

    /**
     * Method enumerateScript.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Script> enumerateScript(
    ) {
        return Collections.enumeration(this.m_scriptList);
    }

    /**
     * Method enumerateVarbindsdecode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Varbindsdecode> enumerateVarbindsdecode(
    ) {
        return Collections.enumeration(this.m_varbindsdecodeList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Event) {
        
            Event temp = (Event)obj;
            if (this.m_mask != null) {
                if (temp.m_mask == null) return false;
                else if (!(this.m_mask.equals(temp.m_mask))) 
                    return false;
            }
            else if (temp.m_mask != null)
                return false;
            if (this.m_uei != null) {
                if (temp.m_uei == null) return false;
                else if (!(this.m_uei.equals(temp.m_uei))) 
                    return false;
            }
            else if (temp.m_uei != null)
                return false;
            if (this.m_eventLabel != null) {
                if (temp.m_eventLabel == null) return false;
                else if (!(this.m_eventLabel.equals(temp.m_eventLabel))) 
                    return false;
            }
            else if (temp.m_eventLabel != null)
                return false;
            if (this.m_snmp != null) {
                if (temp.m_snmp == null) return false;
                else if (!(this.m_snmp.equals(temp.m_snmp))) 
                    return false;
            }
            else if (temp.m_snmp != null)
                return false;
            if (this.m_descr != null) {
                if (temp.m_descr == null) return false;
                else if (!(this.m_descr.equals(temp.m_descr))) 
                    return false;
            }
            else if (temp.m_descr != null)
                return false;
            if (this.m_logmsg != null) {
                if (temp.m_logmsg == null) return false;
                else if (!(this.m_logmsg.equals(temp.m_logmsg))) 
                    return false;
            }
            else if (temp.m_logmsg != null)
                return false;
            if (this.m_severity != null) {
                if (temp.m_severity == null) return false;
                else if (!(this.m_severity.equals(temp.m_severity))) 
                    return false;
            }
            else if (temp.m_severity != null)
                return false;
            if (this.m_correlation != null) {
                if (temp.m_correlation == null) return false;
                else if (!(this.m_correlation.equals(temp.m_correlation))) 
                    return false;
            }
            else if (temp.m_correlation != null)
                return false;
            if (this.m_operinstruct != null) {
                if (temp.m_operinstruct == null) return false;
                else if (!(this.m_operinstruct.equals(temp.m_operinstruct))) 
                    return false;
            }
            else if (temp.m_operinstruct != null)
                return false;
            if (this.m_autoactionList != null) {
                if (temp.m_autoactionList == null) return false;
                else if (!(this.m_autoactionList.equals(temp.m_autoactionList))) 
                    return false;
            }
            else if (temp.m_autoactionList != null)
                return false;
            if (this.m_varbindsdecodeList != null) {
                if (temp.m_varbindsdecodeList == null) return false;
                else if (!(this.m_varbindsdecodeList.equals(temp.m_varbindsdecodeList))) 
                    return false;
            }
            else if (temp.m_varbindsdecodeList != null)
                return false;
            if (this.m_operactionList != null) {
                if (temp.m_operactionList == null) return false;
                else if (!(this.m_operactionList.equals(temp.m_operactionList))) 
                    return false;
            }
            else if (temp.m_operactionList != null)
                return false;
            if (this.m_autoacknowledge != null) {
                if (temp.m_autoacknowledge == null) return false;
                else if (!(this.m_autoacknowledge.equals(temp.m_autoacknowledge))) 
                    return false;
            }
            else if (temp.m_autoacknowledge != null)
                return false;
            if (this.m_loggroupList != null) {
                if (temp.m_loggroupList == null) return false;
                else if (!(this.m_loggroupList.equals(temp.m_loggroupList))) 
                    return false;
            }
            else if (temp.m_loggroupList != null)
                return false;
            if (this.m_tticket != null) {
                if (temp.m_tticket == null) return false;
                else if (!(this.m_tticket.equals(temp.m_tticket))) 
                    return false;
            }
            else if (temp.m_tticket != null)
                return false;
            if (this.m_forwardList != null) {
                if (temp.m_forwardList == null) return false;
                else if (!(this.m_forwardList.equals(temp.m_forwardList))) 
                    return false;
            }
            else if (temp.m_forwardList != null)
                return false;
            if (this.m_scriptList != null) {
                if (temp.m_scriptList == null) return false;
                else if (!(this.m_scriptList.equals(temp.m_scriptList))) 
                    return false;
            }
            else if (temp.m_scriptList != null)
                return false;
            if (this.m_mouseovertext != null) {
                if (temp.m_mouseovertext == null) return false;
                else if (!(this.m_mouseovertext.equals(temp.m_mouseovertext))) 
                    return false;
            }
            else if (temp.m_mouseovertext != null)
                return false;
            if (this.m_alarmData != null) {
                if (temp.m_alarmData == null) return false;
                else if (!(this.m_alarmData.equals(temp.m_alarmData))) 
                    return false;
            }
            else if (temp.m_alarmData != null)
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
    public AlarmData getAlarmData(
    ) {
        return this.m_alarmData;
    }

    /**
     * Returns the value of field 'autoacknowledge'. The field
     * 'autoacknowledge' has the following description: The
     * autoacknowledge information for the
     *  user
     * 
     * @return the value of field 'Autoacknowledge'.
     */
    public Autoacknowledge getAutoacknowledge(
    ) {
        return this.m_autoacknowledge;
    }

    /**
     * Method getAutoaction.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Autoaction at the given inde
     */
    public Autoaction getAutoaction(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_autoactionList.size()) {
            throw new IndexOutOfBoundsException("getAutoaction: Index value '" + index + "' not in range [0.." + (this.m_autoactionList.size() - 1) + "]");
        }
        
        return (Autoaction) m_autoactionList.get(index);
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
    public Autoaction[] getAutoaction(
    ) {
        Autoaction[] array = new Autoaction[0];
        return (Autoaction[]) this.m_autoactionList.toArray(array);
    }

    /**
     * Method getAutoactionCollection.Returns a reference to
     * '_autoactionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Autoaction> getAutoactionCollection(
    ) {
        return this.m_autoactionList;
    }

    /**
     * Method getAutoactionCount.
     * 
     * @return the size of this collection
     */
    public int getAutoactionCount(
    ) {
        return this.m_autoactionList.size();
    }

    /**
     * Returns the value of field 'correlation'. The field
     * 'correlation' has the following description: The event
     * correlation information
     * 
     * @return the value of field 'Correlation'.
     */
    public Correlation getCorrelation(
    ) {
        return this.m_correlation;
    }

    /**
     * Returns the value of field 'descr'. The field 'descr' has
     * the following description: The event description
     * 
     * @return the value of field 'Descr'.
     */
    public String getDescr(
    ) {
        return this.m_descr;
    }

    /**
     * Returns the value of field 'eventLabel'. The field
     * 'eventLabel' has the following description: A human readable
     * name used to identify an event in
     *  the web ui
     * 
     * @return the value of field 'EventLabel'.
     */
    public String getEventLabel(
    ) {
        return this.m_eventLabel;
    }

    /**
     * Method getForward.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Forward at the given index
     */
    public Forward getForward(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_forwardList.size()) {
            throw new IndexOutOfBoundsException("getForward: Index value '" + index + "' not in range [0.." + (this.m_forwardList.size() - 1) + "]");
        }
        
        return (Forward) m_forwardList.get(index);
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
    public Forward[] getForward(
    ) {
        Forward[] array = new Forward[0];
        return (Forward[]) this.m_forwardList.toArray(array);
    }

    /**
     * Method getForwardCollection.Returns a reference to
     * '_forwardList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Forward> getForwardCollection(
    ) {
        return this.m_forwardList;
    }

    /**
     * Method getForwardCount.
     * 
     * @return the size of this collection
     */
    public int getForwardCount(
    ) {
        return this.m_forwardList.size();
    }

    /**
     * Method getLoggroup.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getLoggroup(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_loggroupList.size()) {
            throw new IndexOutOfBoundsException("getLoggroup: Index value '" + index + "' not in range [0.." + (this.m_loggroupList.size() - 1) + "]");
        }
        
        return (String) m_loggroupList.get(index);
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
    public String[] getLoggroup(
    ) {
        String[] array = new String[0];
        return (String[]) this.m_loggroupList.toArray(array);
    }

    /**
     * Method getLoggroupCollection.Returns a reference to
     * '_loggroupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getLoggroupCollection(
    ) {
        return this.m_loggroupList;
    }

    /**
     * Method getLoggroupCount.
     * 
     * @return the size of this collection
     */
    public int getLoggroupCount(
    ) {
        return this.m_loggroupList.size();
    }

    /**
     * Returns the value of field 'logmsg'. The field 'logmsg' has
     * the following description: The event logmsg
     * 
     * @return the value of field 'Logmsg'.
     */
    public Logmsg getLogmsg(
    ) {
        return this.m_logmsg;
    }

    /**
     * Returns the value of field 'mask'. The field 'mask' has the
     * following description: The event mask which helps to
     * uniquely identify an
     *  event
     * 
     * @return the value of field 'Mask'.
     */
    public Mask getMask(
    ) {
        return this.m_mask;
    }

    /**
     * Returns the value of field 'mouseovertext'. The field
     * 'mouseovertext' has the following description: The text to
     * be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     * 
     * @return the value of field 'Mouseovertext'.
     */
    public String getMouseovertext(
    ) {
        return this.m_mouseovertext;
    }

    /**
     * Method getOperaction.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Operaction at the given inde
     */
    public Operaction getOperaction(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_operactionList.size()) {
            throw new IndexOutOfBoundsException("getOperaction: Index value '" + index + "' not in range [0.." + (this.m_operactionList.size() - 1) + "]");
        }
        
        return (Operaction) m_operactionList.get(index);
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
    public Operaction[] getOperaction(
    ) {
        Operaction[] array = new Operaction[0];
        return (Operaction[]) this.m_operactionList.toArray(array);
    }

    /**
     * Method getOperactionCollection.Returns a reference to
     * '_operactionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Operaction> getOperactionCollection(
    ) {
        return this.m_operactionList;
    }

    /**
     * Method getOperactionCount.
     * 
     * @return the size of this collection
     */
    public int getOperactionCount(
    ) {
        return this.m_operactionList.size();
    }

    /**
     * Returns the value of field 'operinstruct'. The field
     * 'operinstruct' has the following description: The operator
     * instruction for this
     *  event
     * 
     * @return the value of field 'Operinstruct'.
     */
    public String getOperinstruct(
    ) {
        return this.m_operinstruct;
    }

    /**
     * Method getScript.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Script at the given index
     */
    public Script getScript(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_scriptList.size()) {
            throw new IndexOutOfBoundsException("getScript: Index value '" + index + "' not in range [0.." + (this.m_scriptList.size() - 1) + "]");
        }
        
        return (Script) m_scriptList.get(index);
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
    public Script[] getScript(
    ) {
        Script[] array = new Script[0];
        return (Script[]) this.m_scriptList.toArray(array);
    }

    /**
     * Method getScriptCollection.Returns a reference to
     * '_scriptList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Script> getScriptCollection(
    ) {
        return this.m_scriptList;
    }

    /**
     * Method getScriptCount.
     * 
     * @return the size of this collection
     */
    public int getScriptCount(
    ) {
        return this.m_scriptList.size();
    }

    /**
     * Returns the value of field 'severity'. The field 'severity'
     * has the following description: The event severity
     * 
     * @return the value of field 'Severity'.
     */
    public String getSeverity(
    ) {
        return this.m_severity;
    }

    /**
     * Returns the value of field 'snmp'. The field 'snmp' has the
     * following description: The snmp information from the trap
     * 
     * @return the value of field 'Snmp'.
     */
    public Snmp getSnmp(
    ) {
        return this.m_snmp;
    }

    /**
     * Returns the value of field 'tticket'. The field 'tticket'
     * has the following description: The trouble ticket info
     * 
     * @return the value of field 'Tticket'.
     */
    public Tticket getTticket(
    ) {
        return this.m_tticket;
    }

    /**
     * Returns the value of field 'uei'. The field 'uei' has the
     * following description: The Universal Event Identifier
     * 
     * @return the value of field 'Uei'.
     */
    public String getUei(
    ) {
        return this.m_uei;
    }

    /**
     * Method getVarbindsdecode.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Varbindsdecode at the given
     * index
     */
    public Varbindsdecode getVarbindsdecode(
            final int index)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_varbindsdecodeList.size()) {
            throw new IndexOutOfBoundsException("getVarbindsdecode: Index value '" + index + "' not in range [0.." + (this.m_varbindsdecodeList.size() - 1) + "]");
        }
        
        return (Varbindsdecode) m_varbindsdecodeList.get(index);
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
    public Varbindsdecode[] getVarbindsdecode(
    ) {
        Varbindsdecode[] array = new Varbindsdecode[0];
        return (Varbindsdecode[]) this.m_varbindsdecodeList.toArray(array);
    }

    /**
     * Method getVarbindsdecodeCollection.Returns a reference to
     * '_varbindsdecodeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Varbindsdecode> getVarbindsdecodeCollection(
    ) {
        return this.m_varbindsdecodeList;
    }

    /**
     * Method getVarbindsdecodeCount.
     * 
     * @return the size of this collection
     */
    public int getVarbindsdecodeCount(
    ) {
        return this.m_varbindsdecodeList.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        return new HashCodeBuilder().append(getAutoactionCount()).append(getForwardCount()).append(getLoggroupCount()).
        	append(getOperactionCount()).append(getScriptCount()).append(getVarbindsdecodeCount()).append(getAlarmData()).
        	append(getAutoacknowledge()).append(getAutoaction()).append(getCorrelation()).append(getDescr()).append(getEventLabel()).
        	append(getForward()).append(getLoggroup()).append(getLogmsg()).append(getMask()).append(getMouseovertext()).
        	append(getOperaction()).append(getOperinstruct()).append(getScript()).append(getSeverity()).append(getSnmp()).
        	append(getTticket()).append(getUei()).append(getVarbindsdecode()).toHashCode();
        	
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
    public Iterator<Autoaction> iterateAutoaction(
    ) {
        return this.m_autoactionList.iterator();
    }

    /**
     * Method iterateForward.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Forward> iterateForward(
    ) {
        return this.m_forwardList.iterator();
    }

    /**
     * Method iterateLoggroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateLoggroup(
    ) {
        return this.m_loggroupList.iterator();
    }

    /**
     * Method iterateOperaction.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Operaction> iterateOperaction(
    ) {
        return this.m_operactionList.iterator();
    }

    /**
     * Method iterateScript.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Script> iterateScript(
    ) {
        return this.m_scriptList.iterator();
    }

    /**
     * Method iterateVarbindsdecode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Varbindsdecode> iterateVarbindsdecode(
    ) {
        return this.m_varbindsdecodeList.iterator();
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
        this.m_autoactionList.clear();
    }

    /**
     */
    public void removeAllForward(
    ) {
        this.m_forwardList.clear();
    }

    /**
     */
    public void removeAllLoggroup(
    ) {
        this.m_loggroupList.clear();
    }

    /**
     */
    public void removeAllOperaction(
    ) {
        this.m_operactionList.clear();
    }

    /**
     */
    public void removeAllScript(
    ) {
        this.m_scriptList.clear();
    }

    /**
     */
    public void removeAllVarbindsdecode(
    ) {
        this.m_varbindsdecodeList.clear();
    }

    /**
     * Method removeAutoaction.
     * 
     * @param vAutoaction
     * @return true if the object was removed from the collection.
     */
    public boolean removeAutoaction(
            final Autoaction vAutoaction) {
        boolean removed = m_autoactionList.remove(vAutoaction);
        return removed;
    }

    /**
     * Method removeAutoactionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Autoaction removeAutoactionAt(
            final int index) {
        Object obj = this.m_autoactionList.remove(index);
        return (Autoaction) obj;
    }

    /**
     * Method removeForward.
     * 
     * @param vForward
     * @return true if the object was removed from the collection.
     */
    public boolean removeForward(
            final Forward vForward) {
        boolean removed = m_forwardList.remove(vForward);
        return removed;
    }

    /**
     * Method removeForwardAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Forward removeForwardAt(
            final int index) {
        Object obj = this.m_forwardList.remove(index);
        return (Forward) obj;
    }

    /**
     * Method removeLoggroup.
     * 
     * @param vLoggroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeLoggroup(
            final String vLoggroup) {
        boolean removed = m_loggroupList.remove(vLoggroup);
        return removed;
    }

    /**
     * Method removeLoggroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeLoggroupAt(
            final int index) {
        Object obj = this.m_loggroupList.remove(index);
        return (String) obj;
    }

    /**
     * Method removeOperaction.
     * 
     * @param vOperaction
     * @return true if the object was removed from the collection.
     */
    public boolean removeOperaction(
            final Operaction vOperaction) {
        boolean removed = m_operactionList.remove(vOperaction);
        return removed;
    }

    /**
     * Method removeOperactionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Operaction removeOperactionAt(
            final int index) {
        Object obj = this.m_operactionList.remove(index);
        return (Operaction) obj;
    }

    /**
     * Method removeScript.
     * 
     * @param vScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeScript(
            final Script vScript) {
        boolean removed = m_scriptList.remove(vScript);
        return removed;
    }

    /**
     * Method removeScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Script removeScriptAt(
            final int index) {
        Object obj = this.m_scriptList.remove(index);
        return (Script) obj;
    }

    /**
     * Method removeVarbindsdecode.
     * 
     * @param vVarbindsdecode
     * @return true if the object was removed from the collection.
     */
    public boolean removeVarbindsdecode(
            final Varbindsdecode vVarbindsdecode) {
        boolean removed = m_varbindsdecodeList.remove(vVarbindsdecode);
        return removed;
    }

    /**
     * Method removeVarbindsdecodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Varbindsdecode removeVarbindsdecodeAt(
            final int index) {
        Object obj = this.m_varbindsdecodeList.remove(index);
        return (Varbindsdecode) obj;
    }

    /**
     * Sets the value of field 'alarmData'. The field 'alarmData'
     * has the following description: Data used to create an event.
     * 
     * @param alarmData the value of field 'alarmData'.
     */
    public void setAlarmData(
            final AlarmData alarmData) {
        this.m_alarmData = alarmData;
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
            final Autoacknowledge autoacknowledge) {
        this.m_autoacknowledge = autoacknowledge;
    }

    /**
     * 
     * 
     * @param index
     * @param vAutoaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setAutoaction(
            final int index,
            final Autoaction vAutoaction)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_autoactionList.size()) {
            throw new IndexOutOfBoundsException("setAutoaction: Index value '" + index + "' not in range [0.." + (this.m_autoactionList.size() - 1) + "]");
        }
        
        this.m_autoactionList.set(index, vAutoaction);
    }

    /**
     * 
     * 
     * @param vAutoactionArray
     */
    public void setAutoaction(
            final Autoaction[] vAutoactionArray) {
        //-- copy array
        m_autoactionList.clear();
        
        for (int i = 0; i < vAutoactionArray.length; i++) {
                this.m_autoactionList.add(vAutoactionArray[i]);
        }
    }

    /**
     * Sets the value of '_autoactionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vAutoactionList the Vector to copy.
     */
    public void setAutoaction(
            final List<Autoaction> vAutoactionList) {
        // copy vector
        this.m_autoactionList.clear();
        
        this.m_autoactionList.addAll(vAutoactionList);
    }

    /**
     * Sets the value of '_autoactionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param autoactionList the Vector to set.
     */
    public void setAutoactionCollection(
            final List<Autoaction> autoactionList) {
        this.m_autoactionList = autoactionList;
    }

    /**
     * Sets the value of field 'correlation'. The field
     * 'correlation' has the following description: The event
     * correlation information
     * 
     * @param correlation the value of field 'correlation'.
     */
    public void setCorrelation(
            final Correlation correlation) {
        this.m_correlation = correlation;
    }

    /**
     * Sets the value of field 'descr'. The field 'descr' has the
     * following description: The event description
     * 
     * @param descr the value of field 'descr'.
     */
    public void setDescr(
            final String descr) {
        this.m_descr = descr;
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
            final String eventLabel) {
        this.m_eventLabel = eventLabel;
    }

    /**
     * 
     * 
     * @param index
     * @param vForward
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setForward(
            final int index,
            final Forward vForward)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_forwardList.size()) {
            throw new IndexOutOfBoundsException("setForward: Index value '" + index + "' not in range [0.." + (this.m_forwardList.size() - 1) + "]");
        }
        
        this.m_forwardList.set(index, vForward);
    }

    /**
     * 
     * 
     * @param vForwardArray
     */
    public void setForward(
            final Forward[] vForwardArray) {
        //-- copy array
        m_forwardList.clear();
        
        for (int i = 0; i < vForwardArray.length; i++) {
                this.m_forwardList.add(vForwardArray[i]);
        }
    }

    /**
     * Sets the value of '_forwardList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vForwardList the Vector to copy.
     */
    public void setForward(
            final List<Forward> vForwardList) {
        // copy vector
        this.m_forwardList.clear();
        
        this.m_forwardList.addAll(vForwardList);
    }

    /**
     * Sets the value of '_forwardList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param forwardList the Vector to set.
     */
    public void setForwardCollection(
            final List<Forward> forwardList) {
        this.m_forwardList = forwardList;
    }

    /**
     * 
     * 
     * @param index
     * @param vLoggroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setLoggroup(
            final int index,
            final String vLoggroup)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_loggroupList.size()) {
            throw new IndexOutOfBoundsException("setLoggroup: Index value '" + index + "' not in range [0.." + (this.m_loggroupList.size() - 1) + "]");
        }
        
        this.m_loggroupList.set(index, vLoggroup);
    }

    /**
     * 
     * 
     * @param vLoggroupArray
     */
    public void setLoggroup(
            final String[] vLoggroupArray) {
        //-- copy array
        m_loggroupList.clear();
        
        for (int i = 0; i < vLoggroupArray.length; i++) {
                this.m_loggroupList.add(vLoggroupArray[i]);
        }
    }

    /**
     * Sets the value of '_loggroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vLoggroupList the Vector to copy.
     */
    public void setLoggroup(
            final List<String> vLoggroupList) {
        // copy vector
        this.m_loggroupList.clear();
        
        this.m_loggroupList.addAll(vLoggroupList);
    }

    /**
     * Sets the value of '_loggroupList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param loggroupList the Vector to set.
     */
    public void setLoggroupCollection(
            final List<String> loggroupList) {
        this.m_loggroupList = loggroupList;
    }

    /**
     * Sets the value of field 'logmsg'. The field 'logmsg' has the
     * following description: The event logmsg
     * 
     * @param logmsg the value of field 'logmsg'.
     */
    public void setLogmsg(
            final Logmsg logmsg) {
        this.m_logmsg = logmsg;
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
            final Mask mask) {
        this.m_mask = mask;
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
            final String mouseovertext) {
        this.m_mouseovertext = mouseovertext;
    }

    /**
     * 
     * 
     * @param index
     * @param vOperaction
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOperaction(
            final int index,
            final Operaction vOperaction)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_operactionList.size()) {
            throw new IndexOutOfBoundsException("setOperaction: Index value '" + index + "' not in range [0.." + (this.m_operactionList.size() - 1) + "]");
        }
        
        this.m_operactionList.set(index, vOperaction);
    }

    /**
     * 
     * 
     * @param vOperactionArray
     */
    public void setOperaction(
            final Operaction[] vOperactionArray) {
        //-- copy array
        m_operactionList.clear();
        
        for (int i = 0; i < vOperactionArray.length; i++) {
                this.m_operactionList.add(vOperactionArray[i]);
        }
    }

    /**
     * Sets the value of '_operactionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vOperactionList the Vector to copy.
     */
    public void setOperaction(
            final List<Operaction> vOperactionList) {
        // copy vector
        this.m_operactionList.clear();
        
        this.m_operactionList.addAll(vOperactionList);
    }

    /**
     * Sets the value of '_operactionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param operactionList the Vector to set.
     */
    public void setOperactionCollection(
            final List<Operaction> operactionList) {
        this.m_operactionList = operactionList;
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
            final String operinstruct) {
        this.m_operinstruct = operinstruct;
    }

    /**
     * 
     * 
     * @param index
     * @param vScript
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setScript(
            final int index,
            final Script vScript)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_scriptList.size()) {
            throw new IndexOutOfBoundsException("setScript: Index value '" + index + "' not in range [0.." + (this.m_scriptList.size() - 1) + "]");
        }
        
        this.m_scriptList.set(index, vScript);
    }

    /**
     * 
     * 
     * @param vScriptArray
     */
    public void setScript(
            final Script[] vScriptArray) {
        //-- copy array
        m_scriptList.clear();
        
        for (int i = 0; i < vScriptArray.length; i++) {
                this.m_scriptList.add(vScriptArray[i]);
        }
    }

    /**
     * Sets the value of '_scriptList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vScriptList the Vector to copy.
     */
    public void setScript(
            final List<Script> vScriptList) {
        // copy vector
        this.m_scriptList.clear();
        
        this.m_scriptList.addAll(vScriptList);
    }

    /**
     * Sets the value of '_scriptList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param scriptList the Vector to set.
     */
    public void setScriptCollection(
            final List<Script> scriptList) {
        this.m_scriptList = scriptList;
    }

    /**
     * Sets the value of field 'severity'. The field 'severity' has
     * the following description: The event severity
     * 
     * @param severity the value of field 'severity'.
     */
    public void setSeverity(
            final String severity) {
        this.m_severity = severity;
    }

    /**
     * Sets the value of field 'snmp'. The field 'snmp' has the
     * following description: The snmp information from the trap
     * 
     * @param snmp the value of field 'snmp'.
     */
    public void setSnmp(
            final Snmp snmp) {
        this.m_snmp = snmp;
    }

    /**
     * Sets the value of field 'tticket'. The field 'tticket' has
     * the following description: The trouble ticket info
     * 
     * @param tticket the value of field 'tticket'.
     */
    public void setTticket(
            final Tticket tticket) {
        this.m_tticket = tticket;
    }

    /**
     * Sets the value of field 'uei'. The field 'uei' has the
     * following description: The Universal Event Identifier
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(
            final String uei) {
        this.m_uei = uei;
    }

    /**
     * 
     * 
     * @param index
     * @param vVarbindsdecode
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVarbindsdecode(
            final int index,
            final Varbindsdecode vVarbindsdecode)
    throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.m_varbindsdecodeList.size()) {
            throw new IndexOutOfBoundsException("setVarbindsdecode: Index value '" + index + "' not in range [0.." + (this.m_varbindsdecodeList.size() - 1) + "]");
        }
        
        this.m_varbindsdecodeList.set(index, vVarbindsdecode);
    }

    /**
     * 
     * 
     * @param vVarbindsdecodeArray
     */
    public void setVarbindsdecode(
            final Varbindsdecode[] vVarbindsdecodeArray) {
        //-- copy array
        m_varbindsdecodeList.clear();
        
        for (int i = 0; i < vVarbindsdecodeArray.length; i++) {
                this.m_varbindsdecodeList.add(vVarbindsdecodeArray[i]);
        }
    }

    /**
     * Sets the value of '_varbindsdecodeList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vVarbindsdecodeList the Vector to copy.
     */
    public void setVarbindsdecode(
            final List<Varbindsdecode> vVarbindsdecodeList) {
        // copy vector
        this.m_varbindsdecodeList.clear();
        
        this.m_varbindsdecodeList.addAll(vVarbindsdecodeList);
    }

    /**
     * Sets the value of '_varbindsdecodeList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param varbindsdecodeList the Vector to set.
     */
    public void setVarbindsdecodeCollection(
            final List<Varbindsdecode> varbindsdecodeList) {
        this.m_varbindsdecodeList = varbindsdecodeList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled Even
     */
    public static Event unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (Event) Unmarshaller.unmarshal(Event.class, reader);
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
