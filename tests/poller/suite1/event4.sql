DELETE FROM ifServices WHERE nodeID = 4;
DELETE FROM ipInterface WHERE nodeID = 4;
DELETE FROM node WHERE nodeID = 4;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (4, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (4, '192.168.101.4', '192.168.101.4', 'A');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (4, '192.168.101.4', 1, 'A', 'P', 'N');

