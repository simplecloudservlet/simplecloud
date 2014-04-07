<html>
	<head>
	<title>Simple Cloud Dashboard</title>
	</head>
	
	<%@ page import="java.io.*,java.net.*,java.util.*, java.util.regex.*, javax.servlet.*, javax.servlet.http.*" %>
	
	<body bgcolor="white">
		<h3>Simple Cloud Dashboard</h3>

		
		<%= new java.util.Date() %>

		<br>
		<br>
		<h4>Identity Services</h4>
		<p>
		<form action="SimpleCloudServlet" method=POST>
		<input type="hidden" size=20 name=tipo value=getToken>	
			User*:
			<% String username=request.getParameter("username"); 
				if (username==null)
					username="admin";			
			%>
				<input type=text size=20 name=username value=
				<%= username %>>
				<br/>
			Password*: 
			<% String password=request.getParameter("password"); 
				if (password==null)
					password="admin";			
			%>
				<input type=text size=20 name=password value=
				<%= password %>>
				<br/>	
			IP*:
			<% String ip=request.getParameter("ip"); 
				if(ip==null)
					ip="172.16.0.3";
			%>
				<input type=text size=20 name=ip value=
				<%= ip %>>
				<br/>
			Token:
			<% 
			String token=request.getParameter("token");		
			%>
			<input type=text size=40 name=token value=
			<%= token %>
			readonly>
			TenantID:
				<% 
				String tenant=request.getParameter("tenant");		
				%>
				<input type=text size=40 name=tenant value=
				<%= tenant %>
				readonly>
			<input type="submit" name="POST" value="POST">
				
		</form>
		
		<br>
		<br>
		<%//esse e o form do Hosts%>
		<form action="SimpleCloudServlet" method=GET>
			<input type="hidden" size=20 name=tipo value=getHosts>	
			<input type="hidden" size=20 name=username value=
				<%= username %>>
			<input type="hidden" size=20 name=password value=
				<%= password %>>
			<input type="hidden" size=20 name=ip value=
				<%= ip %>>
			<input type="hidden" size=20 name=token value=
			<%= token %>>
			<input type="hidden" size=20 name=tenant value=
				<%= tenant %>>
				<br/>
			Hosts:
				<% 
				String hosts=request.getParameter("hosts");		
				%>
				<input type=text size=50 name=hosts value=
				<%= hosts %>
				readonly>
				<input type="submit" name="GET" value="GET">
				<br/>
		</form>
		
		<%//esse e o form do Servers%>
		<form action="SimpleCloudServlet" method=GET>
			<input type="hidden" size=20 name="tipo" value="getServers">
			<input type="hidden" size=20 name=username value=
				<%= username %>>
			<input type="hidden" size=20 name=password value=
				<%= password %>>
			<input type="hidden" size=20 name=ip value=
				<%= ip %>>
			<input type="hidden" size=20 name=token value=
			<%= token %>>
			<input type="hidden" size=20 name=tenant value=
				<%= tenant %>>
			
				<br/>
			Servers:
				<% 
				String servers=request.getParameter("servers");		
				%>
				<input type=text size=50 name=servers value=
				<%= servers %>
				readonly>
				<input type="submit" name="GET" value="GET">
				<br/>
		</form>
		
			
	<form action="SimpleCloudServlet" method=POST>
		<input type="hidden" size=20 name=tipo value="migrate">
			<input type="hidden" size=20 name=username value=
				<%= username %>>
			<input type="hidden" size=20 name=password value=
				<%= password %>>
			<input type="hidden" size=20 name=ip value=
				<%= ip %>>
			<input type="hidden" size=20 name=token value=
			<%= token %>>
			<input type="hidden" size=20 name=tenant value=
				<%= tenant %>>
			<input type="hidden" size=20 name=servers value=
				<%= servers %>>
			<input type="hidden" size=20 name=hosts value=
				<%= hosts %>>
			<% String serverMigrate=request.getParameter("serverMigrate"); 			
			%>
			ServerMigrate*:
			<input type=text size=20 name=serverMigrate value=
				<%= serverMigrate %>>				
			<br/>
			<% String toHost=request.getParameter("toHost"); 			
			%>
			ToHost*:
			<input type=text size=20 name=toHost value=
				<%= toHost %>>
			
			<input type="submit" name="POST" value="POST">
			<br/>
		</form>

		<% String retornoMigrate=request.getParameter("status"); 			
		%>
		<textarea rows="4" cols="100"><%= retornoMigrate %></textarea>	
		
		
		<br>
		<br>
		<h4>Hosts Status</h4>
		<form action="SimpleCloudServlet" method=GET>		
			<input type="hidden" size=20 name=tipo value="hostStatus">
			<input type="hidden" size=20 name=username value=
				<%= username %>>
			<input type="hidden" size=20 name=password value=
				<%= password %>>
			<input type="hidden" size=20 name=ip value=
				<%= ip %>>
			<input type="hidden" size=20 name=token value=
				<%= token %>>
			<input type="hidden" size=20 name=tenant value=
				<%= tenant %>>		
			Host Query*:
			<% String hostQuery=request.getParameter("hostQuery"); 			
			%>
			<input type=text size=20 name=hostQuery value=
				<%= hostQuery %>>		
			<input type="submit" name="GET" value="GET">		
		</form>
		<% String hostStatus=request.getParameter("hostStatus"); 			
		%>
		<textarea rows="4" cols="100"><%= hostStatus %></textarea>	
		<p>
		...
		<p><a href="SimpleCloudServlet?dataname=foo&amp;datavalue=bar" >Example of internal link</a>
		
		
		
		
	</body>
</html>
