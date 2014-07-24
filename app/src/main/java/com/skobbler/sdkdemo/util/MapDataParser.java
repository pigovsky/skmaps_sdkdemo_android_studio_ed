package com.skobbler.sdkdemo.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.skobbler.sdkdemo.model.DownloadPackage;


/**
 * SAX parser used for parsing the XML file that stores information about the
 * map packages available for download on the server.
 * 
 */
public class MapDataParser {
    
    /**
     * SAX parser to parse the XML
     */
    private SAXParser saxParser;
    
    /**
     * URL to the XML file
     */
    private String url;
    
    /**
     * Map storing download packages - populated when tha XML file is parsed
     */
    private Map<String, DownloadPackage> packageMap = new HashMap<String, DownloadPackage>();
    
    public MapDataParser(String url) {
        this.url = url;
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, DownloadPackage> getPackageMap() {
        return packageMap;
    }
    
    public void parse() {
        HttpGet request = new HttpGet(url);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        InputStream responseStream;
        try {
            responseStream = httpClient.execute(request).getEntity().getContent();
            InputSource inputSource = new InputSource(responseStream);
            inputSource.setEncoding("UTF-8");
            MapsXMLParserHandler parsingHandler = new MapsXMLParserHandler();
            saxParser.parse(inputSource, parsingHandler);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    
    private class MapsXMLParserHandler extends DefaultHandler {
        
        private static final String TAG_PACKAGES = "packages";
        
        private static final String TAG_WORLD = "world";
        
        private static final String TAG_TYPE = "type";
        
        private static final String TAG_SIZE = "size";
        
        private static final String TAG_ENGLISH_NAME = "en";
        
        private Stack<String> tagStack = new Stack<String>();
        
        private DownloadPackage currentPackage;
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (tagStack.contains(TAG_PACKAGES) && tagStack.peek().equals(TAG_PACKAGES)) {
                currentPackage = new DownloadPackage();
                currentPackage.setCode(localName);
            }
            if (tagStack.contains(TAG_WORLD) && !tagStack.get(tagStack.size() - 1).equals(TAG_WORLD)) {
                String parentCode = tagStack.peek();
                packageMap.get(localName).setParentCode(parentCode);
                packageMap.get(parentCode).getChildrenCodes().add(localName);
            }
            tagStack.push(localName);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            tagStack.pop();
            if (tagStack.contains(TAG_PACKAGES) && tagStack.peek().equals(TAG_PACKAGES)) {
                packageMap.put(currentPackage.getCode(), currentPackage);
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String content = new String(ch, start, length);
            if (tagStack.peek().equals(TAG_ENGLISH_NAME)) {
                currentPackage.setName(content);
            } else if (tagStack.peek().equals(TAG_TYPE)) {
                currentPackage.setType(content);
            } else if (tagStack.peek().equals(TAG_SIZE)
                    && tagStack.get(tagStack.size() - 2).equals(currentPackage.getCode())) {
                currentPackage.setSize(Integer.parseInt(content));
            }
        }
    }
}
