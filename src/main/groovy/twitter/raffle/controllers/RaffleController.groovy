package twitter.raffle.controllers

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import twitter.raffle.services.RaffleService
import twitter.raffle.services.TwitterService
import twitter4j.Status

/**
    Connect to the twitter API to return tweets by hashtag accepting filters
    Sample query: raffle?hashtag=piweek14&since=2018-07-16&until=2018-07-18&numWinners=4
 */
@CompileStatic
@Controller("/raffle")
class RaffleController {
    TwitterService twitterService = new TwitterService()
    RaffleService raffleService = new RaffleService()

    @Get("/")
    @Requires(classes=TwitterService)
    HttpResponse<Map> newRaffle(HttpRequest<?> request) {
        Map<String, ?> params = getParams(request)

        List<Status> twitterResponse = twitterService.getUsersByHashtag(
            params.hashtag.toString(),
            params.until.toString(),
            params.since.toString()
        )

        Map<String, List> response = [ winners: [], allTweeters:  [], allTweets: [] ]
        if (twitterResponse) {
            List winners = raffleService.getWinners(twitterResponse, params.numWinners as Integer)
            Collections.shuffle(winners)
            response.winners = winners
            response.allTweeters = twitterService.getTweetersFromTweets(twitterResponse)
            response.allTweets = twitterService.getDataFromTweets(twitterResponse)
        }

        return HttpResponse.ok(response as Map)
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
}