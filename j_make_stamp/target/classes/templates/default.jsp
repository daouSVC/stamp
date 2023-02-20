<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:th="http://www.thymeleaf.org"
	xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3">
<head>
<meta th:include="inc/CommonBlocks :: head" th:remove="tag" />
</head>
<body>
	Welcome!!
	<div th:include="inc/CommonBlocks :: footer" />
</body>
</html>