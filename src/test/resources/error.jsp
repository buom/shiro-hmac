<%@ page pageEncoding="ISO-8859-1" contentType="text/plain" isErrorPage="true" %>
<%@ page import="java.util.Enumeration"%>

<%=request.getAttribute("javax.servlet.error.message")%>

<%--
    for (Enumeration enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
        String attrName = (String) enumeration.nextElement();
        out.print(attrName + "=" + request.getAttribute(attrName) + "    ");
    }
--%>
