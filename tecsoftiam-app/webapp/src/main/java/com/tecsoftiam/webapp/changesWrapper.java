package com.tecsoftiam.webapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for change list
 * Author: Deryck Olivier
 */
public class changesWrapper {
    private List<adChanges> changesList = new ArrayList<adChanges>();

    public changesWrapper() {

    }

    
    /** 
     * @return List<adChanges>
     */
    public List<adChanges> getChangesList() {
        return this.changesList;
    }

    
    /** 
     * @param changesList
     */
    public void setChangesList(List<adChanges> changesList) {
        this.changesList = changesList;
    }

}
