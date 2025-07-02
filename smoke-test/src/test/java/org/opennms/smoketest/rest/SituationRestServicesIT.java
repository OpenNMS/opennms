package org.opennms.smoketest.rest;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SituationRestServicesIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private RestClient restClient;

    private static   Integer situationId;

    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms/api/v2";
        RestAssured.authentication = preemptive()
                .basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD);
        restClient = stack.opennms().getRestClient();
    }



    private int fetchRelatedCount() {
        return given()
                .accept(ContentType.JSON)
                .queryParam("limit", 2)
                .queryParam("offset", 0)
                .when()
                .get("/situations")
                .then()
                .statusCode(200)
                .extract()
                .path("alarm[0].relatedAlarms.size()");
    }

    @Test
    public void test1_createSituation() {
        // send two events
        restClient.sendEvent(getServiceProblemEvent("Major", "uei.opennms.org/bsm/serviceProblem"));
        restClient.sendEvent(getServiceProblemEvent("Minor", "uei.opennms.org/traps/A10/axFan1Failure"));

        await().atMost(2, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS)
                .until(() -> restClient.getAlarms().size() >= 2);

        List<Integer> alarmIds = new ArrayList<>();
        for (OnmsAlarm alarm : restClient.getAlarms().getObjects()) {
            alarmIds.add(alarm.getId());
        }

        JSONObject payload = new JSONObject()
                .put("alarmIdList", new JSONArray(alarmIds))
                .put("diagnosticText", "diagnosticText")
                .put("description", "description")
                .put("feedback", "feedback");

        given().auth().basic(
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD
                )
                .contentType(ContentType.JSON)
                .body(payload.toString())
                .when()
                .post("/situations/create")
                .then()
                .statusCode(200);

        situationId = given()
                .accept(ContentType.JSON)
                .queryParam("limit", 2)
                .queryParam("offset", 0)
                .when()
                .get("/situations")
                .then()
                .statusCode(200)
                .body("alarm.size()", equalTo(1))
                .extract().path("alarm[0].id");

        assertNotNull("Situation ID must be set", situationId);
    }

    @Test
    public void test2_addAlarm() {
        restClient.sendEvent(
                getServiceProblemEvent("Minor", "uei.opennms.org/traps/A10/axLowerPowerSupplyFailure")
        );

        AlarmDao dao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);
        OnmsAlarm newAlarm = await().atMost(2, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS)
                .until(DaoUtils.findMatchingCallable(
                        dao,
                        new CriteriaBuilder(OnmsAlarm.class)
                                .eq("uei", "uei.opennms.org/traps/A10/axLowerPowerSupplyFailure")
                                .toCriteria()), notNullValue());

        assertEquals(1L, (long)newAlarm.getCounter());

        JSONObject assoc = new JSONObject()
                .put("alarmIdList", new JSONArray(List.of(newAlarm.getId())))
                .put("situationId", situationId);

        given().auth().basic(
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD
                )
                .contentType(ContentType.JSON)
                .body(assoc.toString())
                .when()
                .post("/situations/associateAlarm")
                .then()
                .statusCode(200);

        int before = fetchRelatedCount() - 1;
        int after  = fetchRelatedCount();
        assertEquals("Should add exactly one alarm", before + 1, after);
    }

    @Test
    public void test3_removeAlarm() {
        AlarmDao dao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);
        OnmsAlarm target = await().atMost(2, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS)
                .until(DaoUtils.findMatchingCallable(
                        dao,
                        new CriteriaBuilder(OnmsAlarm.class)
                                .eq("uei", "uei.opennms.org/traps/A10/axLowerPowerSupplyFailure")
                                .toCriteria()), notNullValue());

        assertEquals(1L, (long)target.getCounter());

        JSONObject removal = new JSONObject()
                .put("alarmIdList", new JSONArray(List.of(target.getId())))
                .put("situationId", situationId);

        given().auth().basic(
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD
                )
                .contentType(ContentType.JSON)
                .body(removal.toString())
                .when()
                .delete("/situations/removeAlarm")
                .then()
                .statusCode(200);

        int before = fetchRelatedCount() + 1;
        int after  = fetchRelatedCount();
        assertEquals("Should remove exactly one alarm", before - 1, after);
    }

    @Test
    public void test4_clearSituation() {
        PrintStream log = System.out;
        RequestLoggingFilter logFilter = new RequestLoggingFilter(log);

        JSONObject clear = new JSONObject().put("situationId", situationId);

        given().filter(logFilter)
                .auth().basic(
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD
                )
                .contentType(ContentType.JSON)
                .body(clear.toString())
                .when()
                .post("/situations/clear")
                .then()
                .statusCode(equalTo(204));


        given()
                .accept(ContentType.JSON)
                .queryParam("limit", 2)
                .queryParam("offset", 0)
                .when()
                .get("/situations")
                .then()
                .statusCode(equalTo(200));
    }

    private Event getServiceProblemEvent(String severity, String uei) {
        return new EventBuilder(uei, "test").setSeverity(severity).getEvent();
    }

}
