package com.tecsoftiam.webapp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import com.tecsoftiam.WebappApplication;

/**
 * Scope class, represents a scope 
 * Author: Deryck Olivier
 */
public class Scope {
    private int id;
    private String tenantId;
    private String password;
    private String scopeName;
    private Properties oAuthProperties = new Properties();

    /**
     * create scope from parameters
     * 
     * @param id        scope id
     * @param tenantId  tenant id
     * @param password  tenant password
     * @param scopeName scope name
     */
    public Scope(int id, String tenantId, String password, String scopeName) {
        this.id = id;
        this.tenantId = tenantId;
        this.password = password;
        this.scopeName = scopeName;
    }

    /**
     * Constructor for default scope, will read data from config file
     * 
     * @throws IOException
     */
    public Scope() throws IOException {
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        this.tenantId = oAuthProperties.getProperty("app.tenant");
        this.password = oAuthProperties.getProperty("app.secret");
        this.scopeName = oAuthProperties.getProperty("app.scopename");

    }

    /**
     * empty consctruct, not used
     * 
     * @param val to define
     */
    public Scope(int val) {

    }

    public void writeInFile() throws IOException {
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        oAuthProperties.setProperty("app.secret", this.password);
        oAuthProperties.setProperty("app.tenant", this.tenantId);
        oAuthProperties.setProperty("app.scopename", this.scopeName);
        URL url = WebappApplication.class.getClassLoader().getResource("oAuth.properties");
        String path = url.getPath();
        oAuthProperties.store(new FileOutputStream(path), null);

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getScopeName() {
        return this.scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }
}
