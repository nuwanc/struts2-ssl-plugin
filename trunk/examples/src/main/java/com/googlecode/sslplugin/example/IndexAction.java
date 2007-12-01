package com.googlecode.sslplugin.example;

import com.opensymphony.xwork2.ActionSupport;
import com.googlecode.sslplugin.annotation.Secured;

/**
 *
 */

public class IndexAction extends ActionSupport {

    /**
     * This method will not be called in https mode.
     *
     * @return
     * @throws Exception
     */
    public String execute() throws Exception {
        return SUCCESS;
    }


    /**
     * This method is called in https mode, as it has the @Secured annotation
     *
     * @return
     * @throws Exception
     */
    @Secured
    public String execute1() throws Exception {
        return SUCCESS;
    }


}
