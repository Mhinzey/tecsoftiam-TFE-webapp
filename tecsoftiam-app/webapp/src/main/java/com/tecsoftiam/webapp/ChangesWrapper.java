package com.tecsoftiam.webapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for change list
 * Author: Deryck Olivier
 */
public class ChangesWrapper {
    private List<AdChanges> changesList = new ArrayList<AdChanges>();

    public ChangesWrapper() {

    }

    
    /** 
     * @return List<adChanges>
     */
    public List<AdChanges> getChangesList() {
        return this.changesList;
    }

    
    /** 
     * @param changesList
     */
    public void setChangesList(List<AdChanges> changesList) {
        this.changesList = changesList;
    }

}
