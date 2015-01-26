package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ArrayEntryWriter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArrayEntryWriterTest
{
    @Test
    public void shouldWriteSimpleValues()
    {
        // given
        ArrayEntryWriter array = new ArrayEntryWriter();

        // when
        array.write("key1", "value1" );
        array.write("key2", "value2");

        // then
        assertThat( array.endArray(), is("[value1,value2]"));
    }

    @Test
    public void shouldWriteSingleEntry()
    {
        // given
        ArrayEntryWriter array = new ArrayEntryWriter();

        // when
        array.write("key1", "value1" );

        // then
        assertThat( array.endArray(), is("[value1]"));
    }
}