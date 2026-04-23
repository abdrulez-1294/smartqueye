<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartQueue — Error</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #0f0f1a, #1a1a2e);
            min-height: 100vh; display: flex; align-items: center; justify-content: center;
            color: #e2e8f0; margin: 0;
        }
        .error-box {
            text-align: center; max-width: 520px; padding: 3rem 2rem;
            background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);
            border-radius: 20px;
        }
        .error-icon { font-size: 4rem; color: #ef4444; margin-bottom: 1rem; }
        h1 { font-size: 1.8rem; font-weight: 800; margin-bottom: 0.5rem; }
        p  { color: #94a3b8; margin-bottom: 1.5rem; }
        .err-code { font-size: 0.82rem; color: #6c63ff; background: rgba(108,99,255,0.1);
                    padding: 4px 12px; border-radius: 6px; display: inline-block; margin-bottom: 1rem; }
        .btn-home {
            display: inline-block; padding: 0.75rem 2rem; border-radius: 12px;
            background: linear-gradient(135deg, #6c63ff, #8b5cf6);
            color: #fff; text-decoration: none; font-weight: 600; transition: all .2s;
        }
        .btn-home:hover { transform: translateY(-2px); color: #fff; }
    </style>
</head>
<body>
    <div class="error-box">
        <div class="error-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>

        <%
            Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
            String  message    = (String)  request.getAttribute("jakarta.servlet.error.message");
            if (statusCode == null) statusCode = 500;
            if (message == null || message.isEmpty()) message = "An unexpected error occurred.";
        %>

        <div class="err-code">Error <%= statusCode %></div>
        <h1><%= statusCode == 404 ? "Page Not Found" : "Something Went Wrong" %></h1>
        <p><%= statusCode == 404
                ? "The page you are looking for does not exist or has been moved."
                : "The server encountered an error. Please try again or go back home." %>
        </p>

        <a href="<%= request.getContextPath() %>/index.jsp" class="btn-home">
            <i class="bi bi-house-fill me-2"></i>Back to Home
        </a>
    </div>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</body>
</html>
