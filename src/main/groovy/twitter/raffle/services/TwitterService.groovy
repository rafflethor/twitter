package twitter.raffle.services

import groovy.transform.CompileStatic
import twitter4j.*

import java.text.SimpleDateFormat

@CompileStatic
class TwitterService {

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
        return []
    }

    private Query getQuery(String hashtag, String until, String since) {
        Query query = new Query("#$hashtag -filter:retweets")
        query.until(until) // Returns tweets created BEFORE the given date. 2018-06-07
        query.since(since) // Returns tweets newer than the given date. 2018-06-05
        query.resultType(Query.ResultType.recent)
        query.count(100)
        query.sinceId(1)

        return query
    }

    List getDataFromTweets(List<Status> tweets) {
        List result = tweets.collect {[
            name : it.user.name,
            nickname: it.user.screenName,
            avatar: it.user.biggerProfileImageURL,
            tweet: it.text,
            date : new SimpleDateFormat(UtilService.DATE_FORMAT, UtilService.LOCALE)
                .format(UtilService.fixTimeZone(it.createdAt))
        ]}
        .sort { it.date }
        .reverse()

        return result
    }

    List<String> getTweetersFromTweets(List<Status> tweets) {
        return tweets.collect { it.user.name }.unique().sort()
    }
}
