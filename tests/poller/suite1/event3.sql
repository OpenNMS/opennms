DELETE FROM ifServices WHERE nodeID = 3;
DELETE FROM ipInterface WHERE nodeID = 3;
DELETE FROM node WHERE nodeID = 3;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (3, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (3, '192.168.100.32', '192.168.100.32', 'A');
-- INSERT INTO service (serviceID, serviceName) VALUES (1, 'HTTP');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (3, '192.168.100.32', 1, 'A', 'P', 'N');

