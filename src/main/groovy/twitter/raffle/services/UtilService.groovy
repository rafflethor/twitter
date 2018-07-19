package twitter.raffle.services

import groovy.transform.CompileStatic

@CompileStatic
class UtilService {
    static final String DATE_FORMAT = "EEE, MMMM dd yyyy, 'at' hh:mm"
    static final Locale LOCALE = Locale.ENGLISH

    // TODO: Fix TimeZone using utcOffset
    static Date fixTimeZone(Date date) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        cal.add(Calendar.HOUR, -9)
        Date fixedTimeZone = cal.getTime()

        return fixedTimeZone
    }
}
