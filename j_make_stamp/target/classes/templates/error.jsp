<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:th="http://www.thymeleaf.org"
	xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
<head>
<meta th:include="inc/CommonBlocks :: head" th:remove="tag" />
</head>
<body>
	<div style="text-align:center;height:300px;margin-top:140px;color:Black;font-size:16pt;">
	    <p>오류가 발생하였습니다.<br />관리자에게 문의하시기 바랍니다.</p>
	</div>
	<div th:include="inc/CommonBlocks :: footer" />
</body>
</html>