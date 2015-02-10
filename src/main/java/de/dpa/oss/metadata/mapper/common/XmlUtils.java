package de.dpa.oss.metadata.mapper.common;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO review/refactoring
 *
 * @author Ben Asmussen
 */
public class XmlUtils
{

    /**
     * Creates an XPath object with a custom NamespaceContext given the Node to
     * operate on
     * <p/>
     * the Node or document to operate on. Note that namespace
     * handling will not work if a Node fragment is passed in
     *
     * @return a new XPath object
     */
    public static XPath createXPath()
    {
        return XPathFactory.newInstance().newXPath();
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

    public static String selectValue(final String xpath, Node node) throws YAXPathExpressionException
    {
        XPathExpression xPathExpression = compileXPathExpression(xpath);

        try
        {
            return (String) xPathExpression.evaluate(node, XPathConstants.STRING);
        }
        catch (XPathExpressionException e)
        {
            throw new YAXPathExpressionException(xpath, "Error while evaluating xpath expression: " + xpath, e);
        }
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

    public static List<Node> select(XPathExpression compiledXPath, Node node) throws XPathExpressionException
    {
        NodeList nl;
        nl = (NodeList) compiledXPath.evaluate(node, XPathConstants.NODESET);

        List<Node> nodeList = new LinkedList<>();
        for (int i = 0; i < nl.getLength(); i++)
        {
            nodeList.add(nl.item(i));
        }
        return nodeList;
    }

    public static List<Node> select(String xpath, Node node) throws YAXPathExpressionException
    {
        try
        {
            return select(compileXPathExpression(xpath), node);
        }
        catch (XPathExpressionException e)
        {
            throw new YAXPathExpressionException(xpath, "Error while evaluating XPATH expression: " + xpath, e);
        }

    }

    public static List<String> selectValues(String xpath, Node node) throws YAXPathExpressionException
    {
        List<String> result = new LinkedList<>();
        List<Node> list = select(xpath, node);
        for (Node n : list)
        {
            result.add(n.getTextContent());
        }
        return result;
    }

    private static XPathExpression compileXPathExpression(final String xpath) throws YAXPathExpressionException
    {
        XPath xPath = createXPath();
        XPathExpression compiledXPath;
        try
        {
            compiledXPath = xPath.compile(xpath);
        }
        catch (XPathExpressionException t)
        {
            throw new YAXPathExpressionException(xpath, "Error parsing XPATH expression", t);
        }
        return compiledXPath;
    }
}