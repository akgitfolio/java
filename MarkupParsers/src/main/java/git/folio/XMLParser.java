package git.folio;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class XMLParser {
    private Document doc;

    public XMLParser(String filePath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.doc = builder.parse(new File(filePath));
    }

    public Object parse() {
        Element root = doc.getDocumentElement();
        return parseElement(root);
    }

    private Object parseElement(Element element) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Parse attributes
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            result.put("@" + attr.getNodeName(), attr.getNodeValue());
        }

        // Parse child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String childName = child.getNodeName();
                Object childValue = parseElement((Element) child);

                if (result.containsKey(childName)) {
                    // Handle multiple elements with the same name
                    Object existing = result.get(childName);
                    if (existing instanceof List) {
                        ((List<Object>) existing).add(childValue);
                    } else {
                        List<Object> list = new ArrayList<>();
                        list.add(existing);
                        list.add(childValue);
                        result.put(childName, list);
                    }
                } else {
                    result.put(childName, childValue);
                }
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty()) {
                    result.put("#text", text);
                }
            }
        }

        // If the element has only text content, return it directly
        if (result.size() == 1 && result.containsKey("#text")) {
            return result.get("#text");
        }

        return result;
    }

    public static void main(String[] args) {
        try {
            XMLParser parser = new XMLParser("sample.xml");
            Object result = parser.parse();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}