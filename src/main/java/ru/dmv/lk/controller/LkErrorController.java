package ru.dmv.lk.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.dmv.lk.dto.MyAppConst;


import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class LkErrorController implements ErrorController{

    @RequestMapping("/error")
    public String error(Map<String, Object> model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        model.put("MyApp", new MyAppConst());

        if (status != null) {
            int statusCode = Integer.valueOf(status.toString());

            //403
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "errors/error_403";
            }
        }
        return "errors/error";
    }
    //со spring 2.3 - не нужна. Использовать server.error.path
    @Override
    @SuppressWarnings("deprecation")
    public String getErrorPath() {
        return null;
    }
}
