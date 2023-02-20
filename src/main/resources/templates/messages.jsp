<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:th="http://www.thymeleaf.org"
	xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
<head>
<meta th:include="inc/CommonBlocks :: head" th:remove="tag" />
<script th:inline="javascript">
/*<![CDATA[*/			
		function alert_and_redirect(){
		  alert([[${msg}]]);
		  if([[${is_redirect_url}]]) {
		  	location.href = [[${re_url}]];
		  } else {
			history.go(-2);
		  }
		}
/*]]>*/
</script>
</head>
<body th:attr="onload = 'alert_and_redirect()'">	
	<div style="text-align:center;height:300px;margin-top:140px;color:Black;font-size:16pt;">
	    <p th:text="${msg}" />
	</div>
	<div th:include="inc/CommonBlocks :: footer" />
</body>
</html>