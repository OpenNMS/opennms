/* XmpCollectionResource.java

   COPYRIGHT 2008 KRUPCZAK.ORG, LLC.

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
   USA
 
   For more information, visit:
   http://www.krupczak.org/
*/

/** XmpCollectionResource contains a set of AttributeGroups which
    in turn contain actual attributes or XmpVars.  Attribute groups
    closely mirror the data collection config file.
    @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
    @version $Id: XmpCollectionResource.java 38 2008-07-24 13:39:32Z rdk $
 **/

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.utils.*;
import org.opennms.netmgt.model.*;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

class XmpCollectionResource extends AbstractCollectionResource 
{
   /* class variables and methods *********************** */

   /* instance variables ******************************** */
   String nodeTypeName;
   String instance;
   String resourceType;
   int nodeType;
   Set<AttributeGroup> listOfGroups;
   CollectionAgent agent;

   /* constructors  ************************************* */
   XmpCollectionResource(CollectionAgent agent, String resourceType, String nodeTypeName, String instance) 
   {
       super(agent);

       // node type can be "node" for scalars or
       // "if" for network interface resources and
       // "*" for all other resource types
       // or anything but 'node' (e.g. table name)

       // resourceType tells us if we are writing under a separate RRD
       // subdir

       this.agent = agent;
       this.nodeTypeName = nodeTypeName;
       if ((resourceType == null) || (resourceType.isEmpty()))
	   this.resourceType = null;
       else
           this.resourceType = resourceType;
       nodeType = -1;


       // filter the instance so it does not have slashes (/) nor colons 
       // in it as they can munge our rrd file layout
       
       if (instance != null) {
	   this.instance = instance.replace('/','_');
           this.instance = this.instance.replace('\\','_');
           this.instance = this.instance.replace(':','_');
       }
       else {
           this.instance = instance;
       }

       listOfGroups = new HashSet<AttributeGroup>();
   }

   /* private methods *********************************** */
   private Category log() {
       return ThreadCategory.getInstance(getClass());
   }

   /* public methods ************************************ */

   // get the location where we are supposed to write our data to
   public File getResourceDir(RrdRepository repository)
   {

        // if we are a collection resource for scalars,
        // return what our super class would return

       if (nodeTypeName.equalsIgnoreCase("node")) {
          return new File(repository.getRrdBaseDir(), Integer.toString(agent.getNodeId()));
       }
 
       // we are a collection resource for tabular data
       // return essentially share/rrd/snmp/NodeId/resourceType/instance
       // for now; the problem with using key/instance is that
       // it can change for some tables (e.g. proc table)
       // whoever instantiates this object is responsible for
       // passing in an instance that will be unique;
       // if we want a specific instance of a table, we will use
       // the instance/key that was used for the query; if not,
       // we will use the key returned per table row

       File instDir, rtDir;

       File rrdBaseDir = repository.getRrdBaseDir();
       File nodeDir = new File(rrdBaseDir,String.valueOf(agent.getNodeId()));

       // if we have a resourceType, put instances under it
       if (resourceType != null) {
          rtDir = new File(nodeDir,resourceType);
          instDir = new File(rtDir,instance);
       }
       else {
          instDir = new File(nodeDir,instance);
       }

       return instDir;
   }

   public void addAttributeGroup(AttributeGroup aGroup)  
   {  
       listOfGroups.add(aGroup);
   }

   public String getInstance()
   {
       // for node level resources, no instance
       return instance;
   }

   public void setInstance(String instance) { this.instance = instance; }

   public String getResourceTypeName() { return nodeTypeName; };

   public void setResourceTypeName(String nodeTypeName) { this.nodeTypeName = nodeTypeName; }

   // return -1 for non-tabular; what do we return for 
   // for interface or tabular data?

   public int getType() { return nodeType; }
   public void setType(int nodeType) { this.nodeType = nodeType; }

   public boolean rescanNeeded() { return false; }
   public boolean shouldPersist(ServiceParameters params) { return true; }

   public Collection<AttributeGroup>getGroups() { return listOfGroups; }

   public String toString() { return "XmpCollectionResource for "+agent+" resType="+resourceType+" instance="+instance+" nodeType="+nodeTypeName+" nodeType="+nodeType; }

   public void visit(CollectionSetVisitor visitor) 
   { 
       log().debug("XmpCollectionResource: visit starting with "+ getGroups().size()+" attribute groups");

       visitor.visitResource(this);

       // visit the attribute groups one at a time
       for (AttributeGroup ag: getGroups()) {
           ag.visit(visitor);
       }

       visitor.completeResource(this);

       log().debug("XmpCollectionResource: visit finished for "+agent);

   } /* visit */


} /* class XmpCollectionResource */
