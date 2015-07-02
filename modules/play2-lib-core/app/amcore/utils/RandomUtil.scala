package amcore.utils

import org.joda.time.DateTime
import play.api.Logger

object RandomUtil {
  import scala.util.Random

  lazy val rnd = new Random

  def range(start:Int, end:Int):Int = start + rnd.nextInt( (end - start) + 1)


  def jodaDateFromDaysAgo(days:Int):DateTime = {
    val currentDate = DateTime.now
    val minutesAgo = (days * 24 * 60)

    val randomTimeInMinues = rnd.nextInt(minutesAgo)

    val randomDate = currentDate.minusMinutes(randomTimeInMinues)

    Logger.info(s"currentDate: $currentDate randomDate Generated : $randomDate")
    randomDate
  }

}
