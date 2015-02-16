package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.commandline.RootEntryWriter;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class RootEntryWriterTest
{
    @Test
    public void shouldWriteSimpleKeyValue()
    {
        // given
        ListMultimap<String,String> keyValues = ArrayListMultimap.create();

        RootEntryWriter rootEntryWriter = new RootEntryWriter();
        // when
        rootEntryWriter.write("namespace", "key", "value");
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat( keyValueMaps, hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat( entries.get( "namespace:key"), is(notNullValue()));
        assertThat(entries.get("namespace:key"), hasItem("value"));
    }

    @Test
    public void shouldCreateArray()
    {
        // given
        // given


        RootEntryWriter rootEntryWriter = new RootEntryWriter();

        // when
        rootEntryWriter.beginArray("namespaceRef", "array").write( "entry1" ).write( "entry2").endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps, hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat(entries.get( "namespaceRef:array"), hasSize(2));
        List<String> structs = entries.get("namespaceRef:array");
        assertThat(structs.get(0), is("entry1"));
        assertThat(structs.get(1), is("entry2"));
    }

    @Test
    public void shouldCreateArrayOfStruct()
    {
        // given
        // given
        RootEntryWriter rootEntryWriter = new RootEntryWriter();

        // when
        rootEntryWriter
                .beginArray("namespaceRef", "arrayOfStruct")
                .beginStruct("namespaceRef", "arrayStruct1").write("namespaceRef", "arrayStructKey1", "arrayStructVal1")
                .endStruct()
                .beginStruct("namespaceRef", "arrayStruct2").write("namespaceRef", "arrayStructKey2", "arrayStructVal2")
                .endStruct()
                .endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps,hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat(entries.get( "namespaceRef:arrayOfStruct"), hasSize(2));
        List<String> structs = entries.get("namespaceRef:arrayOfStruct");
        assertThat(structs.get(0), is("{arrayStructKey1=arrayStructVal1}"));
        assertThat(structs.get(1), is("{arrayStructKey2=arrayStructVal2}"));
    }

    @Test
    public void shouldReturnComplexStructure()
    {
        // given
        // given
        RootEntryWriter rootEntryWriter = new RootEntryWriter();

        // when
        rootEntryWriter.write("namespaceRef", "simpleKey", "value");
        rootEntryWriter.beginArray("namespaceRef", "array").write( "entry1" ).write( "entry2").endArray();
        rootEntryWriter.beginStruct("namespaceRef", "ssimpleSruct").write("namespaceRef", "structKey1", "structValue1").write("namespaceRef", "structKey2", "structValue2").endStruct();
        rootEntryWriter.beginStruct("namespaceRef", "structWithArray").write("namespaceRef", "structKey3", "structValue3")
                .beginArray("namespaceRef", "structArray").write("structArrayVal1").write("structArrayVal2").endArray()
                .write("namespaceRef", "structKey4", "structValue4").endStruct();
        rootEntryWriter.beginLangAlt("namespaceRef", "title").write("namespaceRef", "de", "titel").write("namespaceRef", "en", "title").endLangAlt();
        rootEntryWriter
                .beginArray("namespaceRef", "arrayOfStruct")
                    .beginStruct("namespaceRef", "arrayStruct1").write("namespaceRef", "arrayStructKey1", "arrayStructVal1")
                    .endStruct()
                    .beginStruct("namespaceRef", "arrayStruct2").write("namespaceRef", "arrayStructKey2", "arrayStructVal2")
                    .endStruct()
                .endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps, hasSize(1));
    }
}