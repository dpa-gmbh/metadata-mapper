package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.collect.Lists;
import de.dpa.oss.metadata.mapper.processor.Processor;

import java.util.List;

/**
* @author oliver langer
*/
public class TestProcessor implements Processor
{
    private String valueToSet;

    @Override public List<String> process(final List<String> values)
    {
        return Lists.newArrayList(valueToSet);
    }

    public void setValueToSet(final String valueToSet)
    {
        this.valueToSet = valueToSet;
    }
}
