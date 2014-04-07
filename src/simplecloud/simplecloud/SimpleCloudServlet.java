package simplecloud;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import simplecloud.HTMLFilter;


public class SimpleCloudServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		String tipo = request.getParameter("tipo");

		if(tipo.equals("hostStatus")){

			out.println("<h3>" + "Passei por aqui" + "</h3>");

			String []retorno = new String[4];
			for(int i=0; i<retorno.length; i++){
				retorno[i]="";
			} 

			String username = request.getParameter("username");
			String password = request.getParameter("password");

			String ip = request.getParameter("ip");			
			String hostQuery = request.getParameter("hostQuery");
			String token = request.getParameter("token");
			String tenantID = request.getParameter("tenant");

			String retornoHostStatus="0";

			try {
				retorno=hostStatus(ip, hostQuery, token, tenantID);				
				out.println("<h3>" + 
						"[Retorno[0]:"+retorno[0]+"]" +
						"[Retorno[1]:"+retorno[1]+"]" +
						"[Retorno[2]:"+retorno[2]+"]" +
						"[Retorno[3]:"+retorno[3]+"]" +
						"</h3>");
				retornoHostStatus=
						"\n\nMemory Total: [" + retorno[0] + "] "+
								"\n\nCPU Total: [" + retorno[1] + "] "+
								"\n\nMemory used now: [" +retorno[2] + "] "+
								"\n\nCPU used now: [" + retorno[3] + "]";

			} catch (Exception e){
				//retornoHostStatus = "2";
				out.println("<h3>" + "["+hostQuery+"]"+"[Retorno[0]:"+retorno[0]+"]"+ 
						"\n" + e.getMessage() + "</h3>");
			}
			//Retorno soh do hostStatus
			response.sendRedirect("index.jsp?username="+username+"&password="+password+"&token="+token+"&ip="+ip+"&tenant="+tenantID+
					"&hostQuery="+hostQuery+"&hostStatus="+retornoHostStatus);

		} else {

			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String ip = request.getParameter("ip");
			String token = request.getParameter("token");
			String tenantID = request.getParameter("tenant");

			ArrayList retornoHosts = new ArrayList();
			ArrayList retornoServers = new ArrayList();

			try {
				retornoHosts=getHosts(username,password,ip,token,tenantID);
			} catch (Exception e){
				out.println("<h3>" + e.getMessage() + "</h3>");
			}

			String urlHosts = "";
			java.util.Iterator itr = retornoHosts.iterator();
			while(itr.hasNext()){
				Object element = itr.next();
				urlHosts +=  element + ";";
			}//fim while

			try {
				retornoServers=getServers(username,password,ip,token,tenantID);

			} catch (Exception e){
				out.println("<h3>" + e.getMessage() + "</h3>");
			}

			String urlServers = "";
			itr = retornoServers.iterator();
			SAXParserServers.Tag element;

			String []retornoDetails = new String[4];
			for(int i=0; i<retornoDetails.length; i++)
				retornoDetails[i]="";
			
			while(itr.hasNext()){
				element = (SAXParserServers.Tag) itr.next();

				try{
					//Para cada server(VM) adquire o host no qual ele foi instanciado
					retornoDetails=serverDetails(ip,element.server_id,token,tenantID);

					urlServers +=  element.server_id + "("+retornoDetails[0]+");";
					out.println("["+urlServers+"]");
				}
				catch (Exception e){
					out.println("<h3>" + e.getMessage() + "</h3>");
				}
			}//fim while


			//Todos os outros retornos
			response.sendRedirect("index.jsp?username="+username+"&password="+password+"&token="+token+"&ip="+ip+"&tenant="+tenantID+
					"&servers="+urlServers+"&hosts="+urlHosts);

		}//fim else

	}//fim doGet

	public String[] serverDetails (String ip, String serverQuery, String token, String tenantID) throws Exception{

		String[] result=new String[4];
		for(int i=0; i<result.length; i++){
			result[i]="";
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String porta="8774";

			//token="f0193900c78d45fa8fb46c8a3a02fd3e";
			//Adquire informacoes do host fisico
			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenantID+"/servers/"+serverQuery);

			//URL url = new URL("http://172.16.0.3:8774/v2/fbc26bace53e4dd7928f06c90608c416/os-hosts/node-3.domain.tld");
			HttpGet httpget = new HttpGet(url.toURI());            
			httpget.setHeader("X-Auth-Token",token);  
			httpget.addHeader("Accept", "application/xml");

			System.out.println("Executing request: " + httpget.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};

			String responseBody = httpclient.execute(httpget, responseHandler);

			//result[0]=responseBody.toString();

			SAXParserServerDetails sax = new SAXParserServerDetails();
			java.util.ArrayList listaServerDetails = sax.processar("getServerDetails",responseBody.toString());

			/*//A linha de baixo funciona para um arquivo fixo
			SAXParserHostStatus sax = new SAXParserHostStatus();
			java.util.ArrayList listaHostStatus = sax.processar("getHostStatus","/usr/local/src/workspace/SimpleCloudServletv7/src/simplecloud/simplecloud/hostStatus.xml");
			 */
			java.util.Iterator itr = listaServerDetails.iterator();
			int indice=0;
			while(itr.hasNext()){
				Object element = itr.next();
				if (indice==0){
					//Host no qual a VM estah instanciada
					result[indice]=element.toString();
					indice++;
				}//fim if
			}//fim while

		} finally {
			httpclient.close();
		}
		return result;

	}//fim serverDetails

	public String[] hostStatus (String ip, String hostQuery, String token, String tenantID) throws Exception{

		String[] result=new String[4];
		for(int i=0; i<result.length; i++){
			result[i]="";
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String porta="8774";

			//token="f0193900c78d45fa8fb46c8a3a02fd3e";
			//Adquire informacoes do host fisico
			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenantID+"/os-hosts/"+hostQuery);

			//URL url = new URL("http://172.16.0.3:8774/v2/fbc26bace53e4dd7928f06c90608c416/os-hosts/node-3.domain.tld");
			HttpGet httpget = new HttpGet(url.toURI());            
			httpget.setHeader("X-Auth-Token",token);  
			httpget.addHeader("Accept", "application/xml");

			System.out.println("Executing request: " + httpget.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};

			String responseBody = httpclient.execute(httpget, responseHandler);

			//result[0]=responseBody.toString();

			SAXParserHostStatus sax = new SAXParserHostStatus();
			java.util.ArrayList listaHostStatus = sax.processar("getHostStatus",responseBody.toString());

			/*//A linha de baixo funciona para um arquivo fixo
			SAXParserHostStatus sax = new SAXParserHostStatus();
			java.util.ArrayList listaHostStatus = sax.processar("getHostStatus","/usr/local/src/workspace/SimpleCloudServletv7/src/simplecloud/simplecloud/hostStatus.xml");
			 */
			java.util.Iterator itr = listaHostStatus.iterator();
			int indice=0;
			while(itr.hasNext()){
				Object element = itr.next();
				if (indice==0){
					//System.out.print("\nMemoria total:" + element);
					result[indice]=element.toString();
					indice++;
				}
				else
					if (indice==1){
						//System.out.print("\nCPU total:" + element);
						result[indice]=element.toString();
						indice++;
					}
					else
						if (indice==2){
							//System.out.print("\nMemoria utilizada:" + element);
							result[indice]=element.toString();
							indice++;
						}
						else
							if (indice==3){
								//System.out.print("\nCPU utilizada:" + element);
								result[indice]=element.toString();
								indice=0;
							}//fim if
			}//fim while

		} finally {
			httpclient.close();
		}
		return result;

	}//fim hostStatus

	/////////////
	public String[] getToken(String username, String password, String ip) throws Exception{

		String[] result=new String[4];
		for(int i=0; i<result.length; i++){
			result[i]="";
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			//tenant-id: 7afff06b13c0409496813404d6a4b436 --> eh o ID do admin --> adquiro com HTTP GET /tenants (ou # keystone tenant-list)
			//           3a9c94ec-d98b-40cb-b185-f3e109c3732d --> eh o ID da VM ativa (recebe aqui o nome de 'server') --> adquiro com /tenant-id/servers (ou # nova list)
			//			 fb523c47-de76-40b8-aac9-1ffcc8d236d5 --> eh o ID da imagem da VM --> --> adquiro com /tenant-id/images (ou # nova image-list)

			//String tenantID="698be4ebdfd24092b80be1f6b78ff57d";
			//String tenantID="ece11538e44444ea9b9b98f7ce6e2d2d";
			//String IP_token = "+ip+";
			//String IP = "172.16.0.3:8774";
			//String token = "fbabb4c205c24fd3b2debf2c905d4604";
			//String serverVMID = "c2e4c7ca-7c58-4cf4-9d6e-d0897f9a8d68";
			String porta="5000";

			URL url = new URL("http://"+ip+":"+porta+"/v2.0/tokens");
			//URL url = new URL("http://172.16.0.3:8774/v2/"+tenantID+"/os-migrations");
			//URL url = new URL("http://172.16.0.3:8774/v2/36d2a86097f54ca3a5baa1d5bac9431a/servers/13b5186d-883d-46c7-8f73-9d91f2ca4eae/action");

			//Para migrate, o escalonador do openstack escolhe o host de destino
			//URL url = new URL("http://"+IP+"/v2/"+tenantID+"/servers/"+serverVMID+"/action");


			HttpPost httppost = new HttpPost(url.toURI());


			InputStreamEntity reqEntity=null;

			String entrada = "{\"auth\": {\"tenantName\": \""+username+"\",\"passwordCredentials\": {\"username\": \""+username+"\", \"password\": \""+password+"\"}}}"; 
			InputStream saida = new ByteArrayInputStream(entrada.getBytes());
			reqEntity = new InputStreamEntity(
					saida, -1, ContentType.APPLICATION_JSON);			

			reqEntity.setChunked(true);
			httppost.setEntity(reqEntity);			

			//Para receber a resposta em XML
			//httppost.addHeader("Accept", "application/json");
			httppost.addHeader("Accept", "application/xml");

			System.out.println("Executing request: " + httppost.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			String responseBody = httpclient.execute(httppost, responseHandler);
			//System.out.println("----------------------------------------");
			//System.out.println(responseBody);                        
			//-----------------

			/*try {
				BufferedWriter out = new BufferedWriter(new FileWriter("/usr/local/src/workspace/SimpleCloudServletv3/arquivo.xml",false));			
				out.write(responseBody.toString());
				out.close();
			} catch (Exception e){
				e.printStackTrace();
			}*/			

			SAXParserToken sax = new SAXParserToken();
			sax.processar("getToken",responseBody.toString());
			result[0] = sax.getTokenID();
			result[1] = sax.getTenantID();



		} finally {
			httpclient.close();
		}

		return result;
	}

	public String migrate(String username, String password, String ip, String serverMigrate, String toHost, String token, String tenant) throws Exception{

		String retorno = "0";
		/*if (args.length != 1)  {
		System.out.println("File path not given");
		System.exit(1);
	}*/
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			//tenant-id: 7afff06b13c0409496813404d6a4b436 --> eh o ID do admin --> adquiro com HTTP GET /tenants (ou # keystone tenant-list)
			//           3a9c94ec-d98b-40cb-b185-f3e109c3732d --> eh o ID da VM ativa (recebe aqui o nome de 'server') --> adquiro com /tenant-id/servers (ou # nova list)
			//			 fb523c47-de76-40b8-aac9-1ffcc8d236d5 --> eh o ID da imagem da VM --> --> adquiro com /tenant-id/images (ou # nova image-list)

			//URL url = new URL("http://172.16.0.2:5000/v2.0/tokens");
			String porta="8774";
			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenant+"/servers/"+serverMigrate+"/action");
			//URL url = new URL("http://172.16.0.2:8774/v2/b8d208064f9c47ddaa42ac5cf1da79e4/servers");

			HttpPost httppost = new HttpPost(url.toURI());

			//File file = new File(args[0]);
			//String entrada = "{\"auth\": {\"tenantName\": \"admin\", \"passwordCredentials\": {\"username\": \"admin\", \"password\": \"admin\"}}}";
			String entrada = "{\"os-migrateLive\": {\"host\": \""+toHost+"\",\"block_migration\": true,\"disk_over_commit\": false}}";
			InputStream saida = new ByteArrayInputStream(entrada.getBytes());

			//Para ler o conteudo do arquivo
			//InputStreamEntity reqEntity = new InputStreamEntity(
			//new FileInputStream(file), -1, ContentType.APPLICATION_JSON);
			InputStreamEntity reqEntity = new InputStreamEntity(
					saida, -1, ContentType.APPLICATION_JSON);
			reqEntity.setChunked(true);
			httppost.setEntity(reqEntity);			

			//Para receber a resposta em XML
			httppost.addHeader("Accept", "application/xml");

			httppost.setHeader("X-Auth-Token",token); 			

			System.out.println("Executing request: " + httppost.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			String responseBody = httpclient.execute(httppost, responseHandler);
			//System.out.println("----------------------------------------");
			//System.out.println(responseBody);  
			retorno = responseBody;
			//-----------------

		} finally {
			httpclient.close();
		}
		return retorno;
	}//fimMigrate



	public java.util.ArrayList getHosts(String username, String password, String ip, String token, String tenant) throws Exception{

		java.util.ArrayList listaHosts = new java.util.ArrayList();


		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			//tenant-id: 7afff06b13c0409496813404d6a4b436 --> eh o ID do admin --> adquiro com HTTP GET /tenants (ou # keystone tenant-list)
			//           3a9c94ec-d98b-40cb-b185-f3e109c3732d --> eh o ID da VM ativa (recebe aqui o nome de 'server') --> adquiro com /tenant-id/servers (ou # nova list)
			//			 fb523c47-de76-40b8-aac9-1ffcc8d236d5 --> eh o ID da imagem da VM --> --> adquiro com /tenant-id/images (ou # nova image-list)

			//String tenantID="698be4ebdfd24092b80be1f6b78ff57d";
			//String tenantID="ece11538e44444ea9b9b98f7ce6e2d2d";
			//String IP_token = "+ip+";
			//String IP = "172.16.0.3:8774";
			//String token = "fbabb4c205c24fd3b2debf2c905d4604";
			//String serverVMID = "c2e4c7ca-7c58-4cf4-9d6e-d0897f9a8d68";
			String porta="8774";

			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenant+"/os-hosts");
			//URL url = new URL("http://172.16.0.3:8774/v2/"+tenantID+"/os-migrations");
			//URL url = new URL("http://172.16.0.3:8774/v2/36d2a86097f54ca3a5baa1d5bac9431a/servers/13b5186d-883d-46c7-8f73-9d91f2ca4eae/action");

			//Para migrate, o escalonador do openstack escolhe o host de destino
			//URL url = new URL("http://"+IP+"/v2/"+tenantID+"/servers/"+serverVMID+"/action");


			HttpGet httpget = new HttpGet(url.toURI());


			httpget.setHeader("X-Auth-Token",token); 
			httpget.addHeader("Accept", "application/xml");

			System.out.println("Executing request: " + httpget.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			String responseBody = httpclient.execute(httpget, responseHandler);

			SAXParserHosts sax = new SAXParserHosts();
			listaHosts =sax.processar ("getHosts",responseBody.toString());

		} finally {
			httpclient.close();
		}

		return listaHosts;
	}

	public java.util.ArrayList getServers(String username, String password, String ip, String token, String tenant) throws Exception{

		java.util.ArrayList listaServers = new java.util.ArrayList();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			//tenant-id: 7afff06b13c0409496813404d6a4b436 --> eh o ID do admin --> adquiro com HTTP GET /tenants (ou # keystone tenant-list)
			//           3a9c94ec-d98b-40cb-b185-f3e109c3732d --> eh o ID da VM ativa (recebe aqui o nome de 'server') --> adquiro com /tenant-id/servers (ou # nova list)
			//			 fb523c47-de76-40b8-aac9-1ffcc8d236d5 --> eh o ID da imagem da VM --> --> adquiro com /tenant-id/images (ou # nova image-list)

			//String tenantID="698be4ebdfd24092b80be1f6b78ff57d";
			//String tenantID="ece11538e44444ea9b9b98f7ce6e2d2d";
			//String IP_token = "+ip+";
			//String IP = "172.16.0.3:8774";
			//String token = "fbabb4c205c24fd3b2debf2c905d4604";
			//String serverVMID = "c2e4c7ca-7c58-4cf4-9d6e-d0897f9a8d68";
			String porta="8774";

			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenant+"/servers");
			//URL url = new URL("http://172.16.0.3:8774/v2/"+tenantID+"/os-migrations");
			//URL url = new URL("http://172.16.0.3:8774/v2/36d2a86097f54ca3a5baa1d5bac9431a/servers/13b5186d-883d-46c7-8f73-9d91f2ca4eae/action");

			//Para migrate, o escalonador do openstack escolhe o host de destino
			//URL url = new URL("http://"+IP+"/v2/"+tenantID+"/servers/"+serverVMID+"/action");


			HttpGet httpget = new HttpGet(url.toURI());


			httpget.setHeader("X-Auth-Token",token); 
			httpget.addHeader("Accept", "application/xml");

			System.out.println("Executing request: " + httpget.getRequestLine());

			//------------
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			String responseBody = httpclient.execute(httpget, responseHandler);

			SAXParserServers sax = new SAXParserServers();
			listaServers=sax.processar ("getServers",responseBody.toString());			

		} finally {
			httpclient.close();
		}

		return listaServers;
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws IOException, ServletException {

		String title = "Simple Cloud Dashboard";

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String ip = request.getParameter("ip");
		String serverMigrate = request.getParameter("serverMigrate");
		String toHost = request.getParameter("toHost");
		String token = request.getParameter("token");
		String tenantID = request.getParameter("tenant");
		String tipo = request.getParameter("tipo");

		String []retorno = new String[4];
		for(int i=0; i<retorno.length; i++){
			retorno[i]="";
		}//fim for    

		out.println("<h3>" + tipo + "</h3>");

		String tokenRetorno="";
		String tenantRetorno="";
		
		if(tipo.equals("getToken")){
			try {

				//out.print(processar(username,password));
				retorno=getToken(username,password,ip);

				tokenRetorno=retorno[0];
				tenantRetorno=retorno[1];
				
			} catch (Exception e){
				out.println("<h3>" + e.getMessage() + "</h3>");
			} 

		}//fimif

		String retornoMigrate = "1";
		if(tipo.equals("migrate")){
			try {
				retornoMigrate=migrate(username, password, ip, serverMigrate, toHost, token, tenantID);
				if(retornoMigrate.equals(""))
					retornoMigrate="Start migrating of Server: [" + serverMigrate + "] -> Host: [" + toHost + "]";
			} catch (Exception e){
				retornoMigrate = "2";
				out.println("<h3>" + e.getMessage() + "</h3>");
			}

			tokenRetorno=token;
			tenantRetorno=tenantID;
			
		}//fimif

		response.sendRedirect("index.jsp?username="+username+"&password="+password+"&token="+tokenRetorno+"&ip="+ip+"&tenant="+tenantRetorno+"&status="+retornoMigrate+"&serverMigrate="+serverMigrate+"&toHost="+toHost);

		//doGet(request, response);

	}//fim doPost

}//fim classe
