
package de.htwk_leipzig.oscardue.tmdb;

import org.slf4j.LoggerFactory;
import org.yamj.api.common.exception.ApiExceptionType;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.methods.TmdbSearch;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.model.person.PersonFind;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.HttpTools;

public class TmdbSearchWrapper extends TmdbSearch
{

    public TmdbSearchWrapper (String apiKey, HttpTools httpTools)
    {
        super(apiKey, httpTools);
    }

    @Override
    public ResultList<PersonFind> searchPeople (String query, Integer page, Boolean includeAdult, SearchType searchType) throws MovieDbException
    {
        try
        {
            return super.searchPeople(query, page, includeAdult, searchType);
        } catch (MovieDbException e)
        {
            if (e.getExceptionType() == ApiExceptionType.HTTP_404_ERROR && e.getResponseCode() == 429)
            {
                LoggerFactory.getLogger(getClass()).debug("Request limit reached, waiting");
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }
                return searchPeople(query, page, includeAdult, searchType);
            } else
                throw e;
        }
    }

    @Override
    public ResultList<MovieInfo> searchMovie (String query, Integer page, String language, Boolean includeAdult, Integer searchYear,
            Integer primaryReleaseYear, SearchType searchType) throws MovieDbException
    {
        try
        {
            return super.searchMovie(query, page, language, includeAdult, searchYear, primaryReleaseYear, searchType);
        } catch (MovieDbException e)
        {
            if (e.getExceptionType() == ApiExceptionType.HTTP_404_ERROR && e.getResponseCode() == 429)
            {
                LoggerFactory.getLogger(getClass()).debug("Request limit reached, waiting");
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }
                return searchMovie(query, page, language, includeAdult, searchYear, primaryReleaseYear, searchType);
            } else
                throw e;
        }
    }

}
