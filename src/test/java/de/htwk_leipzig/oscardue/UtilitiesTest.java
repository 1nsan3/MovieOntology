package de.htwk_leipzig.oscardue;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilitiesTest
{

    @Test
    public void test ()
    {
        assertEquals("1923-01-01", Utilities.convertDateToXSD("1923"));
    }

}
