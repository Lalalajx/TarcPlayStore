package com.example.rongfu.tarucplaystore.Configuration;

import android.content.Context;

import com.example.rongfu.tarucplaystore.Class.Command;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class XMLParser extends DefaultHandler {

    private String cmdFileName;
    private ArrayList<Command> cmdList;
    private Command cmdTemp;
    private String tmpValue;

    public XMLParser(Context context) {
        cmdList = new ArrayList<>();
        parseDocument(context);
    }

    private void parseDocument(Context context) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(context.getAssets().open("Commands.xml"), this);
        } catch (Exception ex) {
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

}

