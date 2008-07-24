/* XmpCollectionAttribute.java

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

/** 
   XmpCollectionAttribute is an actual data point collected via XMP;
   what this means in English is that we've finally arrived at an
   actual Xmp variable -- something that has a MIB object name, type,
   and value.
   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
   @version $Id: XmpCollectionAttribute.java 38 2008-07-24 13:39:32Z rdk $
 **/

package org.opennms.netmgt.collectd;

import org.krupczak.Xmp.*;
import org.opennms.netmgt.utils.*;
import org.opennms.netmgt.model.*;

public class XmpCollectionAttribute extends AbstractCollectionAttribute 
                                    implements CollectionAttribute 
{
   /* class variables and methods *********************** */

   /* instance variables ******************************** */
   private XmpVar aVar;
   private String alias;
   private XmpCollectionResource resource;
   private CollectionAttributeType attribType;

   /* constructors  ************************************* */
   XmpCollectionAttribute() { aVar = null; }

   XmpCollectionAttribute(XmpVar aVar, String alias, XmpCollectionResource res)
   {
       this.aVar = aVar;
       this.alias = alias;
       this.resource = res;
   }

   XmpCollectionAttribute(XmpCollectionResource res, 
                          CollectionAttributeType attribType, 
                          String alias, XmpVar aVar) 
   {
       this(aVar,alias,res);
       this.attribType = attribType;
   }

   /* private methods *********************************** */

   /* public methods ************************************ */

   public CollectionAttributeType getAttributeType() { return attribType; }

   public void setAttributeType(CollectionAttributeType attribType)
   {
       this.attribType = attribType;
   }

   public String getName() 
   { 
       return new String(alias);
   }

   public CollectionResource getResource() { return resource; }

   public String getNumericValue() { return aVar.getValue(); }

   public String getType() { return Xmp.syntaxToString(aVar.xmpSyntax); }

   public boolean shouldPersist(ServiceParameters params) { return true; }

   //public void visit(CollectionSetVisitor visitor) { super(visitor); }

   public String toString() 
   { 
        return "XmpCollectionAttribute "+alias+"="+aVar.getValue()+" attribType="+attribType; 
   }

    public String getStringValue() { return aVar.getValue(); }

}
