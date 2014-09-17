package lxx.events

import org.junit.Test
import org.junit.Assert

public class EventsSourceTest {

    [Test]
    fun logEventSourceShouldBeTraversableMultipleTimes() {
        val log = EventsSource()
        val source = log.getEventsStream { true }

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
        val log = EventsSource()
        log.pushEvent(1)
        val source = log.getEventsStream { true }
        Assert.assertTrue("Past event sholud not be found", source.none())
    }

}