package de.dpa.esb.imaging.xmp.Parser;

import de.dpa.esb.imaging.xmp.metadata.*;
import de.dpa.esb.imaging.xmp.parser.XMPMetadataFactoryContextBuilder;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class XMPMetadataFactoryContextBuilderTest
{
    @Test
    public void shouldAddSingleItem()
    {
        // given
        XMPString item = new XMPString("http://ns.adobe.com/photoshop/1.0/", "LegacyIPTCDigest", "1267126");
        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addSingleMetadata("photoshop:LegacyIPTCDigest", item);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, equalTo((XMPMetadata) item));
    }

    @Test
    public void shouldAddMultipleItems()
    {
        // given
        XMPString item1 = new XMPString("http://ns.adobe.com/photoshop/1.0/", "LegacyIPTCDigest", "1267126");
        XMPString item2 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "0");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addSingleMetadata("photoshop:LegacyIPTCDigest", item1);
        xmpMetadataFactoryContext.addSingleMetadata("crs:Tint", item2);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(2));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, equalTo((XMPMetadata) item1));

        xmpMetadata = metadataList.get(1);
        assertThat(xmpMetadata, equalTo((XMPMetadata) item2));
    }

    @Test
    public void shouldAddEmptyArray()
    {
        // given
        XMPSequence item1 = new XMPSequence("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve"
        );

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("crs:ToneCurve", item1);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, equalTo((XMPMetadata) item1));
    }

    @Test
    public void shouldAddEmptyStruct()
    {
        // given
        XMPStruct item1 = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History", item1);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, equalTo((XMPMetadata) item1));
    }

    @Test
    public void shouldAddArrayWithStringElements()
    {
        // given
        XMPSequence item1 = new XMPSequence("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve"
        );
        XMPString item2 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "32,32");
        XMPString item3 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "128,128");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("crs:ToneCurve", item1);
        xmpMetadataFactoryContext.addSingleMetadata("crs:ToneCurve[2]", item2);
        xmpMetadataFactoryContext.addSingleMetadata("crs:ToneCurve[4]", item3);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();
        assertThat(metadataList, hasSize(1));
        XMPMetadata array = metadataList.get(0);
        assertThat(array, instanceOf(XMPCollection.class));

        List<XMPMetadata> arrayElements = ((XMPCollection) array).getMetadata();
        assertThat(arrayElements, hasSize(2));
        assertThat(arrayElements.get(0), equalTo((XMPMetadata) item2));
        assertThat(arrayElements.get(1), equalTo((XMPMetadata) item3));
    }

    @Test
    public void shouldAddElementsToStruct()
    {
        // given
        XMPStruct item1 = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");
        XMPString item2 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changed", "/");
        XMPString item3 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "softwareAgent",
                "Adobe Photoshop Camera Raw 6.7 (Windows)");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History", item1);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History/stEvt:changed", item2);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History/stEvt:softwareAgent", item3);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, instanceOf(XMPStruct.class));

        List<XMPMetadata> structElements = ((XMPStruct) xmpMetadata).getMetadata();
        assertThat(structElements, hasSize(2));
        assertThat(structElements.get(0), equalTo((XMPMetadata) item2));
        assertThat(structElements.get(1), equalTo((XMPMetadata) item3));
    }

    @Test
    public void shouldAddStructToArray()
    {
        // given
        XMPSequence arrayMetadata = new XMPSequence("http://ns.adobe.com/xap/1.0/mm/", "History"
        );

        XMPStruct structMetadata = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");
        XMPString structItemMetadata1 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changed", "/");
        XMPString structItemMetadata2 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "softwareAgent",
                "Adobe Photoshop Camera Raw 6.7 (Windows)");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("xmpMM:History", arrayMetadata);
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History[0]", structMetadata);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:changed", structItemMetadata1);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:softwareAgent", structItemMetadata2);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, instanceOf(XMPCollection.class));

        List<XMPMetadata> arrayElements = ((XMPCollection) xmpMetadata).getMetadata();
        assertThat(arrayElements, hasSize(1));
        xmpMetadata = arrayElements.get(0);
        assertThat(xmpMetadata, instanceOf(XMPStruct.class));
        List<XMPMetadata> structElements = ((XMPStruct) xmpMetadata).getMetadata();
        assertThat(structElements, hasSize(2));
        assertThat(structElements.get(0), equalTo((XMPMetadata) structItemMetadata1));
        assertThat(structElements.get(1), equalTo((XMPMetadata) structItemMetadata2));
    }

    @Test
    public void shouldAddTwoStrutsToArray()
    {
        // given
        XMPSequence arrayMetadata = new XMPSequence("http://ns.adobe.com/xap/1.0/mm/", "History"
        );

        XMPStruct structMetadataA = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");
        XMPString structItemMetadataA1 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changedA", "/");
        XMPString structItemMetadataA2 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "softwareAgentA",
                "Adobe Photoshop Camera Raw 6.7 (Windows)");

        XMPStruct structMetadataB = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");
        XMPString structItemMetadataB1 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changedB", "/");
        XMPString structItemMetadataB2 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "softwareAgentB",
                "Adobe Photoshop Camera Raw 12 (Windows)");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("xmpMM:History", arrayMetadata);

        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History[0]", structMetadataA);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:changedA", structItemMetadataA1);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:softwareAgentA", structItemMetadataA2);

        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History[1]", structMetadataB);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[1]/stEvt:changedB", structItemMetadataB1);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[1]/stEvt:softwareAgentB", structItemMetadataB2);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, instanceOf(XMPCollection.class));

        List<XMPMetadata> arrayElements = ((XMPCollection) xmpMetadata).getMetadata();
        assertThat(arrayElements, hasSize(2));

        xmpMetadata = arrayElements.get(0);
        assertThat(xmpMetadata, instanceOf(XMPStruct.class));
        List<XMPMetadata> structElements = ((XMPStruct) xmpMetadata).getMetadata();
        assertThat(structElements, hasSize(2));
        assertThat(structElements.get(0), equalTo((XMPMetadata) structItemMetadataA1));
        assertThat(structElements.get(1), equalTo((XMPMetadata) structItemMetadataA2));

        xmpMetadata = arrayElements.get(1);
        assertThat(xmpMetadata, instanceOf(XMPStruct.class));
        structElements = ((XMPStruct) xmpMetadata).getMetadata();
        assertThat(structElements, hasSize(2));
        assertThat(structElements.get(0), equalTo((XMPMetadata) structItemMetadataB1));
        assertThat(structElements.get(1), equalTo((XMPMetadata) structItemMetadataB2));
    }

    @Test
    public void shouldAddStructAndStringToArray()
    {
        // given
        XMPSequence arrayMetadata = new XMPSequence("http://ns.adobe.com/xap/1.0/mm/", "History"
        );

        XMPStruct structMetadata = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "History");
        XMPString structItemMetadata1 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changed", "/");
        XMPString structItemMetadata2 = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "softwareAgent",
                "Adobe Photoshop Camera Raw 6.7 (Windows)");

        XMPString stringMetadata = new XMPString("http://ns.adobe.com/exif/1.0/aux", "ImageNumber", "0");
        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("xmpMM:History", arrayMetadata);
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:History[0]", structMetadata);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:changed", structItemMetadata1);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:History[0]/stEvt:softwareAgent", structItemMetadata2);
        xmpMetadataFactoryContext.addSingleMetadata("aux:ImageNumber", stringMetadata);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(2));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, instanceOf(XMPCollection.class));

        xmpMetadata = metadataList.get(1);
        assertThat(xmpMetadata, instanceOf(XMPString.class));
        assertThat(xmpMetadata, equalTo((XMPMetadata) stringMetadata));
    }

    @Test
    public void shouldAddStructWithArrayWithElement()
    {
        // given
        XMPStruct structMetadata = new XMPStruct("http://structWithArray", "TestStruct");

        XMPSequence arrayMetadata = new XMPSequence("http://sampleArray/1.0/", "TestArray"
        );

        XMPString arrayElementMetadata = new XMPString("http://arrayElement/1.0/", "arrayELement", "1234");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addStructMetadata("test:TestStruct", structMetadata);
        xmpMetadataFactoryContext.addCollectionMetadata("test:TestStruct/crs:TestArray", arrayMetadata);
        xmpMetadataFactoryContext.addSingleMetadata("test:TestStruct/crs:TestArray[0]/ae:arrayELement", arrayElementMetadata);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(1));
        XMPMetadata xmpMetadata = metadataList.get(0);
        assertThat(xmpMetadata, equalTo((XMPMetadata) structMetadata));

        assertThat(((XMPStruct) xmpMetadata).getMetadata(), hasSize(1));
        assertThat(((XMPStruct) xmpMetadata).getMetadata().get(0), instanceOf(XMPCollection.class));

        XMPCollection foundArray = (XMPCollection) (((XMPStruct) xmpMetadata).getMetadata().get(0));
        assertThat(foundArray.getMetadata(), hasSize(1));
        assertThat(foundArray.getMetadata().get(0), equalTo((XMPMetadata) arrayElementMetadata));
    }

    @Test
    public void shouldAddArrayWithArrayWithElement()
    {
        // given
        XMPSequence arrayElement1 = new XMPSequence("http://testaray", "array_1"
        );
        XMPSequence arrayElement2 = new XMPSequence("http://testaray", "array_2"
        );

        XMPString arrayItem1 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "32,32");
        XMPString arrayItem2 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "128,128");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1", arrayElement1);
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1[0]", arrayElement2);
        xmpMetadataFactoryContext.addSingleMetadata("ta:array_1[0]/crs:ToneCurve[0]", arrayItem1);
        xmpMetadataFactoryContext.addSingleMetadata("ta:array_1[0]/crs:ToneCurve[1]", arrayItem2);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();
        assertThat(metadataList, hasSize(1));
        XMPMetadata array = metadataList.get(0);
        assertThat(array, instanceOf(XMPCollection.class));

        assertThat(((XMPCollection) array).getMetadata(), hasSize(1));
        assertThat(((XMPCollection) array).getMetadata().get(0), instanceOf(XMPCollection.class));

        XMPCollection foundInnerArray = (XMPCollection) ((XMPCollection) array).getMetadata().get(0);
        assertThat(foundInnerArray.getMetadata(), hasSize(2));

        assertThat(foundInnerArray.getMetadata().get(0), equalTo((XMPMetadata) arrayItem1));
        assertThat(foundInnerArray.getMetadata().get(1), equalTo((XMPMetadata) arrayItem2));
    }

    @Test
    public void shouldAddArrayWithElementAndSingleRootStringElement()
    {
        // given
        XMPSequence arrayElement1 = new XMPSequence("http://testaray", "array_1"
        );
        XMPSequence arrayElement2 = new XMPSequence("http://testaray", "array_2"
        );

        XMPString singleRootItem = new XMPString("http://testItem/1.0/", "RootItem", "Ping");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1", arrayElement1);
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1[0]", arrayElement2);
        xmpMetadataFactoryContext.addSingleMetadata("ti:RootItem", singleRootItem);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();
        assertThat(metadataList, hasSize(2));
        assertThat(metadataList.get(0), instanceOf(XMPCollection.class));
        assertThat(metadataList.get(1), equalTo((XMPMetadata) singleRootItem));
    }

    @Test
    public void shouldAddArrayWithArrayAndWithSingleElement()
    {
        // given
        XMPSequence arrayElement1 = new XMPSequence("http://testaray", "array_1"
        );
        XMPSequence arrayElement2 = new XMPSequence("http://testaray", "array_2"
        );
        XMPString arrayItem1 = new XMPString("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve", "32,32");

        XMPString arrayItem3 = new XMPString("http://testItem/1.0/", "Item3", "Ping");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1", arrayElement1);
        xmpMetadataFactoryContext.addCollectionMetadata("ta:array_1[0]", arrayElement2);
        xmpMetadataFactoryContext.addSingleMetadata("ta:array_1[0]/crs:ToneCurve[0]", arrayItem1);
        xmpMetadataFactoryContext.addSingleMetadata("ta:array_1[1]/Item3", arrayItem3);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();
        assertThat(metadataList, hasSize(1));
        assertThat(metadataList.get(0), instanceOf(XMPCollection.class));

        XMPCollection foundArray = (XMPCollection) metadataList.get(0);
        assertThat(foundArray.getMetadata(), hasSize(2));
        assertThat(foundArray.getMetadata().get(0), equalTo((XMPMetadata) arrayElement2));
        assertThat(foundArray.getMetadata().get(1), equalTo((XMPMetadata) arrayItem3));
    }

    @Test
    public void shouldAddArrayWithStructAndRootStruct()
    {
        // given
        XMPSequence arrayMetadata = new XMPSequence("http://ns.adobe.com/xap/1.0/mm/", "HistoryA"
        );

        XMPStruct structMetadataA = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "HistoryA");
        XMPString structItemMetadataA = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changedA", "/");

        XMPStruct structMetadataB = new XMPStruct("http://ns.adobe.com/xap/1.0/mm/", "HistoryB");
        XMPString structItemMetadataB = new XMPString("http://ns.adobe.com/xap/1.0/mm/", "changedB", "/");

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("xmpMM:HistoryA", arrayMetadata);
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:HistoryA[0]", structMetadataA);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:HistoryA[0]/stEvt:changedA", structItemMetadataA);
        xmpMetadataFactoryContext.addStructMetadata("xmpMM:HistoryB", structMetadataB);
        xmpMetadataFactoryContext.addSingleMetadata("xmpMM:HistoryB/stEvt:changedB", structItemMetadataB);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(2));
        assertThat(metadataList.get(0), instanceOf(XMPCollection.class));
        XMPCollection foundArray = (XMPCollection) metadataList.get(0);
        assertThat(foundArray.getMetadata(), hasSize(1));
        assertThat(foundArray.getMetadata().get(0), equalTo((XMPMetadata) structMetadataA));

        assertThat(metadataList.get(1), instanceOf(XMPStruct.class));
        XMPStruct foundStruct = (XMPStruct) metadataList.get(1);
        assertThat(foundStruct.getMetadata(), hasSize(1));
        assertThat(foundStruct.getMetadata().get(0), equalTo((XMPMetadata) structItemMetadataB));
    }

    @Test
    public void shouldAddArrayAfterArray()
    {
        // given
        XMPSequence array1 = new XMPSequence("http://ns.adobe.com/camera-raw-settings/1.0/", "ToneCurve"
        );
        XMPSequence array2 = new XMPSequence("http://sampleArray", "testArray"
        );

        XMPMetadataFactoryContextBuilder xmpMetadataFactoryContext = new XMPMetadataFactoryContextBuilder();

        // when
        xmpMetadataFactoryContext.addCollectionMetadata("crs:ToneCurve", array1);
        xmpMetadataFactoryContext.addCollectionMetadata("ta:testArray", array2);

        // then
        List<XMPMetadata> metadataList = xmpMetadataFactoryContext.getMetadataList();

        assertThat(metadataList, hasSize(2));
        assertThat(metadataList.get(0), equalTo((XMPMetadata) array1));
        assertThat(metadataList.get(1), equalTo((XMPMetadata) array2));
    }
}