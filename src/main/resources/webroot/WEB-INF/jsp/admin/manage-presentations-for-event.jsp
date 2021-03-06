<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp"%>

<title>Manage Presentations</title>
<div style="margin-top: 20px" class="col-md-10 col-md-offset-1">
	<h2>Manage Presentations</h2>
</div>
<div class="row">
	<div class="col-md-10 col-md-offset-1">
		<table class="table table-hover">
			<thead>
				<tr>
					<th>Action</th><th>Title</th>
				</tr>
			</thead>

			<c:forEach items="${presentations}" var="presentation">
				<tr>
					<td><a href="${ctx}${baseSiteUrl}/admin/presentation/${presentation.id}" class="btn btn-default"><span class="glyphicon glyphicon-edit"></span></a></td>
					<td><c:out value="${presentation.title}"/></td>
				</tr>
			</c:forEach>
		</table>
		<a class="btn btn-default" href="${ctx}${baseSiteUrl}/admin/presentation" role="button">Add Presentation</a>
		<a class="btn btn-default" href="${ctx}${baseSiteUrl}/admin/index" role="button">Main Menu</a>
	</div>
</div>
