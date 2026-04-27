package com.ration.filter;

import com.ration.model.User;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// DispatcherType.REQUEST only: prevents AuthFilter running on ERROR/FORWARD dispatches,
// which broke the error-page flow by re-running auth checks and creating redirect loops.
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST})
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
            res.sendRedirect(contextPath + "/LoginServlet");
            return;
        }

        if (user != null && isUnauthorizedRoleAccess(path, user)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicPath(String path) {
        // Explicit allow-list — anything not listed here requires authentication.
        return "/".equals(path)
                || "/LoginServlet".equals(path)
                || "/RegisterServlet".equals(path)
                || "/ResetPasswordServlet".equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon");
    }

    private boolean isUnauthorizedRoleAccess(String path, User user) {
        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();

        // Use startsWith prefix checks — contains("/admin") would falsely match paths
        // like "/administrator" or a query string containing the word admin.
        if ((path.startsWith("/admin-dashboard.jsp") || path.startsWith("/admin/"))
                && !"ADMIN".equals(role)) {
            return true;
        }

        // Static admin-only HTML pages — require ADMIN role
        if (isAdminOnlyStaticPage(path) && !"ADMIN".equals(role)) {
            return true;
        }

        // Citizen-specific paths
        if ((path.startsWith("/citizen-dashboard.jsp") || path.startsWith("/citizen/"))
                && !"CITIZEN".equals(role)) {
            return true;
        }

        // Citizen-only static pages
        if (isCitizenOnlyStaticPage(path) && !"CITIZEN".equals(role)) {
            return true;
        }

        return false;
    }

    /** Static HTML pages that are admin-only. */
    private boolean isAdminOnlyStaticPage(String path) {
        return "/stock.html".equals(path)
                || "/approvals.html".equals(path)
                || "/reports.html".equals(path);
    }

    /** Static HTML pages that are citizen-only. */
    private boolean isCitizenOnlyStaticPage(String path) {
        return "/family.html".equals(path)
                || "/complaints.html".equals(path)
                || "/allocation.html".equals(path);
    }
}
