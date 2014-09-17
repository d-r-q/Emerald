package lxx.events

import java.util.ArrayList
import lxx.util.Logger
import robocode.Event

public val robocodeEvents: (Any) -> Boolean = { it is Event }

class EventsSource {

    private val registeredSources = ArrayList<IteratorRergistration>()

    fun getEventsStream(filter: (Any) -> Boolean): Stream<Any> {
        val iter = EventsIterator()
        registeredSources.add(IteratorRergistration(filter, iter))

        return object : Stream<Any> {
            override public fun iterator(): Iterator<Any> = iter
        }
    }

    fun pushEvent(event: Any) {
        for ((filter, iter) in registeredSources) {
            if (filter(event)) {
                iter.events.add(event)
            }
        }
        Logger.debug({ "Event received: ${event.toString()}" })
    }

}

private class EventsIterator() : Iterator<Any> {

    val events = ArrayList<Any>()

    override fun next() = events.remove(0)

    override fun hasNext() = events.size > 0

}

private data class IteratorRergistration(val filter: (Any) -> Boolean, val iterator: EventsIterator)
