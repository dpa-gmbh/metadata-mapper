package de.dpa.oss.metadata.mapper.common;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils
{

    public final static String FORMAT_EXIF = "yyyy:MM:dd HH:mm:ss";
    public final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public final static String FORMAT_ISO8601_ALT = "yyyyMMdd'T'HHmmssZ";
    public final static String FORMAT_DATE = "yyyy-MM-dd";

    public static Date parseDate(String rawDate) throws ParseException
    {
        Date result = null;

        if (rawDate != null)
        {

            if (rawDate.matches("\\d{4}"))
            {
                // assume yyyy
                rawDate = "01.01." + rawDate;
            }
            else if (rawDate.matches("\\d{6}"))
            {
                // assume yyyyMM
                rawDate = "01." + rawDate.substring(4, 6) + "." + rawDate.substring(0, 4);
            }

            if (rawDate.matches("\\d{2}/\\d{2}/\\d{4}"))
            {
                rawDate = rawDate.replaceAll("/", ".");
            }

            if (rawDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"))
            {
                result = DateTimeUtils.parse(rawDate, "dd.MM.yyyy", "DE", "DE");
            }
            else if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                result = new SimpleDateFormat(DateTimeUtils.FORMAT_DATE).parse(rawDate);
            }
            else if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{1,2}:\\d{1,2}") && rawDate.length() != 19)
            {
                String iso8601 = rawDate.substring(0, rawDate.lastIndexOf('T'));
                result = DateTimeUtils.toDate(iso8601);
            }
            else if (rawDate.matches("\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}") && rawDate.length() == 19)
            {
                result = new SimpleDateFormat(DateTimeUtils.FORMAT_EXIF).parse(rawDate);
            }
            else if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{1,2}:\\d{1,2}[+-]\\d{1,2}:\\d{1,2}"))
            {
                DateTimeFormatter dtf = ISODateTimeFormat.dateTimeNoMillis();
                DateTime dt = dtf.parseDateTime(rawDate);
                return dt.toGregorianCalendar().getTime();
            }

            else if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{1,2}:\\d{1,2}Z"))
            {
                DateTimeFormatter dtf = ISODateTimeFormat.dateTimeNoMillis();
                DateTime dt = dtf.parseDateTime(rawDate);
                return dt.toGregorianCalendar().getTime();
            }

            else if (rawDate.matches("\\d{8}T\\d{6}[+-]\\d{4}"))
            {
                return new SimpleDateFormat(DateTimeUtils.FORMAT_ISO8601_ALT).parse(rawDate);
            }
        }
        return result;
    }

    public static Date toDate(String iso8601) throws ParseException
    {
        Date date = null;
        if (trimToNull(iso8601) != null)
        {
            StringBuilder builder = new StringBuilder(iso8601);
            builder.deleteCharAt(22);
            date = new SimpleDateFormat(DateTimeUtils.FORMAT_ISO8601).parse(builder.toString());
        }
        return date;
    }

    public static Date parse(String str, String pattern, String locale, String timezone) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf;
        if (locale != null)
        {
            sdf = new SimpleDateFormat(pattern, new Locale(locale));
        }
        else
        {
            sdf = new SimpleDateFormat(pattern);
        }
        if (timezone != null)
        {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }

        Date date = sdf.parse(str);

        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        if (year < 99)
        {
            if (year > 50)
            {
                year += 1900;
            }
            else
            {
                year += 2000;
            }
            calendar.set(Calendar.YEAR, year);
        }
        return calendar.getTime();
    }

    public static String trimTo( final String str, String strIfNullOrEmpty )
    {
        final String toReturn;

        if( str != null )
        {
            final String trimedString;
            trimedString = str.trim();
            if(Strings.isNullOrEmpty( trimedString ) )
            {
                toReturn = strIfNullOrEmpty;
            }
            else
            {
                toReturn = trimedString;
            }
        }
        else
        {
            toReturn = strIfNullOrEmpty;
        }

        return toReturn;
    }

    public static String trimToNull( final String str )
    {
        return trimTo( str,null);
    }
}