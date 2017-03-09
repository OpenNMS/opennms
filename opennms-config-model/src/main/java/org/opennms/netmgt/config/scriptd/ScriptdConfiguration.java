/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.scriptd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the scriptd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "scriptd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "engine")
    private List<Engine> engineList = new ArrayList<>();;

    @XmlElement(name = "start-script")
    private List<StartScript> startScriptList = new ArrayList<>();;

    @XmlElement(name = "stop-script")
    private List<StopScript> stopScriptList = new ArrayList<>();;

    @XmlElement(name = "reload-script")
    private List<ReloadScript> reloadScriptList = new ArrayList<>();;

    @XmlElement(name = "event-script")
    private List<EventScript> eventScriptList = new ArrayList<>();;

    /**
     * 
     * 
     * @param vEngine
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEngine(final Engine vEngine) throws IndexOutOfBoundsException {
        this.engineList.add(vEngine);
    }

    /**
     * 
     * 
     * @param index
     * @param vEngine
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEngine(final int index, final Engine vEngine) throws IndexOutOfBoundsException {
        this.engineList.add(index, vEngine);
    }

    /**
     * 
     * 
     * @param vEventScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEventScript(final EventScript vEventScript) throws IndexOutOfBoundsException {
        this.eventScriptList.add(vEventScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vEventScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addEventScript(final int index, final EventScript vEventScript) throws IndexOutOfBoundsException {
        this.eventScriptList.add(index, vEventScript);
    }

    /**
     * 
     * 
     * @param vReloadScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReloadScript(final ReloadScript vReloadScript) throws IndexOutOfBoundsException {
        this.reloadScriptList.add(vReloadScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vReloadScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReloadScript(final int index, final ReloadScript vReloadScript) throws IndexOutOfBoundsException {
        this.reloadScriptList.add(index, vReloadScript);
    }

    /**
     * 
     * 
     * @param vStartScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStartScript(final StartScript vStartScript) throws IndexOutOfBoundsException {
        this.startScriptList.add(vStartScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vStartScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStartScript(final int index, final StartScript vStartScript) throws IndexOutOfBoundsException {
        this.startScriptList.add(index, vStartScript);
    }

    /**
     * 
     * 
     * @param vStopScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStopScript(final StopScript vStopScript) throws IndexOutOfBoundsException {
        this.stopScriptList.add(vStopScript);
    }

    /**
     * 
     * 
     * @param index
     * @param vStopScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addStopScript(final int index, final StopScript vStopScript) throws IndexOutOfBoundsException {
        this.stopScriptList.add(index, vStopScript);
    }

    /**
     * Method enumerateEngine.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Engine> enumerateEngine() {
        return Collections.enumeration(this.engineList);
    }

    /**
     * Method enumerateEventScript.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<EventScript> enumerateEventScript() {
        return Collections.enumeration(this.eventScriptList);
    }

    /**
     * Method enumerateReloadScript.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<ReloadScript> enumerateReloadScript() {
        return Collections.enumeration(this.reloadScriptList);
    }

    /**
     * Method enumerateStartScript.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<StartScript> enumerateStartScript() {
        return Collections.enumeration(this.startScriptList);
    }

    /**
     * Method enumerateStopScript.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<StopScript> enumerateStopScript() {
        return Collections.enumeration(this.stopScriptList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof ScriptdConfiguration) {
            ScriptdConfiguration temp = (ScriptdConfiguration)obj;
            boolean equals = Objects.equals(temp.engineList, engineList)
                && Objects.equals(temp.startScriptList, startScriptList)
                && Objects.equals(temp.stopScriptList, stopScriptList)
                && Objects.equals(temp.reloadScriptList, reloadScriptList)
                && Objects.equals(temp.eventScriptList, eventScriptList);
            return equals;
        }
        return false;
    }

    /**
     * Method getEngine.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the Engine at the
     * given index
     */
    public Engine getEngine(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.engineList.size()) {
            throw new IndexOutOfBoundsException("getEngine: Index value '" + index + "' not in range [0.." + (this.engineList.size() - 1) + "]");
        }
        
        return (Engine) engineList.get(index);
    }

    /**
     * Method getEngine.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public Engine[] getEngine() {
        Engine[] array = new Engine[0];
        return (Engine[]) this.engineList.toArray(array);
    }

    /**
     * Method getEngineCollection.Returns a reference to 'engineList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Engine> getEngineCollection() {
        return this.engineList;
    }

    /**
     * Method getEngineCount.
     * 
     * @return the size of this collection
     */
    public int getEngineCount() {
        return this.engineList.size();
    }

    /**
     * Method getEventScript.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the EventScript at
     * the given index
     */
    public EventScript getEventScript(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.eventScriptList.size()) {
            throw new IndexOutOfBoundsException("getEventScript: Index value '" + index + "' not in range [0.." + (this.eventScriptList.size() - 1) + "]");
        }
        
        return (EventScript) eventScriptList.get(index);
    }

    /**
     * Method getEventScript.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public EventScript[] getEventScript() {
        EventScript[] array = new EventScript[0];
        return (EventScript[]) this.eventScriptList.toArray(array);
    }

    /**
     * Method getEventScriptCollection.Returns a reference to 'eventScriptList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<EventScript> getEventScriptCollection() {
        return this.eventScriptList;
    }

    /**
     * Method getEventScriptCount.
     * 
     * @return the size of this collection
     */
    public int getEventScriptCount() {
        return this.eventScriptList.size();
    }

    /**
     * Method getReloadScript.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the ReloadScript at
     * the given index
     */
    public ReloadScript getReloadScript(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reloadScriptList.size()) {
            throw new IndexOutOfBoundsException("getReloadScript: Index value '" + index + "' not in range [0.." + (this.reloadScriptList.size() - 1) + "]");
        }
        
        return (ReloadScript) reloadScriptList.get(index);
    }

    /**
     * Method getReloadScript.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public ReloadScript[] getReloadScript() {
        ReloadScript[] array = new ReloadScript[0];
        return (ReloadScript[]) this.reloadScriptList.toArray(array);
    }

    /**
     * Method getReloadScriptCollection.Returns a reference to 'reloadScriptList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ReloadScript> getReloadScriptCollection() {
        return this.reloadScriptList;
    }

    /**
     * Method getReloadScriptCount.
     * 
     * @return the size of this collection
     */
    public int getReloadScriptCount() {
        return this.reloadScriptList.size();
    }

    /**
     * Method getStartScript.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the StartScript at
     * the given index
     */
    public StartScript getStartScript(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.startScriptList.size()) {
            throw new IndexOutOfBoundsException("getStartScript: Index value '" + index + "' not in range [0.." + (this.startScriptList.size() - 1) + "]");
        }
        
        return (StartScript) startScriptList.get(index);
    }

    /**
     * Method getStartScript.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public StartScript[] getStartScript() {
        StartScript[] array = new StartScript[0];
        return (StartScript[]) this.startScriptList.toArray(array);
    }

    /**
     * Method getStartScriptCollection.Returns a reference to 'startScriptList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<StartScript> getStartScriptCollection() {
        return this.startScriptList;
    }

    /**
     * Method getStartScriptCount.
     * 
     * @return the size of this collection
     */
    public int getStartScriptCount() {
        return this.startScriptList.size();
    }

    /**
     * Method getStopScript.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the StopScript at
     * the given index
     */
    public StopScript getStopScript(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.stopScriptList.size()) {
            throw new IndexOutOfBoundsException("getStopScript: Index value '" + index + "' not in range [0.." + (this.stopScriptList.size() - 1) + "]");
        }
        
        return (StopScript) stopScriptList.get(index);
    }

    /**
     * Method getStopScript.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public StopScript[] getStopScript() {
        StopScript[] array = new StopScript[0];
        return (StopScript[]) this.stopScriptList.toArray(array);
    }

    /**
     * Method getStopScriptCollection.Returns a reference to 'stopScriptList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<StopScript> getStopScriptCollection() {
        return this.stopScriptList;
    }

    /**
     * Method getStopScriptCount.
     * 
     * @return the size of this collection
     */
    public int getStopScriptCount() {
        return this.stopScriptList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            engineList, 
            startScriptList, 
            stopScriptList, 
            reloadScriptList, 
            eventScriptList);
        return hash;
    }

    /**
     * Method iterateEngine.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Engine> iterateEngine() {
        return this.engineList.iterator();
    }

    /**
     * Method iterateEventScript.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<EventScript> iterateEventScript() {
        return this.eventScriptList.iterator();
    }

    /**
     * Method iterateReloadScript.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<ReloadScript> iterateReloadScript() {
        return this.reloadScriptList.iterator();
    }

    /**
     * Method iterateStartScript.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<StartScript> iterateStartScript() {
        return this.startScriptList.iterator();
    }

    /**
     * Method iterateStopScript.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<StopScript> iterateStopScript() {
        return this.stopScriptList.iterator();
    }

    /**
     */
    public void removeAllEngine() {
        this.engineList.clear();
    }

    /**
     */
    public void removeAllEventScript() {
        this.eventScriptList.clear();
    }

    /**
     */
    public void removeAllReloadScript() {
        this.reloadScriptList.clear();
    }

    /**
     */
    public void removeAllStartScript() {
        this.startScriptList.clear();
    }

    /**
     */
    public void removeAllStopScript() {
        this.stopScriptList.clear();
    }

    /**
     * Method removeEngine.
     * 
     * @param vEngine
     * @return true if the object was removed from the collection.
     */
    public boolean removeEngine(final Engine vEngine) {
        boolean removed = engineList.remove(vEngine);
        return removed;
    }

    /**
     * Method removeEngineAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Engine removeEngineAt(final int index) {
        Object obj = this.engineList.remove(index);
        return (Engine) obj;
    }

    /**
     * Method removeEventScript.
     * 
     * @param vEventScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeEventScript(final EventScript vEventScript) {
        boolean removed = eventScriptList.remove(vEventScript);
        return removed;
    }

    /**
     * Method removeEventScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public EventScript removeEventScriptAt(final int index) {
        Object obj = this.eventScriptList.remove(index);
        return (EventScript) obj;
    }

    /**
     * Method removeReloadScript.
     * 
     * @param vReloadScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeReloadScript(final ReloadScript vReloadScript) {
        boolean removed = reloadScriptList.remove(vReloadScript);
        return removed;
    }

    /**
     * Method removeReloadScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ReloadScript removeReloadScriptAt(final int index) {
        Object obj = this.reloadScriptList.remove(index);
        return (ReloadScript) obj;
    }

    /**
     * Method removeStartScript.
     * 
     * @param vStartScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeStartScript(final StartScript vStartScript) {
        boolean removed = startScriptList.remove(vStartScript);
        return removed;
    }

    /**
     * Method removeStartScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public StartScript removeStartScriptAt(final int index) {
        Object obj = this.startScriptList.remove(index);
        return (StartScript) obj;
    }

    /**
     * Method removeStopScript.
     * 
     * @param vStopScript
     * @return true if the object was removed from the collection.
     */
    public boolean removeStopScript(final StopScript vStopScript) {
        boolean removed = stopScriptList.remove(vStopScript);
        return removed;
    }

    /**
     * Method removeStopScriptAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public StopScript removeStopScriptAt(final int index) {
        Object obj = this.stopScriptList.remove(index);
        return (StopScript) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEngine
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setEngine(final int index, final Engine vEngine) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.engineList.size()) {
            throw new IndexOutOfBoundsException("setEngine: Index value '" + index + "' not in range [0.." + (this.engineList.size() - 1) + "]");
        }
        
        this.engineList.set(index, vEngine);
    }

    /**
     * 
     * 
     * @param vEngineArray
     */
    public void setEngine(final Engine[] vEngineArray) {
        //-- copy array
        engineList.clear();
        
        for (int i = 0; i < vEngineArray.length; i++) {
                this.engineList.add(vEngineArray[i]);
        }
    }

    /**
     * Sets the value of 'engineList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vEngineList the Vector to copy.
     */
    public void setEngine(final List<Engine> vEngineList) {
        // copy vector
        this.engineList.clear();
        
        this.engineList.addAll(vEngineList);
    }

    /**
     * Sets the value of 'engineList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param engineList the Vector to set.
     */
    public void setEngineCollection(final List<Engine> engineList) {
        this.engineList = engineList;
    }

    /**
     * 
     * 
     * @param index
     * @param vEventScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setEventScript(final int index, final EventScript vEventScript) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.eventScriptList.size()) {
            throw new IndexOutOfBoundsException("setEventScript: Index value '" + index + "' not in range [0.." + (this.eventScriptList.size() - 1) + "]");
        }
        
        this.eventScriptList.set(index, vEventScript);
    }

    /**
     * 
     * 
     * @param vEventScriptArray
     */
    public void setEventScript(final EventScript[] vEventScriptArray) {
        //-- copy array
        eventScriptList.clear();
        
        for (int i = 0; i < vEventScriptArray.length; i++) {
                this.eventScriptList.add(vEventScriptArray[i]);
        }
    }

    /**
     * Sets the value of 'eventScriptList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vEventScriptList the Vector to copy.
     */
    public void setEventScript(final List<EventScript> vEventScriptList) {
        // copy vector
        this.eventScriptList.clear();
        
        this.eventScriptList.addAll(vEventScriptList);
    }

    /**
     * Sets the value of 'eventScriptList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param eventScriptList the Vector to set.
     */
    public void setEventScriptCollection(final List<EventScript> eventScriptList) {
        this.eventScriptList = eventScriptList;
    }

    /**
     * 
     * 
     * @param index
     * @param vReloadScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setReloadScript(final int index, final ReloadScript vReloadScript) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reloadScriptList.size()) {
            throw new IndexOutOfBoundsException("setReloadScript: Index value '" + index + "' not in range [0.." + (this.reloadScriptList.size() - 1) + "]");
        }
        
        this.reloadScriptList.set(index, vReloadScript);
    }

    /**
     * 
     * 
     * @param vReloadScriptArray
     */
    public void setReloadScript(final ReloadScript[] vReloadScriptArray) {
        //-- copy array
        reloadScriptList.clear();
        
        for (int i = 0; i < vReloadScriptArray.length; i++) {
                this.reloadScriptList.add(vReloadScriptArray[i]);
        }
    }

    /**
     * Sets the value of 'reloadScriptList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vReloadScriptList the Vector to copy.
     */
    public void setReloadScript(final List<ReloadScript> vReloadScriptList) {
        // copy vector
        this.reloadScriptList.clear();
        
        this.reloadScriptList.addAll(vReloadScriptList);
    }

    /**
     * Sets the value of 'reloadScriptList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param reloadScriptList the Vector to set.
     */
    public void setReloadScriptCollection(final List<ReloadScript> reloadScriptList) {
        this.reloadScriptList = reloadScriptList;
    }

    /**
     * 
     * 
     * @param index
     * @param vStartScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setStartScript(final int index, final StartScript vStartScript) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.startScriptList.size()) {
            throw new IndexOutOfBoundsException("setStartScript: Index value '" + index + "' not in range [0.." + (this.startScriptList.size() - 1) + "]");
        }
        
        this.startScriptList.set(index, vStartScript);
    }

    /**
     * 
     * 
     * @param vStartScriptArray
     */
    public void setStartScript(final StartScript[] vStartScriptArray) {
        //-- copy array
        startScriptList.clear();
        
        for (int i = 0; i < vStartScriptArray.length; i++) {
                this.startScriptList.add(vStartScriptArray[i]);
        }
    }

    /**
     * Sets the value of 'startScriptList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vStartScriptList the Vector to copy.
     */
    public void setStartScript(final List<StartScript> vStartScriptList) {
        // copy vector
        this.startScriptList.clear();
        
        this.startScriptList.addAll(vStartScriptList);
    }

    /**
     * Sets the value of 'startScriptList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param startScriptList the Vector to set.
     */
    public void setStartScriptCollection(final List<StartScript> startScriptList) {
        this.startScriptList = startScriptList;
    }

    /**
     * 
     * 
     * @param index
     * @param vStopScript
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setStopScript(final int index, final StopScript vStopScript) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.stopScriptList.size()) {
            throw new IndexOutOfBoundsException("setStopScript: Index value '" + index + "' not in range [0.." + (this.stopScriptList.size() - 1) + "]");
        }
        
        this.stopScriptList.set(index, vStopScript);
    }

    /**
     * 
     * 
     * @param vStopScriptArray
     */
    public void setStopScript(final StopScript[] vStopScriptArray) {
        //-- copy array
        stopScriptList.clear();
        
        for (int i = 0; i < vStopScriptArray.length; i++) {
                this.stopScriptList.add(vStopScriptArray[i]);
        }
    }

    /**
     * Sets the value of 'stopScriptList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vStopScriptList the Vector to copy.
     */
    public void setStopScript(final List<StopScript> vStopScriptList) {
        // copy vector
        this.stopScriptList.clear();
        
        this.stopScriptList.addAll(vStopScriptList);
    }

    /**
     * Sets the value of 'stopScriptList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param stopScriptList the Vector to set.
     */
    public void setStopScriptCollection(final List<StopScript> stopScriptList) {
        this.stopScriptList = stopScriptList;
    }

}
