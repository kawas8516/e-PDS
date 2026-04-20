package com.ration.filter;

import com.ration.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        boolean isPublic = isPublicPath(path);
        if (user == null && !isPublic) {
            res.sendRedirect(contextPath + "/index.jsp");
            return;
        }

        if (user != null && isUnauthorizedRoleAccess(path, user)) {
            res.sendRedirect(contextPath + "/index.jsp?error=unauthorized");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicPath(String path) {
        return "/".equals(path)
                || "/index.jsp".equals(path)
                || "/LoginServlet".equals(path)
                || "/register.jsp".equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon")
                || path.startsWith("/LogoutServlet");
    }

    private boolean isUnauthorizedRoleAccess(String path, User user) {
        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();

        if (path.contains("/admin") && !"ADMIN".equals(role)) {
            return true;
        }

        return path.contains("/citizen") && !"CITIZEN".equals(role);
    }
}
