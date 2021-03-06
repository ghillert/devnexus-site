<%@page import="com.devnexus.ting.model.ScheduleItemType" %>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<head>
    <title>${contextEvent.title} - Register</title>
</head>

<!-- intro -->
<section id="about" class="module parallax parallax-3">
    <div class="container header">
        <div class="row centered">
            <div class="col-md-10 col-md-offset-1">
                <div class="top-intro travel">
                    <h4 class="section-white-title decorated"><span>Register for ${event.title}</span></h4>
                    <h5 class="intro-white-lead">${scheduleItemList.numberOfSpeakersAssigned} Speakers, ${scheduleItemList.numberOfSessions} Presentations, ${scheduleItemList.days.size()} Days.</h5>
                </div>
            </div>
        </div>
    </div>
</section>
<!-- /intro -->

<div class="container">

    <h1>Registration for <c:out value="${event.title}"/></h1>

        <div class="row">
            <form:form id="form" class="form-horizontal" role="form" method="get" modelAttribute="registrationDetails"  >

            <%@include file="registration_details.jsp" %>

        </form:form>
    </div>

    </div>
    <jsp:include page="includes/questions.jsp"/>

    <content tag='bottom'>
        <script type="text/javascript">
            $("#form input").prop("disabled", true);
            $("#form select").prop("disabled", true);
        </script>
    </content>
