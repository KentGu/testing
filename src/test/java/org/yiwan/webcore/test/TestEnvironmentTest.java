package org.yiwan.webcore.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.yiwan.webcore.test.pojo.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Kenny Wang on 5/12/2016.
 */
public class TestEnvironmentTest {
    private static final Logger logger = LoggerFactory.getLogger(TestEnvironmentTest.class);

    @Test
    public void testJsonToTestEnviroment() throws Exception {
        String json = "[{\"applicationServers\":[{\"id\":0,\"name\":\"default\",\"url\":\"http://localhost:8080/\"}],\"databaseServers\":[{\"name\":\"default\",\"dump\":\"data/system/default.xml\"}]}]";
        List<TestEnvironment> testEnvironments = (new ObjectMapper()).readValue(json, new TypeReference<List<TestEnvironment>>() {
        });
        assertThat(testEnvironments.get(0).getApplicationServer(0).getUrl()).isEqualTo("http://localhost:8080/");
    }

    @Test
    public void testTestEnviromentToJson() {
        TestEnvironment testEnvironment = new TestEnvironment();
        ApplicationServer applicationServer = new ApplicationServer();
        DatabaseServer databaseServer = new DatabaseServer();
        HardwareInformation hardwareInformation = new HardwareInformation();
        SoftwareInformation softwareInformation = new SoftwareInformation();
        applicationServer.setHardwareInformation(hardwareInformation);
        applicationServer.setSoftwareInformations(Arrays.asList(softwareInformation));
        databaseServer.setHardwareInformation(hardwareInformation);
        databaseServer.setSoftwareInformations(Arrays.asList(softwareInformation));
        testEnvironment.setApplicationServers(Arrays.asList(applicationServer));
        testEnvironment.setDatabaseServers(Arrays.asList(databaseServer));
        logger.info(testEnvironment.toString());
    }

    @Test
    public void testTestEnvironmentAndJsonConversionEachOther() throws Exception {
        String url = "http://localhost:8080/";
        TestEnvironment testEnvironment = new TestEnvironment();
        ApplicationServer applicationServer = new ApplicationServer();
        applicationServer.setUrl(url);
        DatabaseServer databaseServer = new DatabaseServer();
        databaseServer.setDump("data/system/default.xml");
        testEnvironment.setApplicationServers(Arrays.asList(applicationServer));
        testEnvironment.setDatabaseServers(Arrays.asList(databaseServer));
        String json = String.format("[%s]", testEnvironment.toString());
        logger.info(json);
        List<TestEnvironment> testEnvironments = (new ObjectMapper()).readValue(json, new TypeReference<List<TestEnvironment>>() {
        });
        assertThat(testEnvironments.get(0).getApplicationServer(0).getUrl()).isEqualTo(url);
    }
}
