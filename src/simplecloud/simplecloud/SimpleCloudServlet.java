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

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String ip = request.getParameter("ip");
		String token = request.getParameter("token");
		String tenant = request.getParameter("tenant");
		String tipo = request.getParameter("tipo");

		ArrayList retornoHosts = new ArrayList();
		ArrayList retornoServers = new ArrayList();

		try {
			retornoHosts=getHosts(username,password,ip,token,tenant);
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
			retornoServers=getServers(username,password,ip,token,tenant);
		} catch (Exception e){
			out.println("<h3>" + e.getMessage() + "</h3>");
		}

		String urlServers = "";
		itr = retornoServers.iterator();
		SAXParserServers.Tag element;

		while(itr.hasNext()){
			element = (SAXParserServers.Tag) itr.next();
			urlServers +=  element.server_id + ";";
			out.println("["+urlServers+"]");
		}//fim while

		response.sendRedirect("index.jsp?username="+username+"&password="+password+"&token="+token+"&ip="+ip+"&tenant="+tenant+
				"&servers="+urlServers+"&hosts="+urlHosts);

	}//fim doGet

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
	public String Migrate(String username, String password, String ip, String serversmigrate, String tohost, String token, String tenant) throws Exception{

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
			String porta="8774";

			URL url = new URL("http://"+ip+":"+porta+"/v2/"+tenant+"/servers/"+serversmigrate+"/action");
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
		String serversmigrate = request.getParameter("serversmigrate");
		String tohost = request.getParameter("tohost");

		String []retorno = new String[4];
		for(int i=0; i<retorno.length; i++){
			retorno[i]="";
		}    

		try {

			//out.print(processar(username,password));
			retorno=getToken(username,password,ip);

		} catch (Exception e){
			out.println("<h3>" + e.getMessage() + "</h3>");
		} 
		
		

		String token=retorno[0];
		String tenant=retorno[1];


		response.sendRedirect("index.jsp?username="+username+"&password="+password+"&token="+token+"&ip="+ip+"&tenant="+tenant);
		//doGet(request, response);

	}//fim doPost

}//fim classe
