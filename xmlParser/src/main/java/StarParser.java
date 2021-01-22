package main.java;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class StarParser {
    public ArrayList<Star> sl;
    Document dom;

    StarParser(){
        sl = new ArrayList<Star>();
    }

    public void run(){
        parseXmlFile();
        parseDocument();
    }

    private void parseXmlFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse("actors63.xml");
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
        NodeList actors = docEle.getElementsByTagName("actor");
        int star_num = actors.getLength(),
            count = 0;
        for(int i=0; i<star_num; i++){
            Element actor = (Element) actors.item(i);
            Star s = get_star(actor);

            if(s.is_valid())
                sl.add(s);
            else{
                System.err.println("Inconsistent Star: " + s.toString());
                count++;
            }
        }
        System.err.println("Inconsistent records in actors.xml: " + count);
    }
    private Star get_star(Element actor){
        Node name, birth;
        int star_birth;
        NodeList n = actor.getElementsByTagName("stagename");
        NodeList b = actor.getElementsByTagName("dob");
        name = n.item(0);
        birth = b.item(0);
        String star_name = name.getFirstChild().getNodeValue();
        try{
            star_birth = Integer.parseInt(birth.getFirstChild().getNodeValue());
            return new Star(star_name, star_birth);
        }
        catch (NumberFormatException e){
            return new Star(star_name, -1);
        }
        catch (NullPointerException e)
        {
            return new Star(star_name, -1);
        }

    }

    public static void main(String[] args) {
        StarParser p = new StarParser();
        p.run();
    }
}
