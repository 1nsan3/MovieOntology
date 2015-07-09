
package de.htwk_leipzig.oscardue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.slf4j.LoggerFactory;

public class Utilities
{
    public static String cleanURI (String uri)
    {
        return uri.replaceAll("[\\s#]", "_").replace("\"", "");
    }

    /**
     * converts a Date, to an xsd conform Date, i.e. if Month and Day are
     * missing, it adds them as 01-01
     * 
     * @param date
     * @return
     */
    public static String convertDateToXSD (String date)
    {
        try
        {
            return LocalDate.parse(date).toString();
        } catch (DateTimeParseException e)
        {
            LoggerFactory.getLogger(Utilities.class).debug("Just Year given");
            return LocalDate.of(Integer.parseInt(date.substring(0, 4)), 01, 01).toString();
        }

    }
}
