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

public class SAXParserServers {

	private String SERVERS_ID="0";

	public String getServersID(){

		return SERVERS_ID;

	}//fim getServersID

	public void setServersID(String servers){
		SERVERS_ID=servers;
	}//fim setTokenID

	//Apenas para testes (IMPORTANTE: 'false' para exibir resultado no browser)
	boolean ARQUIVO_SERVERS_FIXO=false;

	//Construtor
	public SAXParserServers(){

		if(ARQUIVO_SERVERS_FIXO){
			//Busca pela tag <host ... /> no arquivo XML
			//
			//Uso uma lista porque nao sei quantos hosts podem ser retornados
			java.util.ArrayList listaServers = processar("getServers", "src/simplecloud/simplecloud/servers.xml");
			java.util.Iterator itr = listaServers.iterator();			 
			while(itr.hasNext()){
				Tag element = (Tag) itr.next();				
				System.out.print("Server: " + element.server_id);
			}//fim while
		}//fim if

	}//fimdoconstrutor

	public java.util.ArrayList processar(String tipo, String arquivo){

		java.util.ArrayList lista = new java.util.ArrayList();

		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			javax.xml.parsers.SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler(tipo);
			//Apenas para testes			

			if(ARQUIVO_SERVERS_FIXO){
				System.out.println("[[["+arquivo+"]]]");//Como processar o xml que estah na memoria???
				parser.parse(new File(arquivo), handler);
			} else {
				// convert String into InputStream
				InputStream is = new ByteArrayInputStream(arquivo.getBytes());
				parser.parse(is, handler);
			}//fim else

			//Printing the list obtained from XML
			for ( Tag doc : handler.docList){

				/*System.out.println(handler.docList.size());
				if(tipo.equals("getServers")){
					System.out.println("Passei por aqui2: doc.server_id: "+doc.server_id);
				}//fimIfServers
				 */

				lista.add(doc);

			}//fim for
		} catch (Exception e){
			e.printStackTrace();
		}

		return lista;

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

			if(tipo.equals("getServers")){ //tipo
				switch(qName){ //tag atual. Ex.: <server...>
				case "server":
					System.out.println("Passei por aqui");
					doc = new Tag();//Cria um novo objeto para guardar o valor
					doc.server_id = attributes.getValue("id");
					doc.server_name = attributes.getValue("name");
					System.out.println("doc.server_id: " + doc.server_id);					
					break;
				default:
					break;
				}//fim switch
			}//fim if

		}//fim startElement

		//Disparado quando a tag de fim eh encontrada
		//
		//Nota: Eh necessario definir 'cada' tipo de qname aqui tb.
		@Override
		public void endElement(String uri, String localName, 
				String qName) throws SAXException {

			String tipo = getTipo();

			if(tipo.equals("getServers")){ //tipo
				switch(qName){ //tag atual. Ex.: <server...>
				case "server":			
					docList.add(doc);
					break;
				default:
					break;
				}//fim endElement
			}//fim if
		}//fim endElement

	}//fim classe SaxHandler

	//Essa classe cria um objeto do tipo Tag. 
	//O objeto possui 'campos' que serao utilizados para guardar os valores do arquivo XML
	//Ex.: tag.id
	//     tag.name
	//     tag.expires....
	class Tag {

		String server_id="";
		String server_name="";

	}//fim classe interna

	public boolean repetido(Tag doc, java.util.ArrayList listaVisitados){

		boolean repetido=false;


		java.util.Iterator itr = listaVisitados.iterator();

		//A lista possui elementos que nao sao
		boolean fim=false;

		while(itr.hasNext() && !repetido && !fim){
			try {
				Tag elementoVisitado = (Tag) itr.next();
				if (elementoVisitado.server_id == doc.server_id)
					repetido=true;
			} catch (Exception e){
				//Se deu erro aqui, nao possui mais elementos na lista
				fim=true;
			}

		}//fim while

		return repetido;

	}//fim repetido


	//Inicia a classe
	public static void main(String args[]){
		new SAXParserServers();

	}//finalMain

}//fim classe

