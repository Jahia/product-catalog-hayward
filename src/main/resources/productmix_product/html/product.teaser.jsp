<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="name" />
<jcr:nodeProperty node="${currentNode}" name="description" var="description" />
<jcr:nodeProperty node="${currentNode}" name="model" var="model" />
<jcr:nodeProperty node="${currentNode}" name="price" var="price" />
<jcr:nodeProperty node="${currentNode}" name="image" var="image" />
<c:url value="${url.files}${image.node.path}" var="imageUrl" />
<jcr:nodeProperty node="${currentNode}" name="manual" var="manual" />

<div class="contents">
    <h2>${name}</h2>
    <img src="${imageUrl}" />
</div>