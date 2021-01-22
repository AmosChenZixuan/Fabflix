package main.java;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class RelationParser {
    public ArrayList<Relation> relations;
    private Document dom;

    public RelationParser(){
        relations = new ArrayList<Relation>();
    }

    public void run(){
        parseXmlFile();
        parseDocument();
    }
    private void parseXmlFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse("casts124.xml");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private void parseDocument() {
        Element docEle = dom.getDocumentElement();
        NodeList films = docEle.getElementsByTagName("filmc");
        int count = 0;
        int film_num = films.getLength();
//        System.out.println(film_num);
        for(int i=0; i<film_num; ++i){
            Element film = (Element)films.item(i);// one film with multiple records
            NodeList records = film.getElementsByTagName("m");
            int num_records = records.getLength();

            for(int j=0; j <num_records; j++){
                Element rec = (Element) records.item(j);
                //System.out.println(i +"    "+ j + " count" + num_records );
                Relation r = get_relation(rec);
                //System.out.println(r.toString());
                if(!r.is_valid()){
                    System.err.println("Inconsistent Relation: " + r.toString());
                    count++;
                }
                else
                relations.add(r);
            }
        }
        System.err.println("Inconsistent records in cast.xml: " + count);
    }
    private Relation get_relation(Element el){
        String id = get_movie_id(el);
        String title = get_title(el);
        String starname = get_star_name(el);
        return new Relation(id, title, starname);
    }

    private String get_movie_id(Element el){
        NodeList n = el.getElementsByTagName("f");
        Element id = (Element) n.item(0);
        //System.out.println(id.getFirstChild().getNodeValue());
        return id.getFirstChild().getNodeValue();
    }
    private String get_title(Element el){
        NodeList title = el.getElementsByTagName("t");
        Element t = (Element) title.item(0);
        return t.getFirstChild().getNodeValue();
    }
    private String get_star_name(Element el){
        NodeList n = el.getElementsByTagName("a");
        Element name = (Element) n.item(0);
        try{
            return name.getFirstChild().getNodeValue();
        }
        catch (NullPointerException e){
            return null;
        }
    }
    public static void main(String[] args) {
        RelationParser p = new RelationParser();
        p.run();

    }

}
