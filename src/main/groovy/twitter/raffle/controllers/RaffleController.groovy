package twitter.raffle.controllers

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.Client
import io.micronaut.http.client.RxHttpClient
import twitter4j.Query
import twitter4j.QueryResult
import twitter4j.TwitterException
import twitter4j.TwitterFactory

import javax.inject.Inject

@CompileStatic
@Controller("/raffle")
/*
    Sample query: raffle?hashtag=piweek14&since=2018-07-16&until=2018-07-17
 */
class RaffleController {
    @Client("https://twitter.com")
    @Inject
    RxHttpClient httpClient

    @Get("/")
    HttpResponse<Map> newRaffle(HttpRequest<?> request) {

        Map<String, ?> params = getParams(request)
        String twitterResponse = getUsersByHashtag(
            params.hashtag.toString(),
            params.until.toString(),
            params.since.toString()
        )

        return HttpResponse.ok([twitterResponse: twitterResponse] as Map)
            .header("X-My-Header", "Foo")
    }

    Map getUsersByHashtag(String hashtag, String until, String since) {
        twitter4j.Twitter twitter = TwitterFactory.getSingleton()

        Query query = getQuery(hashtag, until, since)
        println "===> ${query.toString()}"

        QueryResult twitterResponse
        try {
            twitterResponse = twitter.search(query)
        } catch (TwitterException e) {
            println "Couldn't connect: " + e
            return [error: "Couldn't connect to " + e.message]
        }

        Map response = [
            tweeters: twitterResponse.tweets.collect { it.user.name }.unique().sort(),
            tweets  : twitterResponse.tweets.collect {[
                    user : it.user.screenName,
                    tweet: it.text,
                    date : fixTimeZone(it.createdAt)
                ]}
                .sort { it.date }
                .reverse()
        ]

        return response
    }

    private Query getQuery(String hashtag, String until, String since) {
        Query query = new Query("#$hashtag -filter:retweets")
        query.until(until) // Returns tweets created BEFORE the given date. 2018-06-07
        query.since(since) // Returns tweets newer than the given date. 2018-06-05
        query.resultType(Query.ResultType.recent)
        query.sinceId(1)

        return query
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