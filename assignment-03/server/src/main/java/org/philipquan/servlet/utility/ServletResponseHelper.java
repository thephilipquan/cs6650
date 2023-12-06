package org.philipquan.servlet.utility;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public class ServletResponseHelper {

    public static void setResponse(Map<String, Object> messages, HttpServletResponse response, int statusCode, Object responseObject)
      throws IOException {
        messages.put("status", statusCode);
        messages.put("message", responseObject);
        response.setStatus(statusCode);
        response.getWriter().write(new Gson().toJson(messages));
    }

    /**
     * @param value the value to check if it is wholly a number.
     * @return true if the string is a number.
     */
    public static boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}