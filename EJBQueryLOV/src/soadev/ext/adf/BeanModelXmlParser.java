package soadev.ext.adf;

import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import soadev.ext.adf.query.AttributeDef;

public class BeanModelXmlParser {
    private static final String ATTRIBUTE = "Attribute";
    private static final String IS_QUERIABLE = "IsQueriable";
    private static final String PROPERTIES = "Properties";
    private static final String SCHEMA_BASED_PROPERTIES =
        "SchemaBasedProperties";
    private static final String CUSTOM_PROPERTIES = "CustomProperties";
    private static final String IS_NOT_NULL = "IsNotNull";
    private static final String IS_UPDATEABLE = "IsUpdateable";
    //    private static final String IS_PERSISTENT = "IsPersistent";
    private static final String NAME_ATTRIBUTE = "Name";

    private static final String TYPE = "Type";
    private static final String CONTROL_TYPE = "CONTROLTYPE";
    private static final String VALUE_ATTRIBUTE = "Value";
    private static final String IS_UNIQUE = "IsUnique";
    private static final String RES_ID = "ResId";
    private static final String LABEL = "LABEL";
    private static final String PROPERTIES_FILE_ATTRIBUTE = "PropertiesFile";
    private static final String PROPERTIES_BUNDLE = "PropertiesBundle";
    private static final String RESOURCE_BUNDLE = "ResourceBundle";

    public static List<AttributeDef> getAttributeDefs(String fullFileName) {
        List<AttributeDef> attributeDefs = new ArrayList<AttributeDef>();
        try {
//            File file = getFile(fullFileName);
            Document doc = getDocument(fullFileName);
            String propertiesBundle = getPropertiesBundle(doc.getDocumentElement()); 
            NodeList nodeList = doc.getElementsByTagName(ATTRIBUTE);

            for (int i = 0; i < nodeList.getLength(); i++) {
                AttributeDef attributeDef = new AttributeDef();
                Node node = nodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element)node;
                attributeDef.setName(element.getAttribute(NAME_ATTRIBUTE));
                attributeDef.setPropertiesBundle(propertiesBundle);
                attributeDef.setType(getClass(element.getAttribute(TYPE)));
                attributeDef.setQueriable(getBoolean(element.getAttribute(IS_QUERIABLE),
                                                     true));
                attributeDef.setUpdateable(getBooleanUpdatableAttribute(element));
                attributeDef.setMandatory(getBoolean(element.getAttribute(IS_NOT_NULL),
                                                     false));
                setSchemaBasedProperties(attributeDef, element);
                attributeDef.setCustomProperties(getCustomPropertiesMap(element));
                attributeDef.setDescription(attributeDef.getLabel());
                attributeDef.setIndexed(getBoolean(element.getAttribute(IS_UNIQUE),
                                                   false));
                attributeDefs.add(attributeDef);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return attributeDefs;
    }

    private static String getPropertiesBundle(Element element) {
        Element resourceBundleElement =
            getChildElement(element, RESOURCE_BUNDLE);
        if (resourceBundleElement == null) {
            return null;
        }
        Element propertiesBundle =
            getChildElement(resourceBundleElement, PROPERTIES_BUNDLE);
        if (propertiesBundle == null) {
            return null;
        }
        return propertiesBundle.getAttribute(PROPERTIES_FILE_ATTRIBUTE);
    }

    private static void setSchemaBasedProperties(AttributeDef attributeDef,
                                                 Element element) {
        //set defaults
        attributeDef.setComponentType(AttributeDef.EDIT); //apply appropriate component later
        attributeDef.setLabel(element.getAttribute(NAME_ATTRIBUTE));
        Element properties = getChildElement(element, PROPERTIES);
        if (properties == null) {
            return;
        }
        Element schemaBasedProperties =
            getChildElement(properties, SCHEMA_BASED_PROPERTIES);
        if (schemaBasedProperties == null) {
            return;
        }
        Map<String, Element> propertiesMap =
            getChildElementsMap(schemaBasedProperties);
        Element controlTypeElement = propertiesMap.get(CONTROL_TYPE);
        if (controlTypeElement != null) {
            attributeDef.setComponentType(controlTypeElement.getAttribute(VALUE_ATTRIBUTE));
        }
        Element labelElement = propertiesMap.get(LABEL);
        if (labelElement != null) {
            attributeDef.setLabel(labelElement.getAttribute(RES_ID));
        }
        Element formatElement = propertiesMap.get("FMT_FORMAT");
        if(formatElement != null){
            attributeDef.setFormat(formatElement.getAttribute(RES_ID));
        }
        
    }

    private static Map<String, String> getCustomPropertiesMap(Element element) {
        Element properties = getChildElement(element, PROPERTIES);
        if (properties == null) {
            return Collections.emptyMap();
        }
        Element customProperties =
            getChildElement(properties, CUSTOM_PROPERTIES);
        if (customProperties == null) {
            return Collections.emptyMap();
        }
        NodeList nodeList = customProperties.getChildNodes();
        Map<String, String> customPropertiesMap =
            new LinkedHashMap<String, String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element property = (Element)node;
            customPropertiesMap.put(property.getAttribute(NAME_ATTRIBUTE),
                                    property.getAttribute(VALUE_ATTRIBUTE));
        }
        return customPropertiesMap;
    }

    public static Map<String, AttributeDef> getAttributeDefsMap(String fullFileName) {
        List<AttributeDef> attributeDefs = getAttributeDefs(fullFileName);
        Map<String, AttributeDef> attributeDefsMap =
            new LinkedHashMap<String, AttributeDef>();
        for (AttributeDef attrDef : attributeDefs) {
            attributeDefsMap.put(attrDef.getName(), attrDef);
        }
        return attributeDefsMap;
    }

    private static Document getDocument(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }
    
  private static Document getDocument(String fullFileName) throws Exception {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = getClassLoader().getResourceAsStream(fullFileName);
      Document doc = db.parse(new InputSource(is));
      doc.getDocumentElement().normalize();
      return doc;
  }

    private static boolean getBooleanUpdatableAttribute(Element element) {
        String value = element.getAttribute(IS_UPDATEABLE);
        return ("".equals(value) || "true".equals(value) ||
                "1".equals(value) || "2".equals(value));
    }


    private static File getFile(String fullFileName) {
        return new File(getClassLoader().getResource(fullFileName).getFile());
    }
    
    private static ClassLoader getClassLoader(){
      return Thread.currentThread().getContextClassLoader();
    }

    private static Element getChildElement(Element element, String name) {
        NodeList childNodes = element.getChildNodes();
        if (childNodes == null) {
            return null;
        }
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node node = childNodes.item(j);
            if ((node.getNodeType() == Node.ELEMENT_NODE) &&
                (node.getNodeName().equals(name))) {
                return (Element)node;
            }
        }
        return null;
    }

    private static Map<String, Element> getChildElementsMap(Element element) {
        NodeList childNodes = element.getChildNodes();
        if (childNodes == null) {
            return Collections.emptyMap();
        }
        Map<String, Element> childElementsMap = new HashMap<String, Element>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                childElementsMap.put(e.getNodeName(), e);
            }
        }
        return childElementsMap;
    }


    private static boolean getBoolean(String value, boolean defaultValue) {
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return ("true".equalsIgnoreCase(value));
    }

    private static Class getClass(String className) {
        try {
            if("boolean".equals(className)){
                return Boolean.class;
            }
            if("int".equals(className)){
                return Integer.class;
            }
            if("double".equals(className)){
                return Double.class;
            }
            if("long".equals(className)){
                return Long.class;
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Object.class;
        }
    }


}
