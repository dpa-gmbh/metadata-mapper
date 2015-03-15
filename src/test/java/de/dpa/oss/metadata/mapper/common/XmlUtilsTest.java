package de.dpa.oss.metadata.mapper.common;

import com.google.common.io.CharStreams;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XmlUtilsTest
{
    final static String xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"

            + "<testDocument>\n"
            + "\t<simpleTag/>\n"
            + "\t<itemWithTextContent>textcontent</itemWithTextContent>\n"
            + "\t<compare>\n"
            + "\t\t<item1>1</item1>\n"
            + "\t\t<item2>2</item2>\n"
            + "\t</compare>\n"
            + "</testDocument>";

    @Test
    public void shouldSelectSingleValue() throws ParserConfigurationException, IOException, SAXException, YAXPathExpressionException
    {
        // given
        ByteArrayInputStream src = new ByteArrayInputStream(xmlDocument.getBytes());
        Document document = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(src);

        // when
        String selectedValue = XmlUtils.selectValue("//itemWithTextContent/text()", document.getDocumentElement());

        // then
        assertThat(selectedValue, is( "textcontent"));
    }
}