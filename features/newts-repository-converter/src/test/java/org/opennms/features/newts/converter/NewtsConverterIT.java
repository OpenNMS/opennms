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

package org.opennms.features.newts.converter;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.TemporaryDatabasePostgreSQL;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
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
import org.opennms.newts.cassandra.NewtsInstance;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations={"classpath:/META-INF/opennms/applicationContext-soa.xml",
                                 "classpath:/META-INF/opennms/applicationContext-newts.xml"})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.rrd.storeByForeignSource=true",
        "org.opennms.timeseries.strategy=newts",
        "org.opennms.newts.nan_on_counter_wrap=false"
})
@JUnitTemporaryDatabase
public class NewtsConverterIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    @ClassRule
    public static NewtsInstance s_newtsInstance = new NewtsInstance();

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("org.opennms.newts.config.hostname", s_newtsInstance.getHost());
        System.setProperty("org.opennms.newts.config.port", Integer.toString(s_newtsInstance.getPort()));
        System.setProperty("org.opennms.newts.config.keyspace", s_newtsInstance.getKeyspace());
    }

    private final static Path OPENNMS_HOME;

    private final static ResourcePath RESOURCE_PATH_SNMP = ResourcePath.get("snmp", "fs", "fs1", "fid1", "eth0-04013f75f101");
    private final static ResourcePath RESOURCE_PATH_RESPONSE = ResourcePath.get("response", "127.0.0.1");

    private final static class Data {
        public final long timestamp;
        public final double value;

        private Data(final long timestamp, final double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private static Path RRD_BINARY = Paths.get("/usr/bin/rrdtool");
    static {
        if (!Files.exists(RRD_BINARY) || !Files.isExecutable(RRD_BINARY)) {
             RRD_BINARY = Paths.get("/usr/local/bin/rrdtool");
        }
    }

    private final static Data[] EXPECTED_DATA = {
            new Data(1414504800, Double.NaN),
            new Data(1414512000, Double.NaN),
            new Data(1414519200, Double.NaN),
            new Data(1414526400, Double.NaN),
            new Data(1414533600, Double.NaN),
            new Data(1414540800, Double.NaN),
            new Data(1414548000, Double.NaN),
            new Data(1414555200, Double.NaN),
            new Data(1414562400, Double.NaN),
            new Data(1414569600, Double.NaN),
            new Data(1414576800, Double.NaN),
            new Data(1414584000, Double.NaN),
            new Data(1414591200, Double.NaN),
            new Data(1414598400, Double.NaN),
            new Data(1414605600, 5531.6938325),
            new Data(1414612800, 4455.846126),
            new Data(1414620000, 635.17586932),
            new Data(1414627200, 274.10224206),
            new Data(1414634400, 272.80721253),
            new Data(1414641600, 272.73986157),
            new Data(1414648800, 272.78715024),
            new Data(1414656000, 274.56559109),
            new Data(1414663200, 273.08853129),
            new Data(1414670400, 271.89681801),
            new Data(1414677600, 523.24057817),
            new Data(1414684800, 1492.5738483),
            new Data(1414692000, 573.05671138),
            new Data(1414699200, 2159.5512362),
            new Data(1414706400, 5058.7548168),
            new Data(1414713600, 337.32252122),
            new Data(1414720800, 272.88883074),
            new Data(1414728000, 274.47826459),
            new Data(1414735200, 273.36833749),
            new Data(1414742400, 273.40830934),
            new Data(1414749600, 272.63246263),
            new Data(1414756800, 273.57742756),
            new Data(1414764000, 280.86905177),
            new Data(1414771200, 422.87544296),
            new Data(1414778400, 328.73004391),
            new Data(1414785600, 321.61006091),
            new Data(1414792800, 299.31773856),
            new Data(1414800000, 274.67561831),
            new Data(1414807200, 274.25790928),
            new Data(1414814400, 276.41772056),
            new Data(1414821600, 275.54827519),
            new Data(1414828800, 276.78948598),
            new Data(1414836000, 274.48604467),
            new Data(1414843200, 274.5228779),
            new Data(1414850400, 274.14460317),
            new Data(1414857600, 276.23911268),
            new Data(1414864800, 276.80193983),
            new Data(1414872000, 274.15346484),
            new Data(1414879200, 273.59105574),
            new Data(1414886400, 271.88462731),
            new Data(1414893600, 272.61512306),
            new Data(1414900800, 310.62198736),
            new Data(1414908000, 279.47495155),
            new Data(1414915200, 272.81952612),
            new Data(1414922400, 272.50835133),
            new Data(1414929600, 272.60627307),
            new Data(1414936800, 272.13984219),
            new Data(1414944000, 274.11375923),
            new Data(1414951200, 283.5517608),
            new Data(1414958400, 291.4291113),
            new Data(1414965600, 299.36599575),
            new Data(1414972800, 270.09306478),
            new Data(1414980000, 270.18613603),
            new Data(1414987200, 269.9630514),
            new Data(1414994400, 268.87677464),
            new Data(1415001600, 269.2061268),
            new Data(1415008800, 302.23889535),
            new Data(1415016000, 270.77449244),
            new Data(1415023200, 365.96435909),
            new Data(1415030400, 1049.0177076),
            new Data(1415037600, 1722.5255108),
            new Data(1415044800, 1122.2423837),
            new Data(1415052000, 691.14940661),
            new Data(1415059200, 541.28442386),
            new Data(1415066400, 278.67993106),
            new Data(1415073600, 324.13830334),
            new Data(1415080800, 268.27577427),
            new Data(1415088000, 268.08875092),
            new Data(1415095200, 268.19264489),
            new Data(1415102400, 279.58538391),
            new Data(1415109600, 268.82247416),
            new Data(1415116800, 351.04155685),
            new Data(1415124000, 353.55874077),
            new Data(1415131200, 331.4838935),
            new Data(1415138400, 327.46785391),
            new Data(1415145600, 391.07831364),
            new Data(1415152800, 272.08733327),
            new Data(1415160000, 307.82434385),
            new Data(1415167200, 275.26000636),
            new Data(1415174400, 271.11099816),
            new Data(1415181600, 271.32451551),
            new Data(1415188800, 268.99793973),
            new Data(1415196000, 269.52927926),
            new Data(1415203200, 519.42586471),
            new Data(1415210400, 508.4934833),
            new Data(1415217600, 693.24320275),
            new Data(1415224800, 27588.323374),
            new Data(1415232000, 9094.1996468),
            new Data(1415239200, 1976.2561303),
            new Data(1415246400, 268.57894255),
            new Data(1415253600, 271.49411268),
            new Data(1415260800, 269.35192321),
            new Data(1415268000, 268.87000923),
            new Data(1415275200, 270.64613326),
            new Data(1415282400, 308.46741048),
            new Data(1415289600, 876.81426542),
            new Data(1415296800, 2242.7481963),
            new Data(1415304000, 539.47730528),
            new Data(1415311200, 1134.8433688),
            new Data(1415318400, 2669.3579144),
            new Data(1415325600, 273.70118079),
            new Data(1415332800, 270.44728821),
            new Data(1415340000, 270.05971345),
            new Data(1415347200, 269.52376233),
            new Data(1415354400, 272.77581363),
            new Data(1415361600, 290.68759874),
            new Data(1415368800, 271.84585409),
            new Data(1415376000, 290.73369048),
            new Data(1415383200, 295.59432009),
            new Data(1415390400, 343.72934025),
            new Data(1415397600, 301.85411268),
            new Data(1415404800, 269.6179868),
            new Data(1415412000, 269.68850867),
            new Data(1415419200, 268.89033914),
            new Data(1415426400, 270.16508536),
            new Data(1415433600, 270.32965301),
            new Data(1415440800, 278.28334164),
            new Data(1415448000, 272.44995062),
            new Data(1415455200, 270.12529854),
            new Data(1415462400, 314.94049558),
            new Data(1415469600, 323.74971299),
            new Data(1415476800, 273.6797416),
            new Data(1415484000, 269.73690892),
            new Data(1415491200, 271.94061231),
            new Data(1415498400, 266.50917359),
            new Data(1415505600, 267.4105311),
            new Data(1415512800, 268.05489895),
            new Data(1415520000, 270.2201578),
            new Data(1415527200, 270.38548634),
            new Data(1415534400, 270.28007623),
            new Data(1415541600, 270.88393818),
            new Data(1415548800, 270.60961933),
            new Data(1415556000, 274.21916436),
            new Data(1415563200, 273.85593762),
            new Data(1415570400, 275.35481451),
            new Data(1415577600, 289.76133121),
            new Data(1415584800, 271.40274502),
            new Data(1415592000, 269.64664267),
            new Data(1415599200, 269.92278701),
            new Data(1415606400, 267.30472084),
            new Data(1415613600, 260.65593807),
            new Data(1415620800, 239.95965808),
            new Data(1415628000, 284.77827843),
            new Data(1415635200, 661.92565614),
            new Data(1415642400, 2027.6783609),
            new Data(1415649600, 604.07627181),
            new Data(1415656800, 637.14661361),
            new Data(1415664000, 328.39664129),
            new Data(1415671200, 269.11763704),
            new Data(1415678400, 267.15324567),
            new Data(1415685600, 269.33577566),
            new Data(1415692800, 269.04923035),
            new Data(1415700000, 268.71590613),
            new Data(1415707200, 269.57139213),
            new Data(1415714400, 269.52777269),
            new Data(1415721600, 287.27689398),
            new Data(1415728800, 290.27326898),
            new Data(1415736000, 292.31315525),
            new Data(1415743200, 299.03550283),
            new Data(1415750400, 269.22522853),
            new Data(1415757600, 267.72670081),
            new Data(1415764800, 270.04964793),
            new Data(1415772000, 268.71501154),
            new Data(1415779200, 269.87314046),
            new Data(1415786400, 280.933116),
            new Data(1415793600, 270.09802418),
            new Data(1415800800, 272.43157623),
            new Data(1415808000, 734.87711286),
            new Data(1415815200, 655.57811139),
            new Data(1415822400, 794.62980066),
            new Data(1415829600, 4492.3888644),
            new Data(1415836800, 410.66188867),
            new Data(1415844000, 288.07364791),
            new Data(1415851200, 278.57335883),
            new Data(1415858400, 267.68370321),
            new Data(1415865600, 267.38421992),
            new Data(1415872800, 268.16400471),
            new Data(1415880000, 269.66134575),
            new Data(1415887200, 282.82567945),
            new Data(1415894400, 679.27930758),
            new Data(1415901600, 1426.3808917),
            new Data(1415908800, 879.27072314),
            new Data(1415916000, 828.9951713),
            new Data(1415923200, 326.07949462),
            new Data(1415930400, 274.76369428),
            new Data(1415937600, 269.80165641),
            new Data(1415944800, 268.97105712),
            new Data(1415952000, 280.72824658),
            new Data(1415959200, 304.79537698),
            new Data(1415966400, 272.06614253),
            new Data(1415973600, 269.63110463),
            new Data(1415980800, 538.51540648),
            new Data(1415988000, 437.40010371),
            new Data(1415995200, 398.87997222),
            new Data(1416002400, 3071.629181),
            new Data(1416009600, 276.72273504),
            new Data(1416016800, 269.54695662),
            new Data(1416024000, 270.02708472),
            new Data(1416031200, 267.71633518),
            new Data(1416038400, 268.86198135),
            new Data(1416045600, 270.02647656),
            new Data(1416052800, 270.20114203),
            new Data(1416060000, 269.29119682),
            new Data(1416067200, 269.0503125),
            new Data(1416074400, 269.15197917),
            new Data(1416081600, 272.47931019),
            new Data(1416088800, 278.44052866),
            new Data(1416096000, 270.08651532),
            new Data(1416103200, 269.17012182),
            new Data(1416110400, 284.56125507),
            new Data(1416117600, 293.08841663),
            new Data(1416124800, 294.1850459),
            new Data(1416132000, 297.19444952),
            new Data(1416139200, 294.29051614),
            new Data(1416146400, 290.9026077),
            new Data(1416153600, 297.85124461),
            new Data(1416160800, 303.80809877),
            new Data(1416168000, 305.96696705),
            new Data(1416175200, 294.29025563),
            new Data(1416182400, 291.49208679),
            new Data(1416189600, 299.43816422),
            new Data(1416196800, 291.00242386),
            new Data(1416204000, 292.77719996),
            new Data(1416211200, 289.72932685),
            new Data(1416218400, 289.83784723),
            new Data(1416225600, 290.88990046),
            new Data(1416232800, 282.32523287),
            new Data(1416240000, 653.66218241),
            new Data(1416247200, 2563.4133435),
            new Data(1416254400, 95230.869954),
            new Data(1416261600, 3178.500549),
            new Data(1416268800, 439.32426736),
            new Data(1416276000, 269.39156423),
            new Data(1416283200, 266.0886531),
            new Data(1416290400, 268.71467423),
            new Data(1416297600, 285.9469749),
            new Data(1416304800, 279.96461656),
            new Data(1416312000, 269.46948735),
            new Data(1416319200, 270.02022241),
            new Data(1416326400, 313.54951504),
            new Data(1416333600, 332.74639997),
            new Data(1416340800, 309.42117438),
            new Data(1416348000, 21719.598091),
            new Data(1416355200, 286.22237772),
            new Data(1416362400, 270.33617202),
            new Data(1416369600, 269.11671327),
            new Data(1416376800, 303.6395012),
            new Data(1416384000, 269.09900148),
            new Data(1416391200, 269.25868401),
            new Data(1416398400, 268.59146964),
            new Data(1416405600, 269.8161185),
            new Data(1416412800, 513.00912606),
            new Data(1416420000, 553.92612198),
            new Data(1416427200, 1101.6777571),
            new Data(1416434400, 27558.56807),
            new Data(1416441600, 334.9193697),
            new Data(1416448800, 272.83034284),
            new Data(1416456000, 326.32155915),
            new Data(1416463200, 367.61463363),
            new Data(1416470400, 312.26599249),
            new Data(1416477600, 297.34590166),
            new Data(1416484800, 275.94522749),
            new Data(1416492000, 302.3437463),
            new Data(1416499200, 510.33056247),
            new Data(1416506400, 565.55211609),
            new Data(1416513600, 861.86348286),
            new Data(1416520800, 28900.856326),
            new Data(1416528000, 3625.2064802),
            new Data(1416535200, 275.32952704),
            new Data(1416542400, 275.08677187),
            new Data(1416549600, 274.29446291),
            new Data(1416556800, 276.52502308),
            new Data(1416564000, 306.65791621),
            new Data(1416571200, 276.85923035),
            new Data(1416578400, 272.48175572),
            new Data(1416585600, 521.69414821),
            new Data(1416592800, 482.98882891),
            new Data(1416600000, 272.33791298),
            new Data(1416607200, 282.10738834),
            new Data(1416614400, 288.81260567),
            new Data(1416621600, 320.7129545),
            new Data(1416628800, 283.63554771),
            new Data(1416636000, 280.44872001),
            new Data(1416643200, 274.98342562),
            new Data(1416650400, 275.63841085),
            new Data(1416657600, 303.28089147),
            new Data(1416664800, 309.30172065),
            new Data(1416672000, 274.05428849),
            new Data(1416679200, 270.70697028),
            new Data(1416686400, 279.43635613),
            new Data(1416693600, 319.35655823),
            new Data(1416700800, 281.36144196),
            new Data(1416708000, 271.58279162),
            new Data(1416715200, 272.22463133),
            new Data(1416722400, 256.16411868),
            new Data(1416729600, 248.81907069),
            new Data(1416736800, 203.24130306),
            new Data(1416744000, 207.43992202),
            new Data(1416751200, 204.57201181),
            new Data(1416758400, 205.7050203),
            new Data(1416765600, 203.24148487),
            new Data(1416772800, 250.20869694),
            new Data(1416780000, 224.58983343),
            new Data(1416787200, 201.19693014),
            new Data(1416794400, 202.62421558),
            new Data(1416801600, 202.86971669),
            new Data(1416808800, 201.46533684),
            new Data(1416816000, 243.19836194),
            new Data(1416823200, 229.56466324),
            new Data(1416830400, 206.26040136),
            new Data(1416837600, 206.95304079),
            new Data(1416844800, 1115.1947725),
            new Data(1416852000, 1130.0013552),
            new Data(1416859200, 472.70847499),
            new Data(1416866400, 712.24797434),
            new Data(1416873600, 377.75863141),
            new Data(1416880800, 305.7432046),
            new Data(1416888000, 338.34625),
            new Data(1416895200, 324.61974945),
            new Data(1416902400, 348.06653516),
            new Data(1416909600, 312.99157254),
            new Data(1416916800, 309.40358573),
            new Data(1416924000, 304.74407992),
            new Data(1416931200, 480.41310447),
            new Data(1416938400, 817.8687966),
            new Data(1416945600, 471.17261535),
            new Data(1416952800, 959.92732327),
            new Data(1416960000, 581.7288146),
            new Data(1416967200, 273.35553512),
            new Data(1416974400, 272.76474081),
            new Data(1416981600, 276.54777409),
            new Data(1416988800, 286.05296788),
            new Data(1416996000, 291.77477298),
            new Data(1417003200, 297.78315892),
            new Data(1417010400, 279.46530823),
            new Data(1417017600, 86547.011294),
            new Data(1417024800, 332768.73057),
            new Data(1417032000, 794.815224),
            new Data(1417039200, 842.01765852),
            new Data(1417046400, 533.42102532),
            new Data(1417053600, 286.22333334),
    };

    static {
        try {
            OPENNMS_HOME = Paths.get(NewtsConverterIT.class.getResource("/opennms-home").toURI());

        } catch (final URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Autowired
    private SampleRepository repository;

    @Autowired
    private ResourceStorageDao resourceStorageDao;

    private TemporaryDatabase database;

    private static boolean populated = false;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.database = database;
    }

    @Before
    public void setupDatabase() throws Exception {
        if (!NewtsConverterIT.populated) {
            this.database.getJdbcTemplate().execute("INSERT INTO node (location, nodeid, nodecreatetime, nodelabel, foreignsource, foreignid) " +
                                               "VALUES ('" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID + "', 1, NOW(), 'my-node-1', 'fs1', 'fid1')");


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

        assertTrue(Files.isDirectory(data));

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> {
            assertThat(resourceStorageDao.exists(RESOURCE_PATH_SNMP, 0), is(true));
            assertThat(resourceStorageDao.getAttributes(RESOURCE_PATH_SNMP),
                       hasItems(allOf(hasProperty("name", is("ifInOctets"))),
                                allOf(hasProperty("name", is("ifSpeed")),
                                      hasProperty("value", is("1000")))));

            assertThat(resourceStorageDao.exists(RESOURCE_PATH_RESPONSE, 0), is(true));
            assertThat(resourceStorageDao.getAttributes(RESOURCE_PATH_RESPONSE),
                       hasItems(allOf(hasProperty("name", is("icmp")))));

            final Results<Measurement> result = repository.select(Context.DEFAULT_CONTEXT,
                                                                  new Resource(NewtsUtils.toResourceId(ResourcePath.get(RESOURCE_PATH_SNMP, "mib2-interfaces"))),
                                                                  Optional.of(Timestamp.fromEpochSeconds(1414504800)),
                                                                  Optional.of(Timestamp.fromEpochSeconds(1417047045)),


                                                                  new ResultDescriptor(Duration.seconds(7200))
                                                                          .datasource("ifInOctets", StandardAggregationFunctions.AVERAGE)
                                                                          .export("ifInOctets"),
                                                                  Optional.of(Duration.seconds(7200)));

            assertThat(result.getRows().size(), is(EXPECTED_DATA.length));

            int i = 0;
            for (Results.Row<Measurement> r : result) {
                final double deltaAbs = r.getElement("ifInOctets").getValue().doubleValue() - EXPECTED_DATA[i].value;
                final double deltaRel = deltaAbs / EXPECTED_DATA[i].value * 100.0;

                /* Use the following for debugging of non-matching entries...
                System.out.println(String.format(
                        "%4d: %-24s %11.2f %11.2f %11.2f (%6.2f) %36s|%-36s %f%%",
                        i,
                        r.getElement("ifInOctets").getTimestamp().asDate().toString(),
                        r.getElement("ifInOctets").getValue().doubleValue(),
                        EXPECTED_DATA[i].value,
                        deltaAbs,
                        deltaRel,
                        deltaAbs < -1.0 ? Strings.repeat("\u2592", (int) Math.abs(Math.log10(-deltaAbs) * 10.0)) : "",
                        deltaAbs >  1.0 ? Strings. repeat("\u2592", (int) Math.abs(Math.log10(deltaAbs) * 10.0)) : "",
                        Math.abs(deltaAbs / EXPECTED_DATA[i].value * 100.0)));
                 */

                assertThat(r.getTimestamp().asSeconds(),
                           is(EXPECTED_DATA[i].timestamp));

                if (i != 270) { // We got some errors on the RRA boundaries - ignore them
                    assertThat(r.getElement("ifInOctets").getValue().doubleValue(),
                               is(anyOf(equalTo(EXPECTED_DATA[i].value),
                                        closeTo(EXPECTED_DATA[i].value, EXPECTED_DATA[i].value * 0.003)))); // Allow a relative error of 0.3%
                }

                i++;
            }
        });

        NewtsConverter.main("-o", OPENNMS_HOME.toString(),
                            "-r", data.toString(),
                            "-t", Boolean.toString(useRrdTool),
                            "-T", RRD_BINARY.toAbsolutePath().toString(),
                            "-s", Boolean.toString(storeByGroup));
    }

    @Test public void test000() throws Exception { execute(false, false, false); }
    @Test public void test001() throws Exception { execute(false, false, true ); }
    @Test public void test010() throws Exception { execute(false, true,  false); }
    @Test public void test011() throws Exception { execute(false, true,  true ); }
    @Test public void test100() throws Exception { execute(true,  false, false); }
    @Test public void test101() throws Exception { execute(true,  false, true ); }
    @Test public void test110() throws Exception { execute(true,  true,  false); }
    @Test public void test111() throws Exception { execute(true,  true,  true ); }

}
