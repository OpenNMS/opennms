/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.tools;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.cassandraunit.JUnitNewtsCassandra;
import org.opennms.core.test.db.TemporaryDatabasePostgreSQL;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.cassandraunit.JUnitNewtsCassandraExecutionListener;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.newts.support.NewtsUtils;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.StandardAggregationFunctions;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({JUnitNewtsCassandraExecutionListener.class,
                         TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations={"classpath:/META-INF/opennms/applicationContext-soa.xml",
                                 "classpath:/META-INF/opennms/applicationContext-newts.xml"})
@JUnitConfigurationEnvironment(systemProperties={"org.opennms.newts.config.hostname=" + NewtsConverterIT.CASSANDRA_HOST,
                                                 "org.opennms.newts.config.port=" + NewtsConverterIT.CASSANDRA_PORT,
                                                 "org.opennms.newts.config.keyspace=" + NewtsConverterIT.CASSANDRA_KEYSPACE,
                                                 "org.opennms.newts.config.max_batch_delay=0", // No delay
                                                 "org.opennms.timeseries.strategy=newts"})
@JUnitTemporaryDatabase()
@JUnitNewtsCassandra(host=NewtsConverterIT.CASSANDRA_HOST,
                     port=NewtsConverterIT.CASSANDRA_PORT,
                     keyspace=NewtsConverterIT.CASSANDRA_KEYSPACE)
public class NewtsConverterIT implements TemporaryDatabaseAware {
    protected final static String CASSANDRA_HOST = "localhost";
    protected final static int CASSANDRA_PORT = 9043;
    protected final static String CASSANDRA_KEYSPACE = "newts";

    private final static Path OPENNMS_HOME;

    private final static ResourcePath RESOURCE_PATH_SNMP = ResourcePath.get("snmp", "fs", "fs1", "fid1", "eth0-04013f75f101");
    private final static ResourcePath RESOURCE_PATH_RESPONSE = ResourcePath.get("response", "127.0.0.1");

    private final static double[] EXPECTED_DATA = {
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            5.5316938325e+03,
            4.4558461260e+03,
            6.3517586932e+02,
            2.7410224206e+02,
            2.7280721253e+02,
            2.7273986157e+02,
            2.7278715024e+02,
            2.7456559109e+02,
            2.7308853129e+02,
            2.7189681801e+02,
            5.2324057817e+02,
            1.4925738483e+03,
            5.7305671138e+02,
            2.1595512362e+03,
            5.0587548168e+03,
            3.3732252122e+02,
            2.7288883074e+02,
            2.7447826459e+02,
            2.7336833749e+02,
            2.7340830934e+02,
            2.7263246263e+02,
            2.7357742756e+02,
            2.8086905177e+02,
            4.2287544296e+02,
            3.2873004391e+02,
            3.2161006091e+02,
            2.9931773856e+02,
            2.7467561831e+02,
            2.7425790928e+02,
            2.7641772056e+02,
            2.7554827519e+02,
            2.7678948598e+02,
            2.7448604467e+02,
            2.7452287790e+02,
            2.7414460317e+02,
            2.7623911268e+02,
            2.7680193983e+02,
            2.7415346484e+02,
            2.7359105574e+02,
            2.7188462731e+02,
            2.7261512306e+02,
            3.1062198736e+02,
            2.7947495155e+02,
            2.7281952612e+02,
            2.7250835133e+02,
            2.7260627307e+02,
            2.7213984219e+02,
            2.7411375923e+02,
            2.8355176080e+02,
            2.9142911130e+02,
            2.9936599575e+02,
            2.7009306478e+02,
            2.7018613603e+02,
            2.6996305140e+02,
            2.6887677464e+02,
            2.6920612680e+02,
            3.0223889535e+02,
            2.7077449244e+02,
            3.6596435909e+02,
            1.0490177076e+03,
            1.7225255108e+03,
            1.1222423837e+03,
            6.9114940661e+02,
            5.4128442386e+02,
            2.7867993106e+02,
            3.2413830334e+02,
            2.6827577427e+02,
            2.6808875092e+02,
            2.6819264489e+02,
            2.7958538391e+02,
            2.6882247416e+02,
            3.5104155685e+02,
            3.5355874077e+02,
            3.3148389350e+02,
            3.2746785391e+02,
            3.9107831364e+02,
            2.7208733327e+02,
            3.0782434385e+02,
            2.7526000636e+02,
            2.7111099816e+02,
            2.7132451551e+02,
            2.6899793973e+02,
            2.6952927926e+02,
            5.1942586471e+02,
            5.0849348330e+02,
            6.9324320275e+02,
            2.7588323374e+04,
            9.0941996468e+03,
            1.9762561303e+03,
            2.6857894255e+02,
            2.7149411268e+02,
            2.6935192321e+02,
            2.6887000923e+02,
            2.7064613326e+02,
            3.0846741048e+02,
            8.7681426542e+02,
            2.2427481963e+03,
            5.3947730528e+02,
            1.1348433688e+03,
            2.6693579144e+03,
            2.7370118079e+02,
            2.7044728821e+02,
            2.7005971345e+02,
            2.6952376233e+02,
            2.7277581363e+02,
            2.9068759874e+02,
            2.7184585409e+02,
            2.9073369048e+02,
            2.9559432009e+02,
            3.4372934025e+02,
            3.0185411268e+02,
            2.6961798680e+02,
            2.6968850867e+02,
            2.6889033914e+02,
            2.7016508536e+02,
            2.7032965301e+02,
            2.7828334164e+02,
            2.7244995062e+02,
            2.7012529854e+02,
            3.1494049558e+02,
            3.2374971299e+02,
            2.7367974160e+02,
            2.6973690892e+02,
            2.7194061231e+02,
            2.6650917359e+02,
            2.6741053110e+02,
            2.6805489895e+02,
            2.7022015780e+02,
            2.7038548634e+02,
            2.7028007623e+02,
            2.7088393818e+02,
            2.7060961933e+02,
            2.7421916436e+02,
            2.7385593762e+02,
            2.7535481451e+02,
            2.8976133121e+02,
            2.7140274502e+02,
            2.6964664267e+02,
            2.6992278701e+02,
            2.6730472084e+02,
            2.6065593807e+02,
            2.3995965808e+02,
            2.8477827843e+02,
            6.6192565614e+02,
            2.0276783609e+03,
            6.0407627181e+02,
            6.3714661361e+02,
            3.2839664129e+02,
            2.6911763704e+02,
            2.6715324567e+02,
            2.6933577566e+02,
            2.6904923035e+02,
            2.6871590613e+02,
            2.6957139213e+03,
            2.6952777269e+02,
            2.8727689398e+02,
            2.9027326898e+02,
            2.9231315525e+02,
            2.9903550283e+02,
            2.6922522853e+02,
            2.6772670081e+02,
            2.7004964793e+02,
            2.6871501154e+02,
            2.6987314046e+02,
            2.8093311600e+02,
            2.7009802418e+02,
            2.7243157623e+02,
            7.3487711286e+02,
            6.5557811139e+02,
            7.9462980066e+02,
            4.4923888644e+03,
            4.1066188867e+02,
            2.8807364791e+02,
            2.7857335883e+02,
            2.6768370321e+02,
            2.6738421992e+02,
            2.6816400471e+02,
            2.6966134575e+02,
            2.8282567945e+02,
            6.7927930758e+02,
            1.4263808917e+03,
            8.7927072314e+02,
            8.2899517130e+02,
            3.2607949462e+02,
            2.7476369428e+02,
            2.6980165641e+02,
            2.6897105712e+02,
            2.8072824658e+02,
            3.0479537698e+02,
            2.7206614253e+02,
            2.6963110463e+02,
            5.3851540648e+02,
            4.3740010371e+02,
            3.9887997222e+02,
            3.0716291810e+03,
            2.7672273504e+02,
            2.6954695662e+02,
            2.7002708472e+02,
            2.6771633518e+02,
            2.6886198135e+02,
            2.7002647656e+02,
            2.7020114203e+02,
            2.6929119682e+02,
            2.6905031250e+02,
            2.6915197917e+02,
            2.7247931019e+02,
            2.7844052866e+02,
            2.7008651532e+02,
            2.6917012182e+02,
            2.8456125507e+02,
            2.9308841663e+02,
            2.9418504590e+02,
            2.9719444952e+02,
            2.9429051614e+02,
            2.9090260770e+02,
            2.9785124461e+02,
            3.0380809877e+02,
            3.0596696705e+02,
            2.9429025563e+02,
            2.9149208679e+02,
            2.9943816422e+02,
            2.9100242386e+02,
            2.9277719996e+02,
            2.8972932685e+02,
            2.8983784723e+02,
            2.9088990046e+02,
            2.8232523287e+02,
            6.5366218241e+02,
            2.5634133435e+03,
            9.5230869954e+04,
            3.1785005490e+03,
            4.3932426736e+02,
            2.6939156423e+02,
            2.6608865310e+02,
            2.6871467423e+02,
            2.8594697490e+02,
            2.7996461656e+02,
            2.6946948735e+02,
            2.7002022241e+02,
            3.1354951504e+02,
            3.3274639997e+02,
            3.0942117438e+02,
            2.1719598091e+04,
            2.8622237772e+02,
            2.7033617202e+02,
            2.6911671327e+02,
            3.0363950120e+02,
            2.6909900148e+02,
            2.6925868401e+02,
            2.6859146964e+02,
            2.6981611850e+02,
            5.1300912606e+02,
            5.5392612198e+02,
            1.1016777571e+03,
            2.7558568070e+04,
            3.3491936970e+02,
            2.7283034284e+02,
            3.2632155915e+02,
            3.6761463363e+02,
            3.1226599249e+02,
            2.9734590166e+02,
            2.7594522749e+02,
            3.0234374630e+02,
            5.1033056247e+02,
            5.6555211609e+02,
            8.6186348286e+02,
            2.8900856326e+04,
            3.6252064802e+03,
            2.7532952704e+02,
            2.7508677187e+02,
            2.7429446291e+02,
            2.7652502308e+02,
            3.0665791621e+02,
            2.7685923035e+02,
            2.7248175572e+02,
            5.2169414821e+02,
            4.8298882891e+02,
            2.7233791298e+02,
            2.8210738834e+02,
            2.8881260567e+02,
            3.2071295450e+02,
            2.8363554771e+02,
            2.8044872001e+02,
            2.7498342562e+02,
            2.7563841085e+02,
            3.0328089147e+02,
            3.0930172065e+02,
            2.7405428849e+02,
            2.7070697028e+02,
            2.7943635613e+02,
            3.1935655823e+02,
            2.8136144196e+02,
            2.7158279162e+02,
            2.7222463133e+02,
            2.5616411868e+02,
            2.4881907069e+02,
            2.0324130306e+02,
            2.0743992202e+02,
            2.0457201181e+02,
            2.0570502030e+02,
            2.0324148487e+02,
            2.5020869694e+02,
            2.2458983343e+02,
            2.0119693014e+02,
            2.0262421558e+02,
            2.0286971669e+02,
            2.0146533684e+02,
            2.4319836194e+02,
            2.2956466324e+02,
            2.0626040136e+02,
            2.0695304079e+02,
            1.1151947725e+03,
            1.1300013552e+03,
            4.7270847499e+02,
            7.1224797434e+02,
            3.7775863141e+02,
            3.0574320460e+02,
            3.3834625000e+02,
            3.2461974945e+02,
            3.4806653516e+02,
            3.1299157254e+02,
            3.0940358573e+02,
            3.0474407992e+02,
            4.8041310447e+02,
            8.1786879660e+02,
            4.7117261535e+02,
            9.5992732327e+02,
            5.8172881460e+02,
            2.7335553512e+02,
            2.7276474081e+02,
            2.7654777409e+02,
            2.8605296788e+02,
            2.9177477298e+02,
            2.9778315892e+02,
            2.7946530823e+02,
            8.6547011294e+04,
            3.3276873057e+05,
            7.9481522400e+02,
            8.4201765852e+02,
            5.3342102532e+02,
            Double.NaN,
    };

    static {
        try {
            OPENNMS_HOME = Paths.get(NewtsConverterIT.class.getResource("/opennms-home").toURI());

        } catch (final URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    private TemporaryDatabase database;

    @Autowired
    private SampleRepository repository;

    @Autowired
    private ResourceStorageDao resourceStorageDao;

    private static boolean populated = false;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.database = database;
    }

    @Before
    public void setupDatabase() throws Exception {
        if (!NewtsConverterIT.populated) {
            this.database.getJdbcTemplate().execute("INSERT INTO node (nodeid, nodecreatetime, nodelabel, foreignsource, foreignid) " +
                                               "VALUES (1, NOW(), 'my-node-1', 'fs1', 'fid1')");


            try (final BufferedReader r = Files.newBufferedReader(OPENNMS_HOME.resolve("etc")
                                                                         .resolve("opennms-datasources.xml.template"));
                 final BufferedWriter w = Files.newBufferedWriter(OPENNMS_HOME.resolve("etc")
                                                                              .resolve("opennms-datasources.xml"))) {
                String line;
                while ((line = r.readLine()) != null) {
                    w.append(line.replace("%%DATABASE%%", this.database.getTestDatabase())
                                 .replace("%%USERNAME%%", ((org.opennms.core.db.install.SimpleDataSource) ((TemporaryDatabasePostgreSQL) this.database).getDataSource()).getUser())
                                 .replace("%%PASSWORD%%", ((org.opennms.core.db.install.SimpleDataSource) ((TemporaryDatabasePostgreSQL) this.database).getDataSource()).getPassword()))
                     .append('\n');
                }
            }

            NewtsConverterIT.populated = true;
        }
    }

    private void execute(final boolean storeByGroup,
                         final boolean storeByForeignSource,
                         final boolean useRrdTool) throws Exception {
        final Path data = OPENNMS_HOME.resolve("share")
                                      .resolve(useRrdTool ? "rrd" : "jrb")
                                      .resolve(storeByGroup ? "sbg" : "sbm")
                                      .resolve(storeByForeignSource ? "fs" : "id");

        System.out.println(data.toAbsolutePath());
        assertTrue(Files.isDirectory(data));

        NewtsConverter.main("-o", OPENNMS_HOME.toString(),
                            "-r", data.toString(),
                            "-t", Boolean.toString(useRrdTool),
                            "-s", Boolean.toString(storeByGroup));

        assertThat(resourceStorageDao.exists(RESOURCE_PATH_SNMP, 0), is(true));
        assertThat(resourceStorageDao.getAttributes(RESOURCE_PATH_SNMP),
                   hasItems(allOf(hasProperty("name", is("ifInOctets"))),
                            allOf(hasProperty("name", is("ifSpeed")),
                                  hasProperty("value", is("1000")))));

        assertThat(resourceStorageDao.exists(RESOURCE_PATH_RESPONSE, 0), is(true));
        assertThat(resourceStorageDao.getAttributes(RESOURCE_PATH_RESPONSE),
                   hasItems(allOf(hasProperty("name", is("icmp")))));

//        for (final Results.Row<Sample> r : repository.select(Context.DEFAULT_CONTEXT,
//                                                              new Resource(NewtsUtils.toResourceId(ResourcePath.get(RESOURCE_PATH_SNMP, "mib2-interfaces"))),
//                                                              Optional.of(Timestamp.fromEpochSeconds(1414504800)),
//                                                              Optional.of(Timestamp.fromEpochSeconds(1417047045)))) {
//                System.out.println("111 " + r.getElement("ifInOctets"));
//        }

        final Results<Measurement> result = repository.select(Context.DEFAULT_CONTEXT,
                                                              new Resource(NewtsUtils.toResourceId(ResourcePath.get(RESOURCE_PATH_SNMP, "mib2-interfaces"))),
                                                              Optional.of(Timestamp.fromEpochSeconds(1414504800)),
                                                              Optional.of(Timestamp.fromEpochSeconds(1417047045)),
                                                              new ResultDescriptor(Duration.seconds(3600))
                                                                      .datasource("ifInOctets", StandardAggregationFunctions.AVERAGE)
                                                                      .export("ifInOctets"),
                                                              Optional.of(Duration.seconds(7200)));

        assertThat(result.getRows().size(), is(EXPECTED_DATA.length));

        int i = 0;
        for (Results.Row<Measurement> r : result) {
            final double d = r.getElement("ifInOctets").getValue().doubleValue() - EXPECTED_DATA[i];

            System.out.println(String.format(
                    "%s\t%11.2f %11.2f %11.2f %36s|%s",
                    r.getElement("ifInOctets").getTimestamp().asDate().toString(),
                    r.getElement("ifInOctets").getValue().doubleValue(),
                    EXPECTED_DATA[i],
                    d,
                    d < -1.0 ? Strings.repeat("\u2592", (int) Math.abs(Math.log10(-d) * 10.0)) : "",
                    d >  1.0 ? Strings.repeat("\u2592", (int) Math.abs(Math.log10(d) * 10.0)) : ""));
            i++;
        }
    }

//    @Test public void test000() throws Exception { execute(false, false, false); }
//    @Test public void test001() throws Exception { execute(false, false, true ); }
//    @Test public void test010() throws Exception { execute(false, true,  false); }
//    @Test public void test011() throws Exception { execute(false, true,  true ); }
    @Test public void test100() throws Exception { execute(true,  false, false); }
//    @Test public void test101() throws Exception { execute(true,  false, true ); }
//    @Test public void test110() throws Exception { execute(true,  true,  false); }
//    @Test public void test111() throws Exception { execute(true,  true,  true ); }
}
