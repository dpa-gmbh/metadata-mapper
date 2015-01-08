package de.dpa.metadata.imaging.common;

import com.google.common.io.Files;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO review/refactoring
 *
 * @author Ben Asmussen
 *
 */
public class XmlUtils
{

    /**
     * Creates an XPath object with a custom NamespaceContext given the Node to
     * operate on
     *
     *            the Node or document to operate on. Note that namespace
     *            handling will not work if a Node fragment is passed in
     * @return a new XPath object
     */
    public static XPath createXPath()
    {
        return XPathFactory.newInstance().newXPath();
    }

    public static String asXml(Node node) throws TransformerFactoryConfigurationError, javax.xml.transform.TransformerException
    {
        return asXml(node, false);
    }

    public static String asXml(Node node, boolean format) throws TransformerFactoryConfigurationError,
            javax.xml.transform.TransformerException
    {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();

        Transformer t = tf.newTransformer();

        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        if (format)
        {
            t.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        t.transform(new DOMSource(node), new StreamResult(sw));

        String result = sw.toString();

        result = result.replaceAll("xml:xml:lang", "xml:lang");
        result = result.replaceAll("xmlns:xml=\"\"", "");
        result = result.replaceAll("xmlns=\"\"", "");

        return result;
    }

    public static Document toDocument(Object object) throws Exception
    {
        return toDocument(object, false);
    }

    public static Document toDocument(Object object, boolean namespaceAware) throws Exception
    {
        if (object instanceof String)
        {
            // default we ignore namespaces
            return toDocument((String) object, namespaceAware);
        }
        else if (object instanceof Document)
        {
            return (Document) object;
        }
        else if (object instanceof File)
        {
            String raw = new String(Files.toByteArray((File) object));
            return toDocument(raw);
        }
        else
        {
            String objectClass = null;
            if (object != null)
            {
                objectClass = object.getClass().toString();
            }
            throw new Exception("Unsupport object. Class: " + objectClass);
        }
    }

    public static Document toDocument(String xml) throws Exception
    {
        // default we ignore namespaces
        return toDocument(xml, false);
    }

    public static Document toDocument(String xml, boolean namespaceAware) throws Exception
    {
        return toDocument(xml, namespaceAware, false);
    }

    public static Document toDocument(String xml, boolean namespaceAware, boolean ignoreDtd) throws Exception
    {
        Reader input = new StringReader(xml);
        InputSource is = new InputSource(input);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaceAware);

        // ignore dtd files
        if (ignoreDtd)
        {
            dbf.setValidating(false);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }

        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(new ErrorHandler()
        {
            public void warning(SAXParseException exception) throws SAXException
            {
                throw exception;

            }

            public void fatalError(SAXParseException exception) throws SAXException
            {
                throw exception;
            }

            public void error(SAXParseException exception) throws SAXException
            {
                throw exception;
            }
        });
        return db.parse(is);
    }

    public static List<Element> toList(NodeList nodeList)
    {

        List<Element> list = new LinkedList<>();

        for (int count = 0; count < nodeList.getLength(); count++)
        {
            Node item = nodeList.item(count);
            if (item.getNodeType() == Node.ELEMENT_NODE)
            {
                Element node = (Element) item;
                list.add(node);
            }
        }
        return list;
    }

    public static String selectValue( final String xpath, Node node ) throws XPathExpressionException
    {
        return selectValue( createXPath(),xpath,node);
    }

    public static String selectValue(XPath xp, String xpath, Node node) throws XPathExpressionException
    {
        return (String) xp.evaluate(xpath, node, XPathConstants.STRING);
    }

    public static List<String> selectValues(XPath xp, String xpath, Node node)
    {
        List<String> result = new LinkedList<>();
        try
        {
            List<Node> list = select(xp, xpath, node);
            for (Node n : list)
            {
                result.add(n.getTextContent());
            }
        }
        catch (XPathExpressionException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static List<Node> select(XPath xp, String xpath, Node node) throws XPathExpressionException
    {
        NodeList nl = (NodeList) xp.evaluate(xpath, node, XPathConstants.NODESET);
        List<Node> nodeList = new LinkedList<>();
        for (int i = 0; i < nl.getLength(); i++)
        {
            nodeList.add(nl.item(i));
        }
        return nodeList;
    }

    public static List<Node> select(String xpath, Node node) throws XPathExpressionException
    {
        XPath xp = createXPath();
        NodeList nl = (NodeList) xp.evaluate(xpath, node, XPathConstants.NODESET);
        List<Node> nodeList = new LinkedList<>();
        for (int i = 0; i < nl.getLength(); i++)
        {
            nodeList.add(nl.item(i));
        }
        return nodeList;
    }

    public static List<String> selectValues(String xpath, Node node)
    {
        List<String> result = new LinkedList<>();
        try
        {
            List<Node> list = select(xpath, node);
            for (Node n : list)
            {
                result.add(n.getTextContent());
            }
        }
        catch (XPathExpressionException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}