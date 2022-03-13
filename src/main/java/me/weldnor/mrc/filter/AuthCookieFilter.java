package me.weldnor.mrc.filter;


import me.weldnor.mrc.security.SimpleAuthentication;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthCookieFilter implements Filter {


    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpSession session = httpRequest.getSession();

        ObjectId userIdAttribute = (ObjectId) session.getAttribute("userId");

        if (userIdAttribute != null) {
            Authentication authentication = new SimpleAuthentication(userIdAttribute);
            SecurityContext context = new SecurityContextImpl(authentication);
            SecurityContextHolder.setContext(context);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
