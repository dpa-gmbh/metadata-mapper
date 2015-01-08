package de.dpa.metadata.common;

import javax.xml.xpath.XPathException;

/**
 * An XPathException which tells something about the XPath....
 * @author oliver langer
 */
public class ExtXPathException extends XPathException
{
    private String xpathExpression;

    public ExtXPathException(final String xpathExpression, final Throwable cause)
    {
        super(cause);
        this.xpathExpression = xpathExpression;
    }

    public String getXpathExpression()
    {
        return xpathExpression;
    }
}
