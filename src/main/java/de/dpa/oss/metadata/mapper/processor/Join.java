package de.dpa.oss.metadata.mapper.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Joins all values of a list to one single value
 *
 * @author oliver langer
 */
public class Join
                implements Processor
{
    private String delimiter = "";

    @Override public List<String> process(final List<String> values)
    {
        return Lists.newArrayList(Joiner.on(delimiter).skipNulls().join(values));
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }
}
