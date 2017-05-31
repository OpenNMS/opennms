package org.opennms.netmgt.enlinkd.snmp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>After walking the Q-BRIDGE-MIB::dot1qVlanCurrentTable Dot1qVlanCurrentTableTracker
 * holds the following maps :
 * <UL>
 * <LI>a map from vlanid to table content, @see getVlanMap()</LI>
 * <LI>a map from forwarding database id to vlan id, @see #getFdgId2VlanIdMap(), 
 * <P>this map may be unusable if many vlans are associated to the same forwarding 
 *    database @see #isFdgId2vlanIdMapUsable()</P>
 * </LI>
 * </UL>
 * 
 * </P>
 * <P><B>Remarks</B> 
 * <UL>
 * <LI>portlist fields (dot1qVlanCurrentEgressPorts, dot1qVlanCurrentUntaggedPorts) are not 
 * processed in this version</LI>
 * <LI>Handling results of the walk is non trivial as this table uses TimeFilter (from RMON2) 
 * feature. It then include a callback mechanism to help in testing, 
 * @see setEntryCreationCallback(CreationCallback callback). 
 * For an example of use @see NMS7758En#testVlanCurrentUsable 
 * </LI>
 * 
 * </UL>
 * <P>
 * @author <A HREF="mailto:jm+opennms@kubek.fr">Jean-Marie Kubek</A>
 * @see <A HREF="https://tools.ietf.org/html/rfc4363">RFC4363</A>
 * @see <A HREF="http://www.ieee802.org/1/files/public/MIBs/IEEE8021-Q-BRIDGE-MIB-200810150000Z.txt">IEEE Q-BRIDGE-MIB</A>
 *
 */
public  class Dot1qVlanCurrentTableTracker extends TableTracker{

	private static final Logger LOG = LoggerFactory.getLogger(Dot1qVlanCurrentTableTracker.class);
	
	/**
	  	dot1qVlanCurrentTable OBJECT-TYPE
    	SYNTAX      SEQUENCE OF Dot1qVlanCurrentEntry
    	MAX-ACCESS  not-accessible
    	STATUS      current
    	DESCRIPTION
        	"A table containing current configuration information
        	for each VLAN currently configured into the device by
        	(local or network) management, or dynamically created
        	as a result of GVRP requests received."
    	::= { dot1qVlan 2 }
	 */
	private static final String DOT1Q_VLAN_CURRENT_TABLE = ".1.3.6.1.2.1.17.7.1.4.2";
	
	/**
	 * m_timeFilter holds the time mark (not used at the moment) 
	 */
	private Integer m_timeFilter = 0;
	
	public Dot1qVlanCurrentTableTracker(){
		this(0);
	}
	
	public  Dot1qVlanCurrentTableTracker(Integer timeFilter){
		super(SnmpObjId.get(DOT1Q_VLAN_CURRENT_TABLE));
		this.m_timeFilter = timeFilter;
		if (timeFilter != 0) {
			LOG.warn("TimeMark Filtering is not implemented, will walk the whole table");
		}
	}
	/*
	dot1qVlanStatus OBJECT-TYPE
    SYNTAX      INTEGER {
                    other(1),
                    permanent(2),
                    dynamicGvrp(3)
                }
    MAX-ACCESS  read-only
    STATUS      current
    DESCRIPTION
        "This object indicates the status of this entry.
            other(1) - this entry is currently in use, but the
                conditions under which it will remain so differ
                from the following values.
            permanent(2) - this entry, corresponding to an entry
                in dot1qVlanStaticTable, is currently in use and
                will remain so after the next reset of the
                device.  The port lists for this entry include
                ports from the equivalent dot1qVlanStaticTable
                entry and ports learned dynamically.
            dynamicGvrp(3) - this entry is currently in use

                and will remain so until removed by GVRP.  There
                is no static entry for this VLAN, and it will be
                removed when the last port leaves the VLAN."
    ::= { dot1qVlanCurrentEntry 6 }
	*/
	enum Dot1qVlanStatus {
		other(1),
		permanent(2),
		dynamicGvrp(3);
		private int intValue;
		private static Dot1qVlanStatus[] int2EnumMap = {
				other,
				permanent,
				dynamicGvrp
		};
		static Dot1qVlanStatus makeStatus(int intValue){
			if ((intValue < 1) ||  (intValue > 3)) 
				throw new IndexOutOfBoundsException("Dot1qVlanStatus conversion : value should be between 1 and 3");
			return int2EnumMap[intValue-1];
		}
		Dot1qVlanStatus(int aValue){
			intValue = aValue;
		}
		int getValue(){
			return intValue;
		}
	}
	
	

	private Map<Integer,Dot1QVLanCurrent> vlanMap = new HashMap<>();
	private Map<Integer,Integer> fdgId2vlanIdMap = new HashMap<>();
	
	
	/**
	 * Transient utility map used when processing rows
	 */
	private Map<SnmpObjId,Dot1QVLanCurrent> rows = new HashMap<>();

	private boolean m_inconsistentFdgId2vlanIdMap = false;
	
	/**
	 * @return true if {@link #getFdgId2VlanIdMap()} is usable, ie if
	 * multiple vlan are not associated to the same fdbid
	 */
	public boolean isFdgId2vlanIdMapUsable(){
		return !m_inconsistentFdgId2vlanIdMap;
	}
	/**
	 * @return mapping from the tracked map  from fdbid to vlanid 
	 */
	public Map<Integer, Integer> getFdgId2VlanIdMap() {
		return Collections.unmodifiableMap(fdgId2vlanIdMap);
	}
	
