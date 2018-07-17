package twitter.raffle.controllers

import groovy.transform.CompileStatic
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.Client
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer

import javax.inject.Inject

@CompileStatic
@Controller("/raffle")
class RaffleController {

    @Get("/")
    HttpResponse<Map> newRaffle(HttpRequest<?> request) {

        String numWinners = request.getParameters()
            .getFirst("numWinners")
            .orElse("Nobody")
        println "===> " + getUsersByHashTag("FelizLunes")

        return HttpResponse.ok([numWinners: numWinners] as Map)
            .header("X-My-Header", "Foo")
    }

    String getUsersByHashTag(String hashtag) {
        HttpClient twitterClient = HttpClient.create(new URL("https://twitter.com"))

//        @Client('https://twitter.com') RxHttpClient twitterClient
        HttpRequest<String> twitterRequest = HttpRequest.GET("https://twitter.com")
        String result = twitterClient.toBlocking().retrieve("/hashtag/$hashtag")

        return result
    }
}