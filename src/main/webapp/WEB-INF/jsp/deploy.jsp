<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>배포</title>
</head>
<body>
    <form action="fileupload" method="post" enctype="Multipart/form-data">
        APK 파일 : <input type="file" name="fileName" /></br>
        서버 IP : <input type="text" name="serverIp" /></br>
        서버 PORT : <input type="text" name="serverPort" /></br>
        계정 ID : <input type="text" name="accountID" /></br>
        계정 PW : <input type="text" name="accountPW" /></br>
        <input type="submit" value="Submit" />
        <input type="reset" value="cancel" />
    </form>


</body>
</html>
