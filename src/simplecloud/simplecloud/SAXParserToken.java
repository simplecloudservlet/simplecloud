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



public class SAXParserToken {

	private String TOKEN_ID="0";

	private String TENANT_ID="0";
	
	public String getTokenID(){

		return TOKEN_ID;

	}//fim getTokenID

	public String getTenantID(){

		return TENANT_ID;

	}//fim getTenantID

	public void setTokenID(String token){
		TOKEN_ID=token;
	}//fim setTokenID

	public void setTenantID(String tenant){
		TENANT_ID=tenant;
	}//fim setTokenID

	//Construtor
	public SAXParserToken(){

		super();

	}//fimdoconstrutor

	public java.util.ArrayList processar(String tipo, String arquivo){

		java.util.ArrayList lista = new java.util.ArrayList();

		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			javax.xml.parsers.SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler(tipo);

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(arquivo.getBytes());
			parser.parse(is, handler);

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
			}//fim if
		}//fim startElement

		//Disparado quando a tag de fim eh encontrada
		@Override
		public void endElement(String uri, String localName, 
				String qName) throws SAXException {

			//String tipo = getTipo();

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
		String expires="";

	}//fim classe interna

	//Inicia a classe
	public static void main(String args[]){
		new SAXParserToken();

	}//finalMain

}//fim classe

