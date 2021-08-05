package com.tecsoftiam.webapp;

import java.io.IOException;
import java.util.Properties;
import com.tecsoftiam.WebappApplication;

public class scope {
    private int id;
    private String tenantId;
    private String password;
    private String scopeName;
    private Properties oAuthProperties = new Properties();
    
    public scope(int id, String tenantId, String password, String scopeName){
        this.id=id;
        this.tenantId=tenantId;
        this.password=password;
        this.scopeName=scopeName;
    }

    public void writeInFile() throws IOException{
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        oAuthProperties.setProperty("app.secret", this.password);
        oAuthProperties.setProperty("app.tenant", this.tenantId);
        oAuthProperties.setProperty("app.scopeName", this.scopeName);
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
