<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!doctype html>
<html lang="zh">
<head>
<!-- Required meta tags -->
<meta charset="utf-8" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no" />
<%@include file="/WEB-INF/jsp/common/css.jsp"%>
<title>主机列表</title>
</head>
<body>

	<%@include file="/WEB-INF/jsp/common/nav.jsp"%>
	<div class="container-fluid">
		<div class="w-75 p-3" style="background-color: #eee;">
			<form method="post" action="${MYCONTEXTPATH}/home/hostlist">
				<div class="form-row align-items-center">

					<div class="col-auto">
						<label class="sr-only" for="sname">name</label>
						<div class="input-group mb-2">
							<div class="input-group-prepend">
								<div class="input-group-text">服务名</div>
							</div>
							<input type="text" class="form-control" id="sname" name="sname"
								placeholder="" autofocus="autofocus" value="${sname}"
								autocomplete="off" maxlength="30">
						</div>
					</div>

					<div class="col-auto">
						<button type="submit" class="btn btn-primary mb-2">搜索</button>
					</div>
				</div>
			</form>
		</div>
		<div class="table-responsive">
			<table class="table">
				<thead>
					<tr>
						<th scope="col">#</th>
						<th scope="col">IP</th>
						<th scope="col">端口</th>
						<th scope="col">contextPath</th>
						<th scope="col">服务数量</th>
						<th scope="col">服务名</th>
						<th scope="col">请求方法</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="h" varStatus="vs" items="${cas}">
						<tr
							<c:choose>
						<c:when test="${vs.count%8==1 }"> class="table-primary"</c:when>
						<c:when test="${vs.count%8==2 }"> class="table-secondary"</c:when>
						<c:when test="${vs.count%8==3 }"> class="table-success"</c:when>
						<c:when test="${vs.count%8==4 }"> class="table-danger"</c:when>
						<c:when test="${vs.count%8==5 }"> class="table-warning"</c:when>
						<c:when test="${vs.count%8==6 }"> class="table-info"</c:when>
						<c:when test="${vs.count%8==7 }"> class="table-light"</c:when>
					</c:choose>>
							<td>${vs.count}</td>
							<td><c:out value="${h.ip}"></c:out></td>
							<td><c:out value="${h.port }"></c:out></td>
							<td><c:out value="${h.contextPath }"></c:out></td>
							<td>${h.count}</td>
							<td><c:out value="${h.name }"></c:out></td>
							<td><c:out value="${h.methods }"></c:out></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>

	</div>

	<%@include file="/WEB-INF/jsp/common/js.jsp"%>
</body>
</html>