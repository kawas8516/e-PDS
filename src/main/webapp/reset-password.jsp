<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%
    // Redirect to ResetPasswordServlet — the view lives under WEB-INF and must be
    // accessed via the servlet, which seeds the CSRF token before forwarding.
    response.sendRedirect(request.getContextPath() + "/ResetPasswordServlet");
%>
