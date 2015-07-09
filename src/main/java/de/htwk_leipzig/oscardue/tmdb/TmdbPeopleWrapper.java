
package de.htwk_leipzig.oscardue.tmdb;

import org.slf4j.LoggerFactory;
import org.yamj.api.common.exception.ApiExceptionType;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.methods.TmdbPeople;
import com.omertron.themoviedbapi.model.person.PersonInfo;
import com.omertron.themoviedbapi.tools.HttpTools;

public class TmdbPeopleWrapper extends TmdbPeople
{

    public TmdbPeopleWrapper (String apiKey, HttpTools httpTools)
    {
        super(apiKey, httpTools);
    }

    @Override
    public PersonInfo getPersonInfo (int personId, String... appendToResponse) throws MovieDbException
    {
        try
        {
            return super.getPersonInfo(personId, appendToResponse);
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
                return getPersonInfo(personId, appendToResponse);
            } else
                throw e;
        }
    }

}