	/**
	 * @return mapping from  vlanid  to  Dot1QVLanCurrent (@see #Dot1QVLanCurrent)
	 */
	public Map<Integer, Dot1QVLanCurrent> getVlanMap() {
		return Collections.unmodifiableMap(vlanMap);
	}
	
	
	/** {@inheritDoc} */
    @Override
	public void rowCompleted(final SnmpRowResult row) {
        SnmpInstId instance = row.getInstance();
        int vlanId = instance.getLastSubId();
        int [] rowId ={
        		instance.getSubIdAt(instance.length()-2),
        		vlanId
        };
        SnmpObjId rowInstanceId = SnmpObjId.get(rowId);
        Dot1QVLanCurrent dot1QVLanCurrent = rows.get(rowInstanceId);
        if (dot1QVLanCurrent == null){
        	dot1QVLanCurrent = createDot1QVLanCurrent(vlanId) ;
        	rows.put(rowInstanceId,dot1QVLanCurrent);
        }
        
        vlanMap.put(vlanId, dot1QVLanCurrent);
        int entryValueIndex = instance.getSubIdAt(1);
        for (SnmpResult r: row.getResults()){
        	SnmpObjId absoluteInstanceId=r.getAbsoluteInstance();
        	SnmpValue value = r.getValue();
        	if (value != null){
        		LOG.info("oid {} value: {]", absoluteInstanceId, value);
        		processMap[entryValueIndex].process(dot1QVLanCurrent,value);
        	} else {
        		LOG.warn("value for oid {} is null", absoluteInstanceId);
        	}
        }
    }
	
	private Dot1QVLanCurrent createDot1QVLanCurrent(int vlanId) {
		Dot1QVLanCurrent result = new Dot1QVLanCurrent(vlanId);
		m_creationCallback.callback(result);
		return  result;
	}

	/**
	 * Processor interface for row entry processing
	 *
	 */
	@FunctionalInterface
	private static interface Processor{
		void process(Dot1QVLanCurrent vlanCurrent, SnmpValue value);
	}
	/**
	 * Processing for returned rows data entries
	 *
	 */
	private Processor[] processMap ={
			//entrydataindex can't be in 0..2
			(vlanCurrent,value) ->  canNotHappen(0),
			(vlanCurrent,value) ->  canNotHappen(1),
			(vlanCurrent,value) ->  canNotHappen(2),
			//entrydataindex : 3 
			(vlanCurrent,value) -> {
				Integer previous = fdgId2vlanIdMap.get(value.toInt());
				if (previous != null){
					LOG.warn("multiple values for fdbid, map from fbid to vlan will be inconsistent");
					m_inconsistentFdgId2vlanIdMap = true;
				}
				fdgId2vlanIdMap.put(value.toInt(),vlanCurrent.getVlanId()); 
			}, 
			//entrydataindex : 4
			(vlanCurrent,value) -> {unimplemented("Egressport");}, //EgressPort
			//entrydataindex : 5
			(vlanCurrent,value) -> {unimplemented("UntaggedPorts");},
			//entrydataindex : 6
			(vlanCurrent,value) -> vlanCurrent.setVlanStatus(value.toInt()),
			//entrydataindex : 7
			(vlanCurrent,value) -> {vlanCurrent.setCreationTime(value.toInt());},
		
	};

	@FunctionalInterface
	public interface CreationCallback{
		void callback(Dot1QVLanCurrent entry);
	}

	private CreationCallback m_creationCallback = entry -> {};
	public void setEntryCreationCallback(CreationCallback callback){
		m_creationCallback = callback;
		
	}
	public static class Dot1QVLanCurrent {
		private int m_vlanId;
		private Dot1qVlanStatus m_vlanStatus;
		
		private int m_creationTime;
		private Dot1QVLanCurrent(){
			super();
		}
		public Dot1QVLanCurrent(int vlanId) {
			this();
			this.m_vlanId = vlanId;
		}

		public Integer getVlanId() {
			return m_vlanId;
			
		}
		public void setVlanStatus(int i) {
			 this.m_vlanStatus= Dot1qVlanStatus.makeStatus(i);
		}
		
		public Dot1qVlanStatus getVlanStatus(){
			return this.m_vlanStatus;
		}
		
		public void setCreationTime(int timeTick) {
			 this.m_creationTime= timeTick;
			
		}
		
		public int getCreationTime(){
			return this.m_creationTime;
		}
		

		
		
		
		@Override
		public String toString(){
			StringBuilder sb  = new StringBuilder("{Dot1QVLanCurrent")
					.append("{vlanId: ").append(m_vlanId).append("},")
					.append("{vlanStatus: ") .append(m_vlanStatus).append("},")
					.append("{creationTime: ") .append(m_creationTime).append("},")
					.append('}');
			return sb.toString();
		}
	}
	
	
 
    /**
     * Utility method to log a message if a field of the
     * table is not processed
     * @see #processMap
     * 
     * @param  unimplementedField unimplemented field
     */
    private void unimplemented(String unimplementedField) {
    	LOG.info("{} is Unimplemented",unimplementedField);
		
	}
    /**
     * Utility method logging a message and  throwinq an exception when 
     * an unsupported index is returned when mrocessing the snmp table
     * @see #processMap
     * @param  unimplementedField unimplemented field
     */
	private Object canNotHappen(int entryDataIndex) {
		LOG.error("walk returned {} as entry index, this should not  happen",entryDataIndex);
		 throw new RuntimeException("Cannot happen");
	}
	
	
}
