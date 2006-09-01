-- Refactoring DB with IpInterface IDs

CREATE OR REPLACE FUNCTION setIpInterfaceKeysOnUpdate() RETURNS trigger AS '
BEGIN

  --
  -- (Used with Trigger Update with old style foreign key)
  -- This condition keeps ipinterfaceid inSync with the composite foreign key of nodeid, ipaddr, ifindex
  -- This usually happens when a record is being updated by old JDBC code (non-Hibernate DAOs) and has changed
  -- one or more of the composite key values, the ipInterfaceId needs to be updated
  --
  IF (NEW.ipInterfaceId = OLD.ipInterfaceId) AND (NEW.nodeId != OLD.nodeId OR NEW.ipAddr != OLD.ipAddr OR NEW.ifIndex != OLD.ifIndex) 
  THEN
     SELECT ipif.id INTO NEW.ipInterfaceId
       FROM ipinterface ifsvc
       WHERE (ipif.nodeid = NEW.nodeid AND ipif.ipAddr = NEW.ipAddr AND ipif.ifIndex = NEW.ifIndex);
       
     IF NOT FOUND THEN
        RAISE EXCEPTION ''IfServices Trigger Exception, Condition 3: No IpInterface found for... nodeid: %  ipaddr: %  ifindex: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     ELSE
        RAISE NOTICE ''IfServices Trigger Success, Condition 3: IpInterface found for... nodeid: %  ipaddr: %  serviceid: %'', NEW.nodeid, NEW.ipAddr, NEW.ifIndex;
     END IF;
     
  --
  -- (Used with Trigger Update with new style foreign key)
  -- This condition keeps the composite foreign key of nodeid, ipaddr, ifindex inSync with the ipinterfaceid
  -- This usually happens with the Hibernate DAOs decide to change the ipinterfaceid represented
  -- by the ifservices.
  --
  ELSIF NEW.ipInterfaceId != OLD.ipInterfaceId
  THEN
     SELECT ipif.nodeId, ipif.ipAddr, ipif.ifIndex INTO NEW.nodeId, NEW.ipAddr, NEW.ifIndex
       FROM ipinterface ipif
      WHERE (ipif.id = NEW.ipInterfaceId);
      
      IF NOT FOUND THEN
         RAISE EXCEPTION ''IfServices Trigger Exception, Condition 4: No IpInterface found for ipInterfaceId: %'', NEW.ipInterfaceId;
      ELSE
         RAISE NOTICE ''IfServices Trigger Success, Condition 4: IpInterface found for ipIntefaceId: %'', NEW.ipInterfaceId;
      END IF;
  END IF;

  RETURN NEW;
END;
' LANGUAGE 'plpgsql';
