package de.dpa.oss.metadata.mapper.common;

import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeUtils
{
    private static Logger logger = Logger.getLogger(DateTimeUtils.class);

    private static Set<String> SUPPORTED_TZ = new TreeSet<String>();
    public final static String FORMAT_EXIF = "yyyy:MM:dd HH:mm:ss";
    public final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public final static String FORMAT_ISO8601_ALT = "yyyyMMdd'T'HHmmssZ";
    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    public final static String FORMAT_DATE = "yyyy-MM-dd";

    public static boolean isFuture(String iso8601)
    {
        Date date;
        try
        {
            date = DateTimeUtils.toDate(iso8601);

            if (date != null)
            {
                if (date.compareTo(new Date()) > 0)
                {
                    return true;
                }
            }
            return false;
        }
        catch (ParseException e)
        {
            return false;
        }
    }

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

    public static Date parseDateSilent(String rawDate)
    {
        try
        {
            return parseDate(rawDate);
        }
        catch (Exception e)
        {
            // silent
        }
        return null;
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

    public static String concatenateDateTime(Date date, String time)
    {

        if (date == null)
        {
            return null;
        }

        String d = new SimpleDateFormat(DateTimeUtils.FORMAT_DATE).format(date);
        if (!Strings.isNullOrEmpty(time))
        {
            StringBuilder timeBuilder = new StringBuilder(time);

            if (time.length() <= 6)
            {
                timeBuilder.append("+0000");
            }

            if (!time.contains(":"))
            {
                timeBuilder.insert(2, ":");
                timeBuilder.insert(5, ":");
                timeBuilder.insert(11, ":");
            }

            return d + "T" + timeBuilder.toString();
        }
        return d;
    }

    public static String formatISO8601(String date, String time)
    {
        if (date != null)
        {

            StringBuilder dateBuilder = new StringBuilder(date);
            StringBuilder timeBuilder = null;

            dateBuilder.insert(4, "-");
            dateBuilder.insert(7, "-");

            if (trimToNull(time) == null)
            {
                timeBuilder = new StringBuilder("00:00:00+02:00");
            }
            else
            {
                timeBuilder = new StringBuilder(time);

                if (time.length() <= 6)
                {
                    timeBuilder.append("+0200");
                }

                if (!time.contains(":"))
                {
                    timeBuilder.insert(2, ":");
                    timeBuilder.insert(5, ":");
                    timeBuilder.insert(11, ":");
                }
            }
            return dateBuilder.toString() + "T" + timeBuilder.toString();
        }
        return "";
    }

    public static String formatISO8601(Date date)
    {
        String result = new SimpleDateFormat(DateTimeUtils.FORMAT_ISO8601).format(date);
        StringBuilder builder = new StringBuilder(result);
        builder.insert(22, ":");

        return builder.toString();
    }

    public static String cleanDirtyTime(String time)
    {
        time = trimTo(time,"");

        StringBuilder sb = new StringBuilder(time);

        if (sb.length() == 4)
        {
            sb.insert(2, ":");
            sb.append(":00");
        }

        if (sb.length() == 6)
        {
            sb.insert(2, ":");
            sb.insert(5, ":");
        }

        if (sb.toString().matches("\\d{6}(\\+|\\-)\\d{4}"))
        {
            sb.insert(2, ":");
            sb.insert(5, ":");
        }

        String validTime = "(([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]).*";
        String validTimezone = "(\\+|\\-)([0]?[0-9]|1[0-4]):*[0-5][0-9]";
        Pattern pattern = Pattern.compile(validTime);
        Pattern pattern2 = Pattern.compile(validTimezone);

        Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.matches())
        {
            time = matcher.group(1);

            sb.delete(0, time.length());

            Matcher matcher2 = pattern2.matcher(sb.toString());
            if (matcher2.matches())
            {
                if (sb.length() == 5)
                {
                    sb.insert(3, ":");
                }
                time += sb.toString();
            }
        }
        else
        {
            time = "";
        }

        return time;
    }

    public static Date parseSilent(String str, String pattern, String locale, String timezone)
    {
        Date result = null;
        try
        {
            result = DateTimeUtils.parse(str, pattern, locale, timezone);
        }
        catch (ParseException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("unable to parse str='").append(str).append("' ");
            sb.append("pattern='").append(pattern).append("' ");
            sb.append("exception='").append(e.getMessage()).append("'");
            logger.info(sb.toString());
        }
        return result;
    }

    public static Date parse(String str, String pattern, String locale, String timezone) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = null;
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

    public static boolean tzIsSupported(String tz)
    {
        if (DateTimeUtils.SUPPORTED_TZ.isEmpty())
        {
            synchronized (DateTimeUtils.SUPPORTED_TZ)
            {
                String[] ids = TimeZone.getAvailableIDs();
                for (String id : ids)
                {
                    DateTimeUtils.SUPPORTED_TZ.add(id);
                }
            }
        }
        return tz == null ? false : DateTimeUtils.SUPPORTED_TZ.contains(tz);
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