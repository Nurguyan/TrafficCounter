package com.company.database

import org.junit.Test
import org.testng.Assert
import java.util.concurrent.Callable

class DBChangeListenerTest extends DataBaseTest {

    @Test
    void testCreateListenerTest() {
        boolean listener_works = false

        DBChangeListener listener = new DBChangeListener()
        Callable<Void> func = () -> {
            listener_works = true
            return null
        };
        listener.createListener(func);

        testGetMinLimit()

        sleep(5000)
        Assert.assertTrue(listener_works)
    }
}
