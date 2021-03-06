<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jcr:nodeProperty node="${currentNode}" name="name" var="name" />
<jcr:nodeProperty node="${currentNode}" name="description" var="description" />
<jcr:nodeProperty node="${currentNode}" name="model" var="model" />
<jcr:nodeProperty node="${currentNode}" name="price" var="price" />
<jcr:nodeProperty node="${currentNode}" name="image" var="image" />
<jcr:nodeProperty node="${currentNode}" name="manual" var="manual" />
<c:if test="${not empty currentNode.properties['image']}">
  <c:choose>
  <c:when test="${functions:contains(image, 'http')}">
    <c:set value="${image}" var="imageUrl" />
  </c:when>
  <c:otherwise>
    <c:url value="${url.files}${image}" var="imageUrl" />
  </c:otherwise>
  </c:choose>
</c:if>

<div class="contents">
<h1>${name}</h1>
<p><b>Product Description:</b> ${description}</p>
<img src="${imageUrl}" />
<p><b>Model:</b> ${model}</p>
<p><b>Price:</b> ${price}</p>
<p><a href="${manual}">User Manual</a></p>
</div>