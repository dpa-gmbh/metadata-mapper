package de.dpa.oss.metadata.mapper.common;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

/**
 * An XPathException which tells something about the XPath....
 * @author oliver langer
 */
public class YAXPathExpressionException extends XPathExpressionException
{
    private String xpathExpression;

    public YAXPathExpressionException(final String xpathExpression, final String message, final Throwable cause)
    {
        super("\"" + message+ "\":"+cause.getLocalizedMessage());
        this.xpathExpression = xpathExpression;
    }

    public String getXpathExpression()
    {
        return xpathExpression;
    }
}
