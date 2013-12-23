package lxx.events

import java.util.ArrayList
import lxx.util.Logger

class Log {

    private val avgEventsPerRound = 15000

    private val events = ArrayList<Any>(avgEventsPerRound)

    fun getEventsSource(filter: (Any) -> Boolean) = object : Iterator<Any> {

        private var idx = events.size

        override fun next() = events.get(idx++)

        override fun hasNext(): Boolean {
            while (idx < events.size) {
                if (filter(events.get(idx))) {
                    return true
                } else {
                    idx++
                }
            }
            return false
        }

    }

    fun pushEvent(event: Any) {
        events.add(event)
        Logger.debug({"Event received: ${event.toString()}"})
    }

}