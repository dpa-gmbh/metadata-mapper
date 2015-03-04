package de.dpa.oss.metadata.mapper;

import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CustomizedMappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMEncodingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.MappingType.Metadata;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class MetadataMapperConfigReaderTest
{
    @Test
    public void shouldReadDefaultCOnfig() throws JAXBException
    {
        // given
        // when
        MappingType mappingType = new MetadataMapperConfigReader().getDefaultConfig();

        // then
        assertThat(mappingType, is(notNullValue()));
        assertThat(mappingType.getName(), is("defaultConfig"));
    }

    @Test
    public void shouldOverrideMappings() throws JAXBException
    {
        // given

        // when
        CustomizedMappingType customizedDefaultConfig = new MetadataMapperConfigReader().readCustomizedDefaultConfig(this.getClass()
                .getResourceAsStream("/content/overriding-mapping.xml"));

        MappingType defaultConfig = new MetadataMapperConfigReader().getDefaultConfig();

        // then
        assertThat(customizedDefaultConfig, is(Matchers.notNullValue()));
        assertThat(defaultConfig, is(Matchers.notNullValue()));
        assertThat(customizedDefaultConfig.getMetadata().size(), is(defaultConfig.getMetadata().size()));

        Metadata customizedEntry = null;

        for (Metadata metadata : customizedDefaultConfig.getMetadata())
        {
            if ("Headline".equals(metadata.getName()))
            {
                customizedEntry = metadata;
            }
        }

        assertThat(customizedEntry, is(Matchers.notNullValue()));
        assertThat(customizedEntry.getIim(), is(Matchers.notNullValue()));
        List<IIMMapping.MapsTo> mapsTo = customizedEntry.getIim().getMapsTo();
        assertThat(mapsTo, is(Matchers.notNullValue()));
        assertThat(mapsTo, hasSize(1));
        assertThat(mapsTo.get(0).getField(), is("City"));

        assertThat( customizedEntry.getXmp(), is(notNullValue()));
        assertThat( customizedEntry.getXmp().getMapsTo(), hasSize(1));
        assertThat( customizedEntry.getXmp().getMapsTo().get(0).getField(), is("UsageTerms"));
        assertThat( customizedEntry.getXmp().getMapsTo().get(0).getTargetNamespace(), is("http://ns.adobe.com/xap/1.0/rights/"));

    }

    @Test
    public void shouldOvertakeNonCustomizedMapping() throws JAXBException
    {
        // given

        // when
        CustomizedMappingType customizedDefaultConfig = new MetadataMapperConfigReader().readCustomizedDefaultConfig(this.getClass()
                .getResourceAsStream("/content/overriding-mapping.xml"));

        MappingType defaultConfig = new MetadataMapperConfigReader().getDefaultConfig();

        // then
        assertThat(customizedDefaultConfig, is(Matchers.notNullValue()));
        assertThat(defaultConfig, is(Matchers.notNullValue()));
        assertThat(customizedDefaultConfig.getMetadata().size(), is(defaultConfig.getMetadata().size()));

        Map<String, Metadata> nameToMetadata = new HashMap<>();
        for (Metadata metadata : defaultConfig.getMetadata())
        {
            nameToMetadata.put( metadata.getName(), metadata );
        }

        for (Metadata customMetadata : customizedDefaultConfig.getMetadata())
        {
            assertThat( nameToMetadata.remove( customMetadata.getName()), is(Matchers.notNullValue()));
        }

        assertThat(nameToMetadata.size(), is(0));
    }

    @Test
    public void shouldOverrideConfig() throws JAXBException
    {
        // given
        CustomizedMappingType customizedDefaultConfig = new MetadataMapperConfigReader().readCustomizedDefaultConfig(this.getClass()
                .getResourceAsStream("/content/overriding-mapping.xml"));

        // then
        assertThat(customizedDefaultConfig.getConfig(), is(Matchers.notNullValue()));
        assertThat(customizedDefaultConfig.getConfig().getTimezone(), is("Europe/London"));
        assertThat(customizedDefaultConfig.getConfig().getIim().getCharset(), is(IIMEncodingType.UTF_8));

    }
}