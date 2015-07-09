
package de.htwk_leipzig.oscardue.tmdb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.http.impl.client.cache.CachingHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.enumeration.SortBy;
import com.omertron.themoviedbapi.methods.TmdbDiscover;
import com.omertron.themoviedbapi.methods.TmdbMovies;
import com.omertron.themoviedbapi.methods.TmdbPeople;
import com.omertron.themoviedbapi.methods.TmdbSearch;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.discover.Discover;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.model.person.PersonFind;
import com.omertron.themoviedbapi.model.person.PersonInfo;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.HttpTools;

import de.htwk_leipzig.oscardue.MainClass;
import de.htwk_leipzig.oscardue.Utilities;
import de.htwk_leipzig.oscardue.imdb.ImdbParser;
import de.htwk_leipzig.oscardue.model.Model;

public class TmdbParser
{
    private static String apiKey;
    private final Logger        logger       = LoggerFactory.getLogger(getClass());
    private final TmdbSearch    tmdbSearch   = new TmdbSearchWrapper(apiKey, new HttpTools(new CachingHttpClient()));
    private final TmdbDiscover  tmdbDiscover = new TmdbDiscoverWrapper(apiKey, new HttpTools(new CachingHttpClient()));
    private final TmdbMovies    tmdbMovies   = new TmdbMoviesWrapper(apiKey, new HttpTools(new CachingHttpClient()));
    private final TmdbPeople    tmdbPeople   = new TmdbPeopleWrapper(apiKey, new HttpTools(new CachingHttpClient()));
    private final Model         model;

    public TmdbParser (Model model)
    {
        this.model = model;
        try
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream("src/main/resources/private.properties"));
            Preconditions.checkNotNull(apiKey= properties.getProperty("tmdbKey"));
            logger.debug("ApiKey loaded");
            
        } catch (IOException e)
        {
            logger.error("ApiKey Properties File could not be loaded, exiting");
            System.exit(-1);
        } 
    }

    /**
     * looks for an actor and adds the best matching actor to the ontology with
     * the TMDB ID in Braces
     * 
     * @param name
     * @param model
     * @return
     * @throws MovieDbException
     */
    public Optional<Individual> searchActorByNameAndaddToOntology (String name) throws MovieDbException
    {

        List<PersonFind> persons = tmdbSearch.searchPeople(name, 1, false, SearchType.PHRASE).getResults();
        if (persons.size() == 0)
            return Optional.empty();
        PersonFind person = persons.get(0);
        Individual actorIndividual = searchActorByIDAndAddToOntology(person.getId());

        return Optional.of(actorIndividual);
    }

    public Individual searchActorByIDAndAddToOntology (int id) throws MovieDbException
    {

        PersonInfo personInfo = tmdbPeople.getPersonInfo(id, null);
        Individual actorIndividual = model.getOntModel().createIndividual(
                Model.NS + Utilities.cleanURI(personInfo.getName()) + "(" + personInfo.getId() + ")", model.getActor());
        actorIndividual.addLiteral(model.getName(), personInfo.getName());
        actorIndividual.addLabel(model.getOntModel().createLiteral(personInfo.getName()));
        actorIndividual.addLiteral(model.getTmdbid(), personInfo.getId());
        if (personInfo.getBirthday() != null && !personInfo.getBirthday().equals(""))
        {
            try
            {
                String birthday = Utilities.convertDateToXSD(personInfo.getBirthday());
                actorIndividual.addLiteral(model.getBirthday(), ResourceFactory.createTypedLiteral(birthday, XSDDatatype.XSDdate));
            } catch (NumberFormatException e)
            {
                logger.warn("Wrong numberformat for Birthday");
            }
        }
        if (personInfo.getDeathday() != null && !personInfo.getDeathday().equals(""))
        {
            try
            {
                String deathDay = Utilities.convertDateToXSD(personInfo.getDeathday());
                actorIndividual.addLiteral(model.getDeathday(), ResourceFactory.createTypedLiteral(deathDay, XSDDatatype.XSDdate));
            } catch (NumberFormatException e)
            {
                logger.warn("Wrong numberformat for Deathday");
            }
        }
        return actorIndividual;

    }

    public Individual searchMovieByNameAndaddToOntology (String name, int maxyear) throws MovieDbException
    {
        List<MovieInfo> movies = tmdbSearch.searchMovie(name, 0, null, false, 0, 0, SearchType.PHRASE).getResults();
        MovieInfo movie = movies
                .stream()
                .filter(movieInfo -> movieInfo.getReleaseDate() != null && movieInfo.getReleaseDate().length() >= 4
                        && Integer.parseInt(movieInfo.getReleaseDate().substring(0, 4)) <= maxyear + 1).findFirst().get();
        logger.debug("Found movie {}", movie.getId());
        Individual movieIndividual = createMovieIndividualByID(movie.getId());
        return movieIndividual;
    }

    private void createCreditsForMovie (Individual movieIndividual)
    {
        try
        {
            List<MediaCreditCast> cast = tmdbMovies.getMovieCredits(movieIndividual.getProperty(model.getTmdbid()).getInt()).getCast();
            logger.debug("found {} cast members", cast.size());
            for (MediaCreditCast mediaCreditCast : cast)
            {
                logger.debug("Parsing Person {}", mediaCreditCast.getName());
                Individual actorIndividual = searchActorByIDAndAddToOntology(mediaCreditCast.getId());
                Individual characterIndividual = model.getOntModel().createIndividual(
                        Model.NS + Utilities.cleanURI(mediaCreditCast.getCharacter()) + "(" + mediaCreditCast.getId() + ")", model.getCharacter());
                characterIndividual.addLiteral(model.getName(), mediaCreditCast.getCharacter());
                characterIndividual.addLabel(model.getOntModel().createLiteral(mediaCreditCast.getCharacter()));
                characterIndividual.addProperty(model.getActedBy(), actorIndividual);
                characterIndividual.addProperty(model.getActedIn(), movieIndividual);
                characterIndividual.addLiteral(model.getTmdbOrder(), mediaCreditCast.getOrder());
                characterIndividual.addLiteral(model.getTmdbCastID(), mediaCreditCast.getCastId());
            }
        } catch (MovieDbException e)
        {
            return;
        }
    }

    private Individual createMovieIndividualByID (int id) throws MovieDbException
    {
        MovieInfo movie = tmdbMovies.getMovieInfo(id, null, null);
        logger.debug("found {}", movie.getTitle());
        Individual movieIndividual = model.getOntModel().createIndividual(
                Model.NS + movie.getTitle().replaceAll("\\s", "_") + "(" + movie.getReleaseDate().substring(0, 4) + ")", model.getMovie());
        movieIndividual.addLiteral(model.getName(), movie.getTitle());
        movieIndividual.addLabel(model.getOntModel().createLiteral(movie.getTitle()));
        movieIndividual.addLiteral(model.getTmdbid(), movie.getId());
        movieIndividual.addLiteral(model.getTmdbRating(), movie.getVoteAverage());
        movieIndividual.addLiteral(model.getTmdbVoteCount(), movie.getVoteCount());
        movieIndividual.addLiteral(model.getTmdbPopularity(), movie.getPopularity());
        movieIndividual.addLiteral(model.getImdbid(), movie.getImdbID());

        ImdbParser parser = new ImdbParser(model);
        parser.parseImdb(movieIndividual, movie.getImdbID());

        createCreditsForMovie(movieIndividual);

        return movieIndividual;

    }

    public void addImportantMovies (int page)
    {
        Discover discover = new Discover();
        discover.includeAdult(false);
        discover.voteAverageGte(7);
        discover.voteCountGte(50);
        discover.sortBy(SortBy.VOTE_AVERAGE_DESC);
        try
        {
            ResultList<MovieBasic> discoverMovies = tmdbDiscover.getDiscoverMovies(discover);
            for (int i = page; i <= discoverMovies.getTotalPages(); i++)
            {
                logger.info("adding page {} of {}", i, discoverMovies.getTotalPages());
                discover.page(i);
                discoverMovies = tmdbDiscover.getDiscoverMovies(discover);
                for (MovieBasic movie : discoverMovies.getResults())
                {
                    Individual movieIndividual = createMovieIndividualByID(movie.getId());
                }
                MainClass.write();
            }

        } catch (MovieDbException e)
        {
            e.printStackTrace();
        }
    }
}
