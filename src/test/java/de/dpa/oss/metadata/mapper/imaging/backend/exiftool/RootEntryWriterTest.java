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

        RootEntryWriter rootEntryWriter = new RootEntryWriter(keyValues);
        // when
        rootEntryWriter.write( "key", "value");
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat( keyValueMaps, hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat( entries.get( "key"), is(notNullValue()));
        assertThat(entries.get("key"), hasItem("value"));
    }

    @Test
    public void shouldCreateArray()
    {
        // given
        // given
        ListMultimap<String,String> keyValues = ArrayListMultimap.create();

        RootEntryWriter rootEntryWriter = new RootEntryWriter(keyValues);

        // when
        rootEntryWriter.beginArray("array").write( "entry1" ).write( "entry2").endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps, hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat(entries.get( "array"), hasSize(2));
        List<String> structs = entries.get("array");
        assertThat(structs.get(0), is("entry1"));
        assertThat(structs.get(1), is("entry2"));
    }

    @Test
    public void shouldCreateArrayOfStruct()
    {
        // given
        // given
        ListMultimap<String,String> keyValues = ArrayListMultimap.create();

        RootEntryWriter rootEntryWriter = new RootEntryWriter(keyValues);

        // when
        rootEntryWriter
                .beginArray( "arrayOfStruct")
                .beginStruct("arrayStruct1").write( "arrayStructKey1", "arrayStructVal1")
                .endStruct()
                .beginStruct("arrayStruct2").write( "arrayStructKey2", "arrayStructVal2")
                .endStruct()
                .endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps,hasSize(1));
        ListMultimap<String,String> entries = keyValueMaps.get(0);
        assertThat(entries.get( "arrayOfStruct"), hasSize(2));
        List<String> structs = entries.get("arrayOfStruct");
        assertThat(structs.get(0), is("{arrayStructKey1=arrayStructVal1}"));
        assertThat(structs.get(1), is("{arrayStructKey2=arrayStructVal2}"));
    }

    @Test
    public void shouldReturnComplexStructure()
    {
        // given
        // given
        ListMultimap<String,String> keyValues = ArrayListMultimap.create();

        RootEntryWriter rootEntryWriter = new RootEntryWriter(keyValues);

        // when
        rootEntryWriter.write("simpleKey", "value");
        rootEntryWriter.beginArray("array").write( "entry1" ).write( "entry2").endArray();
        rootEntryWriter.beginStruct("ssimpleSruct").write( "structKey1", "structValue1").write( "structKey2", "structValue2").endStruct();
        rootEntryWriter.beginStruct("structWithArray").write( "structKey3", "structValue3")
                .beginArray("structArray").write("structArrayVal1").write("structArrayVal2").endArray()
                .write("structKey4", "structValue4").endStruct();
        rootEntryWriter.beginLangAlt("title").write("de", "titel").write("en", "title").endLangAlt();
        rootEntryWriter
                .beginArray( "arrayOfStruct")
                    .beginStruct("arrayStruct1").write( "arrayStructKey1", "arrayStructVal1")
                    .endStruct()
                    .beginStruct("arrayStruct2").write( "arrayStructKey2", "arrayStructVal2")
                    .endStruct()
                .endArray();
        List<ListMultimap<String, String>> keyValueMaps = rootEntryWriter.getKeyValueMaps();

        // then
        assertThat(keyValueMaps, hasSize(1));
    }
}