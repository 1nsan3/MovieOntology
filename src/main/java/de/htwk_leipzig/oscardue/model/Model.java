
package de.htwk_leipzig.oscardue.model;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.XSD;

import de.htwk_leipzig.oscardue.MainClass;

public class Model
{

    public static final String       NS     = "http://www.imn.htwk-leipzig.de/~gkraus#";
    public static final String       foafNS = "http://xmlns.com/foaf/0.1/";
    private final OntModel           model;
    private final OntClass           actor;
    private final OntClass           movie;
    private final OntClass           award;
    private final ObjectProperty     actedIn;
    private final OntClass           acting;
    private final OntClass           supporting;
    private final OntClass           leading;
    private final ObjectProperty     actedBy;
    private final OntClass           character;
    private final OntClass           awardedFor;
    private final OntClass           nominated;
    private final OntClass           won;

    private final ObjectProperty     actorAwardedFor;
    private final ObjectProperty     movieAwardedFor;
    private final ObjectProperty     awardedForAward;

    private final Property           name;
    private final DatatypeProperty   year;
    private final DatatypeProperty   tmdbid;
    private final DatatypeProperty   tmdbRating;
    private final DatatypeProperty   tmdbVoteCount;
    private final DatatypeProperty   imdbid;
    private final DatatypeProperty   imdbRating;
    private final DatatypeProperty   imdbVoteCount;
    private final DatatypeProperty   tmdbPopularity;
    private final DatatypeProperty   tmdbOrder;
    private final DatatypeProperty   tmdbCastID;
    private final Property           birthday;
    private final AnnotationProperty deathday;

    public Model ()
    {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
        model.setNsPrefix("gkraus", NS);
        model.read(MainClass.getOntologyLocation());
        actor = model.createClass(NS + "Actor");
        actor.addSuperClass(FOAF.Person);
        character = model.createClass(NS + "Character");
        character.addSuperClass(FOAF.Person);
        movie = model.createClass(NS + "Movie");
        award = model.createClass(NS + "Award");
        acting = model.createClass(NS + "Acting");
        acting.addSuperClass(award);
        supporting = model.createClass(NS + "Supporting");
        supporting.addSuperClass(acting);
        leading = model.createClass(NS + "Leading");
        leading.addSuperClass(acting);
        actedIn = model.createObjectProperty(NS + "actedIn");
        actedIn.addDomain(character);
        actedIn.addRange(movie);
        actedBy = model.createObjectProperty(NS + "actedBy");
        actedBy.addDomain(character);
        actedBy.addRange(actor);
        awardedFor = model.createClass(NS + "awardedFor");
        nominated = model.createClass(NS + "nominated");
        nominated.addSuperClass(awardedFor);
        won = model.createClass(NS + "won");
        won.addSuperClass(awardedFor);

        actorAwardedFor = model.createObjectProperty(NS + "actorAwardedFor");
        actorAwardedFor.addDomain(actor);
        actorAwardedFor.addRange(awardedFor);
        movieAwardedFor = model.createObjectProperty(NS + "movieAwardedFor");
        movieAwardedFor.addDomain(movie);
        movieAwardedFor.addRange(awardedFor);
        awardedForAward = model.createObjectProperty(NS + "awardedForAward");
        awardedForAward.addDomain(awardedFor);
        awardedForAward.addRange(award);

        tmdbid = model.createDatatypeProperty(NS + "tmdbID");
        tmdbid.addRange(XSD.xint);
        tmdbRating = model.createDatatypeProperty(NS + "tmdbRating");
        tmdbRating.addDomain(movie);
        tmdbRating.addRange(XSD.xfloat);
        tmdbVoteCount = model.createDatatypeProperty(NS + "tmdbRateCount");
        tmdbVoteCount.addDomain(movie);
        tmdbVoteCount.addRange(XSD.xint);
        tmdbPopularity = model.createDatatypeProperty(NS + "tmdbPopularity");
        tmdbPopularity.addDomain(movie);
        tmdbPopularity.addRange(XSD.xfloat);
        tmdbOrder = model.createDatatypeProperty(NS + "tmdbOrder");
        tmdbOrder.addRange(XSD.xint);

        tmdbCastID = model.createDatatypeProperty(NS + "tmdbCastID");
        tmdbCastID.addRange(XSD.xint);

        imdbid = model.createDatatypeProperty(NS + "imdbID");
        imdbid.addRange(XSD.xstring);
        imdbRating = model.createDatatypeProperty(NS + "imdbRating");
        imdbRating.addDomain(movie);
        imdbRating.addRange(XSD.xfloat);
        imdbVoteCount = model.createDatatypeProperty(NS + "imdbRateCount");
        imdbVoteCount.addDomain(movie);
        imdbVoteCount.addRange(XSD.xint);

        name = FOAF.name;
        year = model.createDatatypeProperty(NS + "year");
        year.addRange(XSD.date);

        birthday = FOAF.birthday;

        deathday = model.createAnnotationProperty(NS + "deathday");
        deathday.addRange(XSD.date);
    }

    public DatatypeProperty getTmdbCastID ()
    {
        return tmdbCastID;
    }

    public final OntModel getModel ()
    {
        return model;
    }

    public final ObjectProperty getActedIn ()
    {
        return actedIn;
    }

    public final OntClass getActing ()
    {
        return acting;
    }

    public final DatatypeProperty getYear ()
    {
        return year;
    }

    public final OntClass getSupporting ()
    {
        return supporting;
    }

    public final OntClass getLeading ()
    {
        return leading;
    }

    public final ObjectProperty getActedBy ()
    {
        return actedBy;
    }

    public final OntClass getCharacter ()
    {
        return character;
    }

    public final OntClass getAwardedFor ()
    {
        return awardedFor;
    }

    public final OntClass getNominated ()
    {
        return nominated;
    }

    public final OntClass getWon ()
    {
        return won;
    }

    public ObjectProperty getActorAwardedFor ()
    {
        return actorAwardedFor;
    }

    public ObjectProperty getMovieAwardedFor ()
    {
        return movieAwardedFor;
    }

    public ObjectProperty getAwardedForAward ()
    {
        return awardedForAward;
    }

    public final OntClass getActor ()
    {
        return actor;
    }

    public final OntClass getMovie ()
    {
        return movie;
    }

    public final OntClass getAward ()
    {
        return award;
    }

    public final OntModel getOntModel ()
    {
        return model;
    }

    public final Property getName ()
    {
        return name;
    }

    public final DatatypeProperty getTmdbid ()
    {
        return tmdbid;
    }

    public final DatatypeProperty getTmdbRating ()
    {
        return tmdbRating;
    }

    public final DatatypeProperty getTmdbVoteCount ()
    {
        return tmdbVoteCount;
    }

    public final DatatypeProperty getImdbid ()
    {
        return imdbid;
    }

    public final DatatypeProperty getImdbRating ()
    {
        return imdbRating;
    }

    public final DatatypeProperty getImdbVoteCount ()
    {
        return imdbVoteCount;
    }

    public final DatatypeProperty getTmdbPopularity ()
    {
        return tmdbPopularity;
    }

    public final DatatypeProperty getTmdbOrder ()
    {
        return tmdbOrder;
    }

    public final Property getBirthday ()
    {
        return birthday;
    }

    public final AnnotationProperty getDeathday ()
    {
        return deathday;
    }

}
