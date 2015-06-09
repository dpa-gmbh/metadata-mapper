package de.dpa.oss.metadata.mapper.processor;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows to prepend and append strings to values. It only modifies values
 * which are not empty
 * @author oliver langer
 */
public class ModifyValueIfNotEmpty implements Processor
{
    private String prependString = null;
    private String appendString = null;

    @Override public List<String> process(final List<String> values)
    {
        List<String> result = new ArrayList<>();
        for (String value : values)
        {
            if(!Strings.isNullOrEmpty(value))
            {
                StringBuffer stringBuffer = new StringBuffer();
                if( !Strings.isNullOrEmpty(prependString ) )
                {
                    stringBuffer.append(prependString);
                }
                stringBuffer.append( value );

                if(!Strings.isNullOrEmpty(appendString))
                {
                    stringBuffer.append( appendString);
                }

                result.add( stringBuffer.toString());
            }
            else
            {
                result.add(value);
            }
        }
        return result;
    }

    public void setAppendString(final String appendString)
    {
        this.appendString = appendString;
    }

    public void setPrependString(final String prependString)
    {
        this.prependString = prependString;
    }
}
