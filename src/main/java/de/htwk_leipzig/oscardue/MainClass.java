
package de.htwk_leipzig.oscardue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.htwk_leipzig.oscardue.model.Model;
import de.htwk_leipzig.oscardue.oscarParser.OscarParser;
import de.htwk_leipzig.oscardue.tmdb.TmdbParser;

public class MainClass
{
    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);
    private static String       academyAwardsPageLocation;
    private static String       ontologyLocation;
    private static Model        m;

    public MainClass () throws IOException
    {
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
            Preconditions.checkNotNull(ontologyLocation = properties.getProperty("ontologyLocation"));
            Preconditions.checkNotNull(academyAwardsPageLocation = properties.getProperty("academyAwardsPageLocation"));
            logger.debug("Main Properties file loaded");

        } catch (IOException e)
        {
            logger.error("Properties File could not be loaded, exiting");
            System.exit(-1);
        }

        m = new Model();
        OscarParser p = new OscarParser(m);

        for (int year = 1920; year < 1980; year = year + 10)
        {
            logger.info("Parsing year {} (+10)", year);
            p.parse(academyAwardsPageLocation, year, year + 9);
            write();
        }

        new TmdbParser(m).addImportantMovies(8);
    }

    public final static String getAcademyAwardsPageLocation ()
    {
        return academyAwardsPageLocation;
    }

    public final static String getOntologyLocation ()
    {
        return ontologyLocation;
    }

    public static void main (String[] args) throws IOException
    {
        new MainClass();
    }

    public static void write ()
    {

        try
        {
            logger.debug("Writing ttl");
            m.getOntModel().write(new FileOutputStream(ontologyLocation), "TURTLE");
            logger.info("ttl written");
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

}
