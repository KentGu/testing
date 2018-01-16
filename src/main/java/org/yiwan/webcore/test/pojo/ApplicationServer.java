package org.yiwan.webcore.test.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Kenny Wang on 5/12/2016.
 */

public class ApplicationServer {
    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServer.class);
    private int id;
    private String name;
    private String url;
    private String host;
    private String port;
    private String version;
    private String username;
    private String password;
    private String key;
    private HardwareInformation hardwareInformation;
    private List<SoftwareInformation> softwareInformations;

    public List<SoftwareInformation> getSoftwareInformations() {
        return softwareInformations;
    }

    public void setSoftwareInformations(List<SoftwareInformation> softwareInformations) {
        this.softwareInformations = softwareInformations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HardwareInformation getHardwareInformation() {
        return hardwareInformation;
    }

    public void setHardwareInformation(HardwareInformation hardwareInformation) {
        this.hardwareInformation = hardwareInformation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

