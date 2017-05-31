package org.opennms.netmgt.enlinkd.snmp;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.snmp4j.smi.OctetString;

/**
 * <P>Abstract class used to walk the Q-BRIDGE-MIB::dot1qVlanStaticTable. 
 * This class defines the following inner classes :
 * <UL>
 * <li>A class to hold the PortList fields @see #Portlist</li>
 * <li>A class to hold the table rows : @see Dot1qVlanStaticRow</li>
 * </UL>
 * This class defines the following enums  :
 * <UL>
 * <li>A public enum for RowStatus @see #dot1qVlanStaticRowStatus</li>
 * <li>A private utility enum for table rows tracking @see #VlanStaticRowElement</li>
 * </UL>
 * 
 * </P>
 * 
 * @author <A HREF="mailto:jm+opennms@kubek.fr">Jean-Marie Kubek</A>
 * @see <A HREF="https://tools.ietf.org/html/rfc4363">RFC4363</A>
 * @see <A HREF="http://www.ieee802.org/1/files/public/MIBs/IEEE8021-Q-BRIDGE-MIB-200810150000Z.txt">IEEE Q-BRIDGE-MIB</A>
 *
 */
public abstract class Dot1qVlanStaticTableTracker  extends TableTracker {
	public static 
		final String DOT1Q_VLAN_STATIC_TABLE_ENTRY_OID = ".1.3.6.1.2.1.17.7.1.4.3.1";
	public static 
		final SnmpObjId DOT1Q_VLAN_STATIC_TABLE_ENTRY =  SnmpObjId.get(DOT1Q_VLAN_STATIC_TABLE_ENTRY_OID);
	private static SnmpObjId[] elemList;
	static {
		VlanStaticRowElement[] values = VlanStaticRowElement.values();
		elemList = new SnmpObjId[values.length];
		int cpt = 0;
		for (VlanStaticRowElement v : values){
			elemList[cpt++]=v.getSnmpObjId();
		}
	}




	private int rowCount;
	public Dot1qVlanStaticTableTracker() {
        super(elemList);
        rowCount = 0;
    }
	
