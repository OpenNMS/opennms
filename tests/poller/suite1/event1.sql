DELETE FROM ifServices WHERE nodeID = 1;
DELETE FROM ipInterface WHERE nodeID = 1;
DELETE FROM node WHERE nodeID = 1;
DELETE FROM service WHERE serviceID = 1;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (1, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (1, '192.168.100.30', '192.168.100.30', 'A');
INSERT INTO service (serviceID, serviceName) VALUES (1, 'HTTP');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (1, '192.168.100.30', 1, 'A', 'P', 'N');

