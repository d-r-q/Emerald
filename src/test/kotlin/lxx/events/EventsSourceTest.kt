package lxx.events

import org.junit.Test
import org.junit.Assert

public class EventsSourceTest {

    [Test]
    fun logEventSourceShouldBeTraversableMultipleTimes() {
        val log = EventsSource<Int>()
        val source = log.getEventsStream(allEvents)

        log.pushEvent(1)
        var found = false
        for (e in source) {
            found = true
        }
        Assert.assertTrue("First pushed event sholud be found", found)

        log.pushEvent(2)
        found = false
        for (e in source) {
            found = true
        }
        Assert.assertTrue("Second pushed event sholud be found", found)
    }

    [Test]
    fun pastEventsShouldBeLost() {
        val log = EventsSource<Int>()
        log.pushEvent(1)
        val source = log.getEventsStream(allEvents)
        Assert.assertTrue("Past event sholud not be found", source.none())
    }

}