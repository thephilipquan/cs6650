package org.philipquan.servlet.validator;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

public abstract class RequestValidator {

    protected Integer status;
    protected String errorMessage;
    protected HashMap<String, Object> map;

    protected RequestValidator(){
        this.status = -1;
        this.errorMessage = null;
        this.map = new HashMap<>();
    }

    public abstract Boolean validate(HttpServletRequest request);

    public Integer getStatus() {
        if (this.status.equals(-1)) {
            throw new RuntimeException("calling getStatus on RequestValidator that is valid.");
        }
        return this.status;
    }

    public String getErrorMessage() {
        if (this.errorMessage == null) {
            throw new RuntimeException("calling getErrorMessage on RequestValidator that is valid.");
        }
        return this.errorMessage;
    }

    public Object getParameter(String key) {
        return this.map.get(key);
    }
}