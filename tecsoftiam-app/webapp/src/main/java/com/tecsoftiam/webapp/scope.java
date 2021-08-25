package com.tecsoftiam.webapp;

import java.io.FileInputStream;
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
    private String appId;
    private Properties oAuthProperties = new Properties();
    String path = "./oAuth.properties";

    /**
     * create scope from parameters
     * 
     * @param id        scope id
     * @param tenantId  tenant id
     * @param password  tenant password
     * @param scopeName scope name
     */
    public Scope(int id, String tenantId, String password, String scopeName,String appid) {
        this.id = id;
        this.tenantId = tenantId;
        this.password = password;
        this.scopeName = scopeName;
        this.appId=appid;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }


    

    /**
     * Constructor for default scope, will read data from config file
     * 
     * @throws IOException
     */
    public Scope() throws IOException {
        try{
        FileInputStream file= new FileInputStream(path);
        Properties write= new Properties();
        write.load(file);
        this.tenantId = write.getProperty("app.tenant");
        this.password = write.getProperty("app.secret");
        this.scopeName = write.getProperty("app.scopename");
        this.appId=write.getProperty("app.id");
        System.out.println("ok");
        }
        catch(Exception e) {
            System.out.println("firstload");
            firstLoad();
        }

    }

    public void firstLoad() throws IOException{
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        this.tenantId = oAuthProperties.getProperty("app.tenant");
        this.password = oAuthProperties.getProperty("app.secret");
        this.scopeName = oAuthProperties.getProperty("app.scopename");
        this.appId=oAuthProperties.getProperty("app.id");
        writeInFile();
    }
    /**
     * empty consctruct, not used
     * 
     * @param val to define
     */
    public Scope(int val) {

    }

    
    /** 
     * @throws IOException
     */
    public void writeInFile() throws IOException {
        
        FileOutputStream fos = new FileOutputStream(path);
        Properties write= new Properties();
        write.setProperty("app.secret", this.password);
        write.setProperty("app.tenant", this.tenantId);
        write.setProperty("app.scopename", this.scopeName);
        write.setProperty("app.id", this.appId);
        write.store(new FileOutputStream(path), null);

    }

    
    /** 
     * @return int
     */
    public int getId() {
        return this.id;
    }

    
    /** 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    
    /** 
     * @return String
     */
    public String getTenantId() {
        return this.tenantId;
    }

    
    /** 
     * @param tenantId
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    
    /** 
     * @return String
     */
    public String getPassword() {
        return this.password;
    }

    
    /** 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    
    /** 
     * @return String
     */
    public String getScopeName() {
        return this.scopeName;
    }

    
    /** 
     * @param scopeName
     */
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }
}
