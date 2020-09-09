<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:if test="${renderContext.editMode}">
    <legend>${fn:escapeXml(jcr:label(currentNode.primaryNodeType, currentResource.locale))}</legend>
</c:if>

<jcr:nodeProperty node="${currentNode}" name="name" var="name" />
<jcr:nodeProperty node="${currentNode}" name="image" var="image" />
<c:url value="${url.base}${currentNode.path}.html" var="nodeUrl" />

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
    <h2>${name}</h2>
    <img src="${imageUrl}" width="256" height="256"/>
    <p><a href="${nodeUrl}">Product Details</a></p>
</div>