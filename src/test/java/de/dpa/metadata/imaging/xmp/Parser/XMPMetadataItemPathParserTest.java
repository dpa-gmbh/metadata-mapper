package de.dpa.metadata.imaging.xmp.Parser;

import de.dpa.metadata.imaging.xmp.parser.XMPMetadataArrayItemPath;
import de.dpa.metadata.imaging.xmp.parser.XMPMetadataItemPath;
import de.dpa.metadata.imaging.xmp.parser.XMPMetadataItemPathParser;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class XMPMetadataItemPathParserTest
{
    @Test
    public void shouldReturnSimplePathItem() {
        // given
        final String testPath = "simpleName";

        // when
        List<XMPMetadataItemPath> xmpPathItems = new XMPMetadataItemPathParser().parsePaths(testPath);

        // then
        assertThat( xmpPathItems, hasSize(1));
        XMPMetadataItemPath item = xmpPathItems.get(0);
        assertThat(item.getPath(), is(testPath));
        assertThat(item.getPrefix(), is(nullValue()));
    }

    @Test
    public void shouldReturnPrefix()
    {
        // given
        final String testPath = "prefix:simpleName";

        // when
        List<XMPMetadataItemPath> xmpPathItems = new XMPMetadataItemPathParser().parsePaths(testPath);

        // then
        assertThat( xmpPathItems, hasSize(1));
        XMPMetadataItemPath item = xmpPathItems.get(0);
        assertThat(item.getPath(), is("simpleName"));
        assertThat(item.getPrefix(), is("prefix"));
    }

    @Test
    public void shouldReturnArrayAndElementPathItem()
    {
        // given
        final String testPath = "prefix:arrayElement[1]";

        // when
        List<XMPMetadataItemPath> xmpPathItems = new XMPMetadataItemPathParser().parsePaths(testPath);

        // then
        assertThat( xmpPathItems, hasSize(2));
        XMPMetadataItemPath pathItem = xmpPathItems.get(0);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));

        pathItem = xmpPathItems.get(1);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));

        assertThat( pathItem, is(instanceOf(XMPMetadataArrayItemPath.class)));
        XMPMetadataArrayItemPath arrayItem = (XMPMetadataArrayItemPath) pathItem;
        assertThat( arrayItem.getArrayIndex(), is(1));
    }

    @Test
    public void shouldReturnArrayElementAndSubelement()
    {
        // given
        final String testPath = "prefix:arrayElement[1]/subelement";

        // when
        List<XMPMetadataItemPath> xmpPathItems = new XMPMetadataItemPathParser().parsePaths(testPath);

        // then
        assertThat(xmpPathItems, hasSize(3));
        XMPMetadataItemPath pathItem = xmpPathItems.get(0);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));

        pathItem = xmpPathItems.get(1);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));

        assertThat( pathItem, is(instanceOf(XMPMetadataArrayItemPath.class)));
        XMPMetadataArrayItemPath arrayItem = (XMPMetadataArrayItemPath) pathItem;
        assertThat( arrayItem.getArrayIndex(), is(1));

        pathItem = xmpPathItems.get(2);
        assertThat(pathItem.getPath(), is("subelement"));
        assertThat(pathItem.getPrefix(), is(Matchers.nullValue()));
    }

    @Test
    public void shouldReturnArrayOfArrayOfElement() {
        // given
        final String testPath = "prefix:arrayElement[1]/arrPrefix:subArray[2]/subSubPrefix:subSubElement";

        // when
        List<XMPMetadataItemPath> xmpPathItems = new XMPMetadataItemPathParser().parsePaths(testPath);

        // then
        assertThat(xmpPathItems, hasSize(5));
        XMPMetadataItemPath pathItem = xmpPathItems.get(0);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));
        assertThat(pathItem, not(instanceOf(XMPMetadataArrayItemPath.class)));

        pathItem = xmpPathItems.get(1);
        assertThat(pathItem.getPath(), is("arrayElement"));
        assertThat(pathItem.getPrefix(), is("prefix"));
        assertThat(pathItem, is(instanceOf(XMPMetadataArrayItemPath.class)));
        XMPMetadataArrayItemPath arrayItem = (XMPMetadataArrayItemPath) pathItem;
        assertThat( arrayItem.getArrayIndex(), is(1));

        pathItem = xmpPathItems.get(2);
        assertThat(pathItem.getPath(), is("subArray"));
        assertThat(pathItem.getPrefix(), is("arrPrefix"));
        assertThat( pathItem, not(instanceOf(XMPMetadataArrayItemPath.class)));

        pathItem = xmpPathItems.get(3);
        assertThat( pathItem.getPath(), is( "subArray" ));
        assertThat( pathItem.getPrefix(), is( "arrPrefix" ));
        assertThat(pathItem,is(Matchers.instanceOf(XMPMetadataArrayItemPath.class)));
        arrayItem = (XMPMetadataArrayItemPath) pathItem;
        assertThat(arrayItem.getArrayIndex(), is(2));

        pathItem = xmpPathItems.get(4);
        assertThat( pathItem.getPath(), is( "subSubElement" ));
        assertThat( pathItem.getPrefix(), is( "subSubPrefix" ));
    }
}