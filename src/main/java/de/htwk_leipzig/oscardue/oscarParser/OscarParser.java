
package de.htwk_leipzig.oscardue.oscarParser;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.omertron.themoviedbapi.MovieDbException;

import de.htwk_leipzig.oscardue.Utilities;
import de.htwk_leipzig.oscardue.model.Model;
import de.htwk_leipzig.oscardue.tmdb.TmdbParser;

public class OscarParser
{
    private final Logger     logger = LoggerFactory.getLogger(getClass());
    private final Model      model;
    private final TmdbParser tmdbParser;

    public OscarParser (Model m)
    {
        model = m;
        tmdbParser = new TmdbParser(model);
    }

    public void parse (String fileToParse, int startYear, int endYear) throws IOException
    {
        File input = new File(fileToParse);
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements years = doc.getElementsByAttributeValue("class", "awardYearHeader");
        for (Element yearElement : years)
        {
            String[] s = yearElement.ownText().split(" ");
            int year = Integer.parseInt(s[0]);
            if (year < startYear || year > endYear)
                continue;
            logger.info("Year: {}", year);
            String number = s[1].substring(1, s[1].length() - 1);

            Element div = yearElement.parent().parent().nextElementSibling();
            while (div != null && div.tagName() == "div")
            {
                String type = div.child(0).ownText();
                logger.trace("Type: {}", type);

                Element table = div.nextElementSibling();
                while (table != null && table.tagName() == "table" && type != null && !type.equals("SPECIAL AWARD") && !type.equals("HONORARY AWARD"))
                {
                    Element tr = table.child(0).child(0);
                    boolean won = tr.child(0).hasText();
                    Element divNames = tr.child(1).child(0);
                    Element actorElement = divNames.child(0);
                    String actorID = actorElement.absUrl("href");
                    actorID = actorID.substring(actorID.length() - 5, actorID.length());
                    String actorName = actorElement.ownText();
                    table = table.nextElementSibling();

                    Element movieElement = divNames.child(1).child(0);

                    String movieID = movieElement.absUrl("href");
                    movieID = movieID.substring(movieID.length() - 5, movieID.length());

                    String movieName = movieElement.ownText();

                    String characterName = divNames.ownText().split("\"")[1];

                    boolean leading = false;
                    switch (type)
                    {
                        case "ACTOR":
                            //$FALL-THROUGH$
                        case "ACTRESS":
                            //$FALL-THROUGH$
                        case "ACTOR IN A LEADING ROLE":
                            //$FALL-THROUGH$
                        case "ACTRESS IN A LEADING ROLE":
                            leading = true;
                            break;
                        case "ACTOR IN A SUPPORTING ROLE":
                            //$FALL-THROUGH$
                        case "ACTRESS IN A SUPPORTING ROLE":
                            leading = false;
                    }
                    logger.debug("Actor:{}, {}, Role: {}, Movie: {}, {}", actorID, actorName, characterName, movieName, movieID);
                    logger.debug("Award: Leading: {}, Won: {}", leading, won);

                    Individual awardIndividual = leading ? model.getOntModel().createIndividual(Model.NS + "Oscars_" + year, model.getLeading())
                            : model.getOntModel().createIndividual(Model.NS + "Oscars_" + year, model.getSupporting());
                    awardIndividual.addLiteral(model.getYear(), ResourceFactory.createTypedLiteral(year + "-01-01", XSDDatatype.XSDdate));
                    Individual actorIndividual = null;
                    try
                    {
                        actorIndividual = tmdbParser.searchActorByNameAndaddToOntology(actorName).get();
                    } catch (MovieDbException e)
                    {
                        e.printStackTrace();
                    }
                    Individual movieIndividual = null;
                    try
                    {
                        movieIndividual = tmdbParser.searchMovieByNameAndaddToOntology(movieName, year);
                    } catch (MovieDbException e)
                    {
                        e.printStackTrace();
                    }

                    Individual awardedForIndividual;
                    if (won)
                    {
                        awardedForIndividual = model.getOntModel().createIndividual(
                                Model.NS + "won_" + Utilities.cleanURI(actorName + "_" + movieName), model.getWon());
                    } else
                    {
                        awardedForIndividual = model.getOntModel().createIndividual(
                                Model.NS + "nominated_" + Utilities.cleanURI(actorName + "_" + movieName), model.getNominated());
                    }
                    actorIndividual.addProperty(model.getActorAwardedFor(), awardedForIndividual);
                    movieIndividual.addProperty(model.getMovieAwardedFor(), awardedForIndividual);
                    awardedForIndividual.addProperty(model.getAwardedForAward(), awardIndividual);
                }
                div = table;
            }

        }
    }
}
