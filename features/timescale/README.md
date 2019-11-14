# set up test

## Timescale 
* set password for postgres user in nano target/opennms-26.0.0-SNAPSHOT/etc/opennms-datasources.xml to ``password`
* run timescale:
  ``sudo docker run -p 5432:5432 -e POSTGRES_PASSWORD=password timescale/timescaledb:latest-pg11|run timescale docker container``
* make sure the timescale plugin ist installed: ``select * from pg_extension;``
* create opennms schema: ``sudo <opennms_home>/bin/install -dis``
* create actual table:
``CREATE TABLE timeseries(
time       TIMESTAMPTZ      NOT NULL,
                context    TEXT              NOT NULL,
                resource   TEXT              NOT  NULL,
                name       TEXT              NOT  NULL,
                type       TEXT              NOT  NULL,
                value      DOUBLE PRECISION  NULL)``
                
* turn table into a timescale table:
  ``create_hypertable('timeseries', 'time');``

## Newts / cassandra
* set ```org.opennms.timeseries.strategy=newts``` in opennms.properties
* start cassandra docker container:
* init newts: ``sudo ./bin/newts init``



        String sql = "INSERT INTO timeseries('time', 'context', 'resource', 'name', 'type', 'value')  values (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = this.dataSource.getConnection().prepareStatement(sql);
        
        for (Sample sample: batch) {
                                ps.setDate(1, new Date(sample.getTimestamp().asMillis()));
                                ps.setString(2, sample.getContext().getId());
                                ps.setString(3, sample.getResource().getId());
                                ps.setString(3, sample.getName());
                                ps.setByte(4, sample.getType().getCode());
                                ps.setDouble(5, sample.getValue().doubleValue());
                                ps.addBatch();
                            }
                            ps.executeBatch();
