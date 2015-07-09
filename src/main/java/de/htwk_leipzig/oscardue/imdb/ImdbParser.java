
package de.htwk_leipzig.oscardue.imdb;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.Individual;

import de.htwk_leipzig.oscardue.model.Model;

public class ImdbParser
{
    private static final String URL    = "http://www.omdbapi.com/?i=%s&plot=short&r=json";
    private final Model         model;
    private final Client        client;
    private final Logger        logger = LoggerFactory.getLogger(getClass());

    public ImdbParser (Model model)
    {
        this.model = model;
        client = ClientBuilder.newClient();
    }

    public void parseImdb (Individual movieIndividual, String id)
    {
        logger.debug("Parsing imdb id {}", id);

        ObjectMapper objectMapper = new ObjectMapper();

        Imdb imdb;
        try
        {
            imdb = objectMapper.readValue(client.target(String.format(URL, id)).request("application/json").get(String.class), Imdb.class);

            try
            {
                float rating = Float.parseFloat(imdb.getImdbRating());
                movieIndividual.addLiteral(model.getImdbRating(), rating);

                int votes = Integer.parseInt(imdb.getImdbVotes());
                movieIndividual.addLiteral(model.getImdbVoteCount(), votes);
            } catch (NumberFormatException e)
            {
                logger.info("No rating/votes given");
            }

            // TODO Also add Actors Listed
        } catch (IOException e)
        {
            e.printStackTrace();
            logger.warn("Couldn't read OMDB");
            return;
        } catch (NullPointerException e)
        {
            logger.warn("Imdb ID not given");
            return;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Imdb
    {
        private String imdbVotes;
        private String imdbRating;
        @JsonProperty("Actors")
        private String Actors;

        public String getImdbVotes ()
        {
            return imdbVotes;
        }

        public String getImdbRating ()
        {
            return imdbRating;
        }

        public String getActors ()
        {
            return Actors;
        }

        public Set<String> getActorSet ()
        {
            return Sets.newHashSet(Actors.split(", "));
        }
    }
}
