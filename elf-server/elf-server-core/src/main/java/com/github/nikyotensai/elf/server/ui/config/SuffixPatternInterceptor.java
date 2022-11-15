package com.github.nikyotensai.elf.server.ui.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;

@Configuration
public class SuffixPatternInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.endsWith(".do")) {
            path = path.substring(0, path.length() - 3);
            Map<String, Object> map = new HashMap() {{
                put("context", request.getAttribute("context"));
            }};
            ModelAndView mav = new ModelAndView(path, map);
            mav.addObject("message", path);
            throw new ModelAndViewDefiningException(mav);
        }
        return true;
    }


}
