<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp"%>
<% pageContext.setAttribute("lf", "\n"); %>

		<!-- Start of first page -->
		<div data-role="page" id="organizers" data-theme="b">

		    <div data-role="header">
		        <a rel="external" href="${ctx}/s/index">Back</a>
		        <h1>DevNexus 2011</h1>
		    </div><!-- /header -->

		    <div data-role="content">
		        <ul data-role="listview" data-inset="true" data-theme="c" data-dividertheme="b">
		            <li data-role="list-divider">Organizers</li>
			            <c:forEach items="${organizerList.organizers}" var="organizer">
			                <li>
			                    <img src="${ctx}/s/organizers/${organizer.id}.jpg"/>
			                    <a href="#${organizer.firstName}_${organizer.lastName}"><c:out value="${organizer.firstName}"/> <c:out value="${organizer.lastName}"/></a>
			                </li>
			            </c:forEach>
		        </ul>
		    </div><!-- /content -->

		    <div data-role="footer">
		        <h4>&copy; 2011 AJUG</h4>
		    </div><!-- /header -->
		</div><!-- /page -->

        <c:forEach items="${organizerList.organizers}" var="organizer">
	        <!-- Start of page -->
	        <div data-role="page" id="${organizer.firstName}_${organizer.lastName}" data-theme="b">

	            <div data-role="header">
	                <h1><c:out value="${organizer.firstName}"/> <c:out value="${organizer.lastName}"/></h1>
	            </div><!-- /header -->

	            <div data-role="content">
	                <c:if test="${organizer.picture != null}">
	                   <img src="${ctx}/s/organizers/${organizer.id}.jpg"/>
	                </c:if>
	                <p>
	                  <c:set var="organizerBio"><c:out value="${organizer.bio}" escapeXml="true"/></c:set>
	                  <c:out value="${fn:replace(organizerBio, lf, '<br/>')}" escapeXml="false"/>
	                </p>
	            </div><!-- /content -->

	            <div data-role="footer">
	                <h4>&copy; 2011 AJUG</h4>
	            </div><!-- /header -->
	        </div><!-- /page -->
        </c:forEach>