	 @Override
	public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
	        return new Dot1qVlanStaticRow(columnCount, instance);
	}
	@Override
	public void rowCompleted(final SnmpRowResult row) {
		this.rowCount ++;
		processDot1qVlanStaticRow((Dot1qVlanStaticRow) row);
	 }



	public abstract void processDot1qVlanStaticRow(Dot1qVlanStaticRow row);
	 
	public static class PortList implements Set<Integer>{

		private byte[] value;
		private BitSet valueAsBitSet;
		public PortList(String hexString, char delimiter) {
			this(OctetString.fromHexString(hexString, delimiter));
		}
		public PortList(OctetString octetString) {
			this(octetString.toByteArray());
		}
		public PortList(byte[] octets) {
			value 	= octets;
			valueAsBitSet = new BitSet(8*octets.length);
			
			int cptBit = 0;
			for (int cptOctets = 0; cptOctets < octets.length; cptOctets++){
				byte octet = octets[cptOctets];
				for (int idxbit = 7; idxbit >= 0;idxbit--){
					valueAsBitSet.set(cptBit++, getBit(octet,idxbit)>0);
				}
			}
			
		}
		
		private int getBit(byte octet, int pos) {
			// TODO Auto-generated method stub
			 return (((byte)octet) & (0x01 << pos)) ;


		}

		public class Unimplemented extends RuntimeException{
			
		}
		
		public int getNumberofPorts(){
			// can( use BitSet size() 
			return 8*value.length;
		}
		@Override
		public int size() {
			return valueAsBitSet.cardinality();
		}
		@Override
		public boolean isEmpty() {
			return valueAsBitSet.isEmpty();
		}
		@Override
		public boolean contains(Object o) {
			if ( !(o instanceof Integer)) return false;
			return valueAsBitSet.get((int) o -1);
		}
		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer> () {
				int idx = 0;

				@Override
				public boolean hasNext() {
					idx = valueAsBitSet.nextSetBit(idx);
					return (idx >= 0);
				}

				@Override
				public Integer next() {
					return (idx++) +1;
				}
				
			};
		}
		@Override
		public Object[] toArray() {
			throw new Unimplemented();
		}
		@Override
		public Integer[] toArray(Object[] a) {
			throw new Unimplemented();
		}
		@Override
		public boolean add(Integer e) {
			throw new Unimplemented();
		}
		@Override
		public boolean remove(Object o) {
			throw new Unimplemented();
		}
		@Override
		public boolean containsAll(Collection c) {
			throw new Unimplemented();
		}
		@Override
		public boolean addAll(Collection c) {
			throw new Unimplemented();
		}
		@Override
		public boolean retainAll(Collection c) {
			throw new Unimplemented();
		}
		@Override
		public boolean removeAll(Collection c) {
			throw new Unimplemented();
		}
		@Override
		public void clear() {
			throw new Unimplemented();
		}
		
	}
	interface RowProcessor {
		public <T> T getValue(SnmpRowResult row);
	}
	enum RowStatus {
		active(1),
		notInService(2),
		notReady(3),
		createAndGo(4),
		createAndWait(5),
		destroy(6);
		
		int code;
		RowStatus(int v){
			this.code = v;
		}
	}
	
	private enum VlanStaticRowElement implements RowProcessor {
		NAME("1",String.class){
			@Override
			public String getValue(SnmpRowResult row) {
				return getRowValue(row).toDisplayString();
			}
		},
		EGRESS_PORT ("2",PortList.class){

			@Override
			public PortList getValue(SnmpRowResult row){
				return new PortList(getRowValue(row).getBytes());
			}
			
		},
		FORBIDDEN_EGRESS_PORTS ("3",PortList.class){

			@Override
			public PortList getValue(SnmpRowResult row) {
				return new PortList(getRowValue(row).getBytes());
			}
			
		},
		UNTAGGED_PORTS ("4",PortList.class){

			@Override
			public PortList getValue(SnmpRowResult row) {
				return new PortList(getRowValue(row).getBytes() );
			}
			
		},
		ROW_STATUS ("5",RowStatus.class){

			@Override
			public RowStatus getValue(SnmpRowResult row) {
				int value = getRowValue(row).toInt();
				return RowStatus.values()[value-1];
			}
		};
		private String position;
		private SnmpObjId snmpObjId;
		private Class objectClass;

		VlanStaticRowElement (String position,Class objectClass) {
			this.position  = position;
			this.snmpObjId = SnmpObjId.get(DOT1Q_VLAN_STATIC_TABLE_ENTRY,position);
			this.objectClass = objectClass;
		}
		protected SnmpValue getRowValue(SnmpRowResult row) {
			return row.getValue(this.snmpObjId);
		}
		SnmpObjId getSnmpObjId () {
			return snmpObjId;
		}
		
	}
	
	
	
	
	public static class Dot1qVlanStaticRow extends SnmpRowResult {

        public Dot1qVlanStaticRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

		public boolean isStatusOperational() {
			return RowStatus.active == VlanStaticRowElement.ROW_STATUS.getValue(this);
		}
		public RowStatus getRowStatus() {
			return VlanStaticRowElement.ROW_STATUS.getValue(this);
		}
		public Integer getVlanId() {
			return getInstance().getLastSubId();
		}

		public String getVlanName() {
			return VlanStaticRowElement.NAME.getValue(this);
		}

		public PortList getEgressPorts() {
			return VlanStaticRowElement.EGRESS_PORT.getValue(this);
		}

		public PortList getForbiddenEgressPorts() {
			return VlanStaticRowElement.FORBIDDEN_EGRESS_PORTS.getValue(this);
		}

		public PortList getUntaggedPorts() {
			return VlanStaticRowElement.UNTAGGED_PORTS.getValue(this);
		}
    }




	public int getRowCount() {
		return rowCount;
	}


}
