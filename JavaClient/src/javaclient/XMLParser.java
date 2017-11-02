package javaclient;

import java.util.ArrayList;
import javaclient.Class.Command;
import javax.xml.parsers.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {

    private String cmdFileName;
    private ArrayList<Command> cmdList;
    private Command cmdTemp;
    private String tmpValue;

    public XMLParser() {
        cmdFileName = "Commands.xml";
        cmdList = new ArrayList<>();
        parseDocument();
    }

    private void parseDocument() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(cmdFileName, this);
        } catch (Exception ex) {
            System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("command")) {
            cmdTemp = new Command();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("command")) {
            cmdList.add(cmdTemp);
        } else if (qName.equalsIgnoreCase("name")) {
            cmdTemp.setName(tmpValue);
        } else if (qName.equalsIgnoreCase("cmdbyte")) {
            cmdTemp.setCmdByte(tmpValue);
        } else if (qName.equalsIgnoreCase("payload")) {
            cmdTemp.setPayload(tmpValue);
        } else if (qName.equalsIgnoreCase("reserve")) {
            cmdTemp.setReserve(tmpValue);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tmpValue = new String(ch, start, length);
    }

    public ArrayList<Command> getCmdList() {
        return cmdList;
    }

    //for testing purpose
    public static void main(String[] args) {
        ArrayList<Command> cmdList = new XMLParser().getCmdList();
        for (int i = 0; i < cmdList.size(); i++) {
            System.out.println(cmdList.get(i).getName());
            System.out.println(cmdList.get(i).getCmdByte());
            System.out.println(cmdList.get(i).getPayload());
            System.out.println(cmdList.get(i).getReserve());
        }

    }

}
