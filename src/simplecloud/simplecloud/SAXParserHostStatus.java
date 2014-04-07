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



public class SAXParserHostStatus {

	//Apenas para testes
	boolean ARQUIVO_FIXO=false;

	//Construtor
	public SAXParserHostStatus(){

		//Apenas para testes
		if(ARQUIVO_FIXO){
			//Busca pela tag <host ... /> no arquivo XML
			//
			//Uso uma lista porque nao sei quantos hosts podem ser retornados
			java.util.ArrayList listaHosts = processar("getHostStatus", "src/simplecloud/simplecloud/hostStatus.xml"); //
			java.util.Iterator itr = listaHosts.iterator();
			int indice=0;
			while(itr.hasNext()){
				Object element = itr.next();
				
				if (indice==0){
					System.out.print("\nMemoria total:" + element);
					indice++;
				}
				else
				if (indice==1){
					System.out.print("\nCPU total:" + element);
					indice++;
				}
				else
				if (indice==2){
					System.out.print("\nMemoria utilizada:" + element);
					indice++;
				}
				else
				if (indice==3){
					System.out.print("\nCPU utilizada:" + element);
					indice=0;
				}
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
			if(ARQUIVO_FIXO){
				System.out.println("[[["+arquivo+"]]]");//Como processar o xml que estah na memoria???
				parser.parse(new File(arquivo), handler);
			} else {
				// convert String into InputStream
				InputStream is = new ByteArrayInputStream(arquivo.getBytes());
				parser.parse(is, handler);
				//A linha de baixo funciona para um arquivo fixo
				//parser.parse(new File(arquivo), handler);
			}//fim else

			//Printing the list obtained from XML
			int indice=0;
			for ( Tag doc : handler.docList){
				if (tipo.equals("getHostStatus"))
					//Armazena apenas os objetos cujo hosts sao do tipo 'compute'
					if(doc.status.equals("(total)")){
						System.out.println(handler.docList.indexOf(doc));
						//Adquire o indice atual
						indice=handler.docList.indexOf(doc);
						//Guarda o valor do proximo objeto (ou seja, lah embaixo, qdo encontra o status, o proximo objeto possui a memoria)
						lista.add(handler.docList.get(indice+1).memory);
						lista.add(handler.docList.get(indice+2).cpu);

					}//fim if
					if(doc.status.equals("(used_now)")){
						System.out.println(handler.docList.indexOf(doc));
						//Adquire o indice atual
						indice=handler.docList.indexOf(doc);
						//Guarda o valor do proximo objeto (ou seja, lah embaixo, qdo encontra o status, o proximo objeto possui a memoria)
						lista.add(handler.docList.get(indice+1).memory);
						lista.add(handler.docList.get(indice+2).cpu);
				}//fim if

			}//fim for
		} catch (Exception e){
			e.printStackTrace();
		}

		//lista.add("Teste");
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
		String content = null;

		@Override
		//Disparado quando a tag de inicio eh encontrada
		public void startElement(String uri, String localName, 
				String qName, Attributes attributes) 
						throws SAXException {

			String tipo = getTipo();

			if(tipo.equals("getHostStatus")){

				//if (qName.equals("resource")){
				//	System.out.println("Passei por aqui: "+attributes.getIndex("cpu"));
				//}
				System.out.println("["+qName+"]");

				if (qName.equals("memory_mb")){
					System.out.println("Passei por aqui (STATUS): "+content);
				}
				if (qName.equals("disk_gb")){
					System.out.println("Passei por aqui (CPU): "+content);
				}
				
				switch(qName){ //tag atual. Ex.: <host...>

				case "memory_mb": //Adquire o status aqui: (total), (used_now), ...
					if (content!=null){
						doc = new Tag(); //Cria um novo objeto para guardar o valor					
						doc.status = content; 
						System.out.println("\tSTATUS:\t" + doc.status);
					}//fim if
					break;
				
				case "host": //Adquire a memoria aqui
					if (content!=null){
						doc = new Tag(); //Cria um novo objeto para guardar o valor					
						doc.memory = content; 						
						System.out.println("\tMEMORY_MB:\t" + doc.memory);
					}//fim if
					break;
					
				case "disk_gb": //Adquire a cpu aqui
					if (content!=null){
						doc = new Tag(); //Cria um novo objeto para guardar o valor					
						doc.cpu = content; 						
						System.out.println("\tCPU:\t" + doc.cpu);
					}//fim if
					break;

				default:
					break;

				}//fim do switch

			}//fim if

		}//fim startElement

		//Disparado quando a tag de fim eh encontrada
		//
		//Nota: Eh necessario definir 'cada' tipo de qname aqui tb.
		@Override
		public void endElement(String uri, String localName, 
				String qName) throws SAXException {

			String tipo = getTipo();
			if(tipo.equals("getHostStatus")){

				switch(qName){ //tag atual. Ex.: <host...>

				case "memory_mb": //Adquire o status aqui: (total), (used_now), ...
					docList.add(doc);
					break;
				
				case "host": //Adquire o valor da memory aqui (Note que o parser estah deslocado em 1 passo)
					docList.add(doc);
					break;
					
				case "disk_gb": //Adquire o valor da cpu aqui (Note que o parser estah deslocado em 1 passo)
					docList.add(doc);
					break;					
					
				default:					
					break;
				}//fim switch
			}//fim if

		}//fim endElement

		//Trata campos dentro da 'hierarquia' da tag
		//Ex.: <host>
		//        <nome1>...</nome1>
		//        <nome2>...</nome2>
		//     </host>

		@Override
		public void characters(char[] ch, int start, int length) 
				throws SAXException {
			//if (content.equals("memory_mb"))
			//	System.out.println("Passei por aqui");
			content = String.copyValueOf(ch, start, length).trim();

		}//fim characters

	}//fim classe SaxHandler

	//Essa classe cria um objeto do tipo Tag. 
	//O objeto possui 'campos' que serao utilizados para guardar os valores do arquivo XML
	//Ex.: tag.id
	//     tag.name
	//     tag.expires....
	class Tag {

		String status="";
		String memory="";
		String cpu="";

	}//fim classe interna

	//Inicia a classe
	public static void main(String args[]){
		new SAXParserHostStatus();

	}//finalMain

}//fim classe

