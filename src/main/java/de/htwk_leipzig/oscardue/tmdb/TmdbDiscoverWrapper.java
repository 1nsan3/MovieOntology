
package de.htwk_leipzig.oscardue.tmdb;

import org.slf4j.LoggerFactory;
import org.yamj.api.common.exception.ApiExceptionType;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.methods.TmdbDiscover;
import com.omertron.themoviedbapi.model.discover.Discover;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.HttpTools;

/**
 * Wraps the {@link TmdbDiscover} Class from {@link TheMovieDbApi} so that it
 * handles the 429 exception and waits
 * 
 * @author Gero Kraus
 *
 */
public class TmdbDiscoverWrapper extends TmdbDiscover
{

    public TmdbDiscoverWrapper (String apiKey, HttpTools httpTools)
    {
        super(apiKey, httpTools);
    }

    @Override
    public ResultList<MovieBasic> getDiscoverMovies (Discover discover) throws MovieDbException
    {
        try
        {
            return super.getDiscoverMovies(discover);
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
                return getDiscoverMovies(discover);
            }
            throw e;
        }
    }

}
