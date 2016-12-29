package lxx.events

import java.util.ArrayList
import lxx.util.Logger
import robocode.Event

val robocodeEvents: (Any) -> Boolean = { it is Event }
val allEvents: (Any) -> Boolean = { true }

class EventsSource<T> {

    private val registeredSources = ArrayList<IteratorRergistration<T>>()

    fun getEventsStream(filter: (T) -> Boolean): Sequence<T> {
        val iter = EventsIterator<T>()
        registeredSources.add(IteratorRergistration(filter, iter))

        return object : Sequence<T> {
            override fun iterator(): Iterator<T> = iter
        }
    }

    fun pushEvent(event: T) {
        for ((filter, iter) in registeredSources) {
            if (filter(event)) {
                iter.events.add(event)
            }
        }
        Logger.debug({ "Event received: ${event.toString()}" })
    }

}

private class EventsIterator<T>() : Iterator<T> {

    val events = ArrayList<T>()

    override fun next() = events.removeAt(0)

    override fun hasNext() = events.size > 0

}

private data class IteratorRergistration<T>(val filter: (T) -> Boolean, val iterator: EventsIterator<T>)
