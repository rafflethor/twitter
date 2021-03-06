package twitter.raffle.services

import groovy.transform.CompileStatic
import twitter4j.Status

@CompileStatic
class RaffleService {
    TwitterService twitterService = new TwitterService()

    List getWinners(List<Status> tweets, Integer numWinners = 3) {
        List<Status> winners = []

        Integer maxWinners = twitterService.getTweetersFromTweets(tweets).size()
        if (numWinners > maxWinners) {
            numWinners = maxWinners
        }

        while (winners.size() < numWinners) {
            Collections.shuffle tweets
            if (!winners.user.name.contains(tweets.first().user.name)) {
                winners.add(tweets.first() as Status)
            }
        }

        return twitterService.getDataFromTweets(winners)
    }
}
