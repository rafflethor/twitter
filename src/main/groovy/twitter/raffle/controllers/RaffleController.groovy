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
import twitter4j.TwitterFactory

import javax.inject.Inject

@CompileStatic
@Controller("/raffle")
class RaffleController {
    @Client("https://twitter.com")
    @Inject
    RxHttpClient httpClient

    @Get("/")
    HttpResponse<Map> newRaffle(HttpRequest<?> request) {

        Integer numWinners = request.getParameters()
            .getFirst("numWinners")
            .orElse("0")
            .toInteger()

        String hashtag = request.getParameters()
            .getFirst("hashtag")
            .orElse("noHashTag")

        String tweets = getUsersByHashTag("#$hashtag", numWinners)
        println "==> $hashtag"
        return HttpResponse.ok([numWinners: numWinners, tweets: tweets] as Map)
            .header("X-My-Header", "Foo")
    }

    String getUsersByHashTag(String hashtag, Integer numWinners) {
        twitter4j.Twitter twitter = TwitterFactory.getSingleton()

        Query query = new Query(hashtag)
        query.count(100) //100 is the max allowed
        QueryResult result = twitter.search(query)

        return result.tweets.get(numWinners)
    }

    /**
     * Percentage encoding
     *
     * @return A encoded string
     */
    private String encode(String value) {
        String encoded = ""
        try {
            encoded = URLEncoder.encode(value, "UTF-8")
        } catch (Exception e) {
            e.printStackTrace()
        }
        String sb = ""
        char focus
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i)
            if (focus == '*') {
                sb += "%2A"
            } else if (focus == '+') {
                sb += "%20"
            } else if (focus == '%' && i + 1 < encoded.length()
                && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                sb += '~'
                i += 2
            } else {
                sb += focus
            }
        }
        return sb.toString()
    }
}