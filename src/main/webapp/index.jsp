<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%
    // Root index.jsp — redirect to LoginServlet which owns the login view.
    // Previously this forwarded to /WEB-INF/views/index.jsp, causing an AuthFilter
    // loop because LoginServlet and ResetPasswordServlet also forwarded here.
    response.sendRedirect(request.getContextPath() + "/LoginServlet");
%>
