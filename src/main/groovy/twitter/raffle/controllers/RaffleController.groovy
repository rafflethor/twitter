package twitter.raffle.controllers

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import twitter4j.*

import java.text.SimpleDateFormat

/**
    Connect to the twitter API to return tweets by hashtag accepting filters
    Sample query: raffle?hashtag=piweek14&since=2018-07-16&until=2018-07-18&numWinners=4
 */
@CompileStatic
@Controller("/raffle")
class RaffleController {
    private static final String DATE_FORMAT = "EEE, MMMM dd yyyy, 'at' hh:mm"
    private static final Locale LOCALE = Locale.ENGLISH

    @Get("/")
    HttpResponse<Map> newRaffle(HttpRequest<?> request) {
        Map<String, ?> params = getParams(request)

        List<Status> twitterResponse = getUsersByHashtag(
            params.hashtag.toString(),
            params.until.toString(),
            params.since.toString()
        )

        Map<String, List> response = [ winners: [], allTweeters:  [], allTweets: [] ]
        if (twitterResponse) {
            List winners = getWinners(twitterResponse, params.numWinners as Integer)
            Collections.shuffle(winners)
            response.winners = winners
            response.allTweeters = getTweeters(twitterResponse)
            response.allTweets = getTweets(twitterResponse)
        }

        return HttpResponse.ok(response as Map)
    }

    List<Status> getUsersByHashtag(String hashtag, String until, String since) {
        twitter4j.Twitter twitter = TwitterFactory.getSingleton()

        Query query = getQuery(hashtag, until, since)
        println "===> ${query.toString()}"

        QueryResult twitterResponse
        try {
            twitterResponse = twitter.search(query)
        } catch (TwitterException e) {
            println "Couldn't connect to " + e.message
        }

        return twitterResponse.tweets
    }

    private Query getQuery(String hashtag, String until, String since) {
        Query query = new Query("#$hashtag -filter:retweets")
        query.until(until) // Returns tweets created BEFORE the given date. 2018-06-07
        query.since(since) // Returns tweets newer than the given date. 2018-06-05
        query.resultType(Query.ResultType.recent)
        query.sinceId(1)

        return query
    }

    private List getTweets(List<Status> tweets) {
        List result = tweets.collect {[
            name : it.user.name,
            nickname: it.user.screenName,
            avatar: it.user.biggerProfileImageURL,
            tweet: it.text,
            date : new SimpleDateFormat(DATE_FORMAT, LOCALE)
                .format(fixTimeZone(it.createdAt))
        ]}
        .sort { it.date }
        .reverse()

        return result
    }

    private List<String> getTweeters(List<Status> tweets) {
        return tweets.collect { it.user.name }.unique().sort()
    }

    private List getWinners(List<Status> tweets, Integer numWinners) {
        List<Status> winners = []

        while (winners.size() < numWinners) {
            Collections.shuffle tweets
            if (!winners.user.name.contains(tweets.first().user.name)) {
                winners.add(tweets.first() as Status)
            }
        }

        return getTweets(winners)
    }

    private Map getParams(HttpRequest<?> request) {
        Integer numWinners = request.getParameters()
            .getFirst("numWinners")
            .orElse("5")
            .toInteger()

        String hashtag = request.getParameters()
            .getFirst("hashtag")
            .orElse("noHashTag")

        String since = request.getParameters()
            .getFirst("since")
            .orElse("")

        String until = request.getParameters()
            .getFirst("until")
            .orElse("")

        return [hashtag   : hashtag,
                numWinners: numWinners,
                since     : since,
                until     : until
        ]
    }

    // TODO: Fix TimeZone using utcOffset
    private Date fixTimeZone(Date date) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.HOUR, -9)
        Date fixedTimeZone = cal.getTime()

        return fixedTimeZone
    }
}