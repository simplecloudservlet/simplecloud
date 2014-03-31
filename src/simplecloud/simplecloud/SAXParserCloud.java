package simplecloud;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;
import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



public class SAXParserCloud {

	private String TOKEN_ID="0";

	private String TENANT_ID="0";

	private String HOSTS_ID="0";

	private String SERVERS_ID="0";

	public String getTokenID(){

		return TOKEN_ID;

	}//fim getTokenID

	public String getTenantID(){

		return TENANT_ID;

	}//fim getTenantID

	public String getHostsID(){

		return HOSTS_ID;

	}//fim geHostsID

	public String getServersID(){

		return SERVERS_ID;

	}//fim getServersID

	public void setTokenID(String token){
		TOKEN_ID=token;
	}//fim setTokenID

	public void setTenantID(String tenant){
		TENANT_ID=tenant;
	}//fim setTokenID

	public void setHostsID(String hosts){
		HOSTS_ID=hosts;
	}//fim setHostsID

	public void setServersID(String servers){
		SERVERS_ID=servers;
	}//fim setTokenID

	//Construtor
	public SAXParserCloud(){

		boolean arquivo_fixo=true;
		if(arquivo_fixo){
			//Busca pela tag <host ... /> no arquivo XML
			//
			//Uso uma lista porque nao sei quantos hosts podem ser retornados
			java.util.ArrayList listaHosts = processar("getHosts", "src/arquivo.xml");
			java.util.Iterator itr = listaHosts.iterator();
			while(itr.hasNext()){
				Object element = itr.next();
				System.out.print(element + " ");			
			}//fim while
		}//fim if

	}//fimdoconstrutor

	public java.util.ArrayList processar(String tipo, String arquivo){

		java.util.ArrayList listaHosts = new java.util.ArrayList();

		java.util.ArrayList listaServers = new java.util.ArrayList();


		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			javax.xml.parsers.SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler(tipo);
			//Apenas para testar com um arquivo fixo
			boolean arquivo_fixo=true;
			if(arquivo_fixo){
				System.out.println("[[["+arquivo+"]]]");//Como processar o xml que estah na memoria???
				parser.parse(new File("src/simplecloud/simplecloud/arquivo.xml"), handler);
			} else {
				// convert String into InputStream
				InputStream is = new ByteArrayInputStream(arquivo.getBytes());
				parser.parse(is, handler);
			}//fim else

			//Printing the list obtained from XML
			for ( Tag doc : handler.docList){
				if (tipo.equals("getHosts")){
					//Armazena apenas os objetos cujo hosts sao do tipo 'compute'
					if(doc.service.equals("compute")){
						System.out.println(doc.host_name);
						listaHosts.add(doc.host_name);
					}//fim if

				}//fim if
				else if(tipo.equals("getServers")){
					listaServers.add(doc);

				}//fimIfServers

			}//fim for
		} catch (Exception e){
			e.printStackTrace();
		}

		return listaHosts;

	}//fim processar


	/**
	 * The Handler for SAX Events.
	 */	 
	class SAXHandler extends DefaultHandler {

		String TIPO = "0";

		public SAXHandler(String tipo){
			TIPO = tipo;
		}//fimSaxHandler

		public String getTipo(){
			return TIPO;
		}//fimGetTipo

		//Importante: docList e doc sao globais (variaveis de instancia)
		//
		//docList 'guarda' uma lista de objetos 'doc'
		List<Tag> docList = new ArrayList<>();
		Tag doc = null; //1 objeto do tipo Tag, que contem varios campos (doc.id, doc.name, doc.expires,...) 
		//String content = null;

		@Override
		//Disparado quando a tag de inicio eh encontrada
		public void startElement(String uri, String localName, 
				String qName, Attributes attributes) 
						throws SAXException {

			String tipo = getTipo();

			if(tipo.equals("getToken")){//tag atual. Ex.: <host...>
				switch(qName){
				//Cria um novo objeto token quando a tag eh encontrada
				case "token":
					doc = new Tag();
					doc.id = attributes.getValue("id");
					doc.expires = attributes.getValue("expires");
					System.out.println(doc.id.toString());
					setTokenID(doc.id.toString());
					break;
				case "tenant":
					doc = new Tag();
					doc.id = attributes.getValue("id");
					//doc.name = attributes.getValue("name");
					System.out.println(doc.id.toString());
					setTenantID(doc.id.toString());
					break;
				}//fimdoSwitch
			} else

				if(tipo.equals("getHosts")){

					switch(qName){ //tag atual. Ex.: <host...>

					case "host":
						doc = new Tag(); //Cria um novo objeto para guardar o valor
						doc.host_name = attributes.getValue("host_name"); //Ex.: <host ... host_name="..." .../>
						doc.service = attributes.getValue("service"); //Ex.: <host ... service="..." .../>
						//System.out.println("\tHOST_NAME:\t" + doc.host_name);
						//System.out.println("\tSERVICE:\t" + doc.service);
						break;

					default:
						break;

					}//fim do switch

				}//fim if

				else
					if(tipo.equals("getServers")){ //tipo
						switch(qName){ //tag atual. Ex.: <server...>
						case "server":
							doc = new Tag();//Cria um novo objeto para guardar o valor
							doc.server_id = attributes.getValue("id");
							doc.server_name = attributes.getValue("name");
							break;
						default:
							break;
						}//fimdoswitch
					}//fimdoif

		}//fim startElement

		//Disparado quando a tag de fim eh encontrada
		@Override
		public void endElement(String uri, String localName, 
				String qName) throws SAXException {
			String tipo = getTipo();

			docList.add(doc);
		}//fim endElement

		//Trata campos dentro da 'hierarquia' da tag
		//Ex.: <host>
		//        <nome1>...</nome1>
		//        <nome2>...</nome2>
		//     </host>
		//Nota: nao utilizamos esse metodo
		/*@Override
		public void characters(char[] ch, int start, int length) 
				throws SAXException {
			System.out.println("Passei por aqui"); 
			//content = String.copyValueOf(ch, start, length).trim();
		}//fim characters
		 */

	}//fim classe SaxHandler

	//Essa classe cria um objeto do tipo Tag. 
	//O objeto possui 'campos' que serao utilizados para guardar os valores do arquivo XML
	//Ex.: tag.id
	//     tag.name
	//     tag.expires....
	class Tag {

		String id="";
		String server_id="";
		String server_name="";
		String expires="";
		String service="";
		String host_name="";
		String zone="";
		String servers="";

	}//fim classe interna

	//Inicia a classe
	public static void main(String args[]){
		new SAXParserCloud();

	}//finalMain

}//fim classe

