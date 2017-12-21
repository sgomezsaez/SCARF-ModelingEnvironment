<%--
/*******************************************************************************
 * Copyright (c) 2012-2013 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Oliver Kopp - initial API and implementation and/or initial documentation
 *******************************************************************************/
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${empty it.policyType.appliesTo.nodeTypeReference}">&ndash;</c:if>

<ul>
<c:forEach items="${it.policyType.appliesTo.nodeTypeReference}" var="nodeTypeReference">
<li>${nodeTypeReference.typeRef}</li>
</c:forEach>
</ul>


Update not yet implemented.