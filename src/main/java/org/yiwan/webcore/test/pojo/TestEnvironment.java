package org.yiwan.webcore.test.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.util.Helper;

import java.util.List;

/**
 * Created by Kenny Wang on 3/30/2016.
 */
public class TestEnvironment {
    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(TestEnvironment.class);
    private List<ApplicationServer> applicationServers;
    private List<DatabaseServer> databaseServers;

    public List<DatabaseServer> getDatabaseServers() {
        return databaseServers;
    }

    public void setDatabaseServers(List<DatabaseServer> databaseServers) {
        this.databaseServers = databaseServers;
    }

    public List<ApplicationServer> getApplicationServers() {
        return applicationServers;
    }

    public void setApplicationServers(List<ApplicationServer> applicationServers) {
        this.applicationServers = applicationServers;
    }

    public ApplicationServer getApplicationServer(int id) throws Exception {
        return (ApplicationServer) Helper.filterListById(applicationServers, id);
    }

    public DatabaseServer getDatabaseServer(int id) throws Exception {
        return (DatabaseServer) Helper.filterListById(databaseServers, id);
    }

    @Override
    public String toString() {
        try {
            return (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }
}