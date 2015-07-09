
package de.htwk_leipzig.oscardue.tmdb;

import org.slf4j.LoggerFactory;
import org.yamj.api.common.exception.ApiExceptionType;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.methods.TmdbMovies;
import com.omertron.themoviedbapi.model.media.MediaCreditList;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.tools.HttpTools;

public class TmdbMoviesWrapper extends TmdbMovies
{

    public TmdbMoviesWrapper (String apiKey, HttpTools httpTools)
    {
        super(apiKey, httpTools);
    }

    @Override
    public MovieInfo getMovieInfo (int movieId, String language, String... appendToResponse) throws MovieDbException
    {
        try
        {
            return super.getMovieInfo(movieId, language, appendToResponse);
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
                return getMovieInfo(movieId, language, appendToResponse);
            } else
                throw e;
        }
    }

    @Override
    public MediaCreditList getMovieCredits (int movieId) throws MovieDbException
    {
        try
        {
            return super.getMovieCredits(movieId);
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
                return getMovieCredits(movieId);
            } else
                throw e;
        }
    }

}
