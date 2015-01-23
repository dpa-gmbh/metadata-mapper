package de.dpa.oss.metadata.mapper.imaging.configuration.generated;

import java.math.BigInteger;

/**
 * @author oliver langer
 */
public class QualifiedXPathFixture
{
    protected String part;
    protected String value;
    protected BigInteger rank;
    protected XPathReturnType returnType;

    private QualifiedXPathFixture()
    {
    }

    public static QualifiedXPathFixture aQualifiedXPath()
    {
        return new QualifiedXPathFixture();
    }

    public QualifiedXPathFixture withPart(String part)
    {
        this.part = part;
        return this;
    }

    public QualifiedXPathFixture withValue(String value)
    {
        this.value = value;
        return this;
    }

    public QualifiedXPathFixture withRank(BigInteger rank)
    {
        this.rank = rank;
        return this;
    }

    public QualifiedXPathFixture withReturnType(XPathReturnType returnType)
    {
        this.returnType = returnType;
        return this;
    }

    public QualifiedXPathFixture but()
    {
        return aQualifiedXPath().withPart(part).withValue(value).withRank(rank).withReturnType(returnType);
    }

    public QualifiedXPath build()
    {
        QualifiedXPath qualifiedXPath = new QualifiedXPath();
        qualifiedXPath.setPart(part);
        qualifiedXPath.setValue(value);
        qualifiedXPath.setRank(rank);
        qualifiedXPath.setReturnType(returnType);
        return qualifiedXPath;
    }
}
