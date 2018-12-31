<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!doctype html>
<html lang="zh">
<head>
<meta charset="utf-8" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no" />
<%@include file="/WEB-INF/jsp/common/css.jsp"%>
<title>登录</title>
</head>
<body>

	<div class="jumbotron">

		<form method="post" action="${MYCONTEXTPATH}/login">
			<div class="form-row align-items-center">
				<div class="col-auto">
					<input type="text" class="form-control mb-2" id="username"
						required="required" autocomplete="off" placeholder="username"
						name="username">
				</div>
				<div class="col-auto">
					<div class="input-group mb-2">
						<input type="password" class="form-control" id="password"
							placeholder="password" required="required" autocomplete="off"
							name="password">
					</div>
				</div>

				<div class="col-auto">
					<button type="submit" class="btn btn-primary mb-2">Submit</button>
				</div>
			</div>
		</form>
	</div>
	<%@include file="/WEB-INF/jsp/common/js.jsp"%>


</body>
</html>