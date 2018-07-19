package twitter.raffle.services

import twitter4j.Status

class RaffleService {
    TwitterService twitterService = new TwitterService()

    List getWinners(List<Status> tweets, Integer numWinners) {
        List<Status> winners = []

        while (winners.size() < numWinners) {
            Collections.shuffle tweets
            if (!winners.user.name.contains(tweets.first().user.name)) {
                winners.add(tweets.first() as Status)
            }
        }

        return twitterService.getDataFromTweets(winners)
    }
}
