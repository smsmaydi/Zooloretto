import java.text.SimpleDateFormat
import java.util.*


class Logger(val name: String) {
    fun log(message: String) {
        val pattern = "MM-dd-yyyy HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = simpleDateFormat.format(Date())
        println("[$date] [$name] $message")
    }
}