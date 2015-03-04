package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.collect.Multimap;
import de.dpa.oss.common.ResourceUtil;
import de.dpa.oss.metadata.mapper.MetadataMapperUtil;
import de.dpa.oss.metadata.mapper.common.XmlUtils;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;

public class MetadataProcessingInfoTest
{
    @Test
    public void shouldSelectViaSimpleXPathValueSelection() throws Exception
    {
        // given
        String xmlDocument = ResourceUtil
                .resourceAsString("/content/imageMetadata/metadata-processing-info-test-input.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);

        MappingType mapping = MetadataMapperUtil
                .getDefaultConfigOverridenBy("/content/imageMetadata/metadata-processing-info-test-mapping.xml", this.getClass());

        MetadataProcessingInfo processing = new MetadataProcessingInfo(mapping.getMetadata().get(0));

        // when
        Multimap<String, String> result = processing.selectXPathValues(document);

        // then
        assertThat(result, is(notNullValue()));
        assertThat(result.keys(), contains("example"));
        Collection<String> strings = result.get("example");
        assertThat( strings,hasSize(1));
        assertThat( strings.iterator().next(), is("A string node"));
    }

    @Test
    public void shouldSelectViaXPathFunction() throws Exception
    {
        // given
        String xmlDocument = ResourceUtil
                .resourceAsString("/content/imageMetadata/metadata-processing-info-test-input.xml", this.getClass());
        Document document = XmlUtils.toDocument(xmlDocument);

        MappingType mapping = MetadataMapperUtil
                .getDefaultConfigOverridenBy("/content/imageMetadata/metadata-processing-info-test-mapping.xml", this.getClass());

        MetadataProcessingInfo processing = new MetadataProcessingInfo(mapping.getMetadata().get(1));

        // when
        Multimap<String, String> result = processing.selectXPathValues(document);

        // then
        assertThat(result, is(notNullValue()));
        assertThat(result.keys(), contains("example"));
        Collection<String> strings = result.get("example");
        assertThat( strings,hasSize(1));
        assertThat( strings.iterator().next(), is("text returned as string"));
    }
}