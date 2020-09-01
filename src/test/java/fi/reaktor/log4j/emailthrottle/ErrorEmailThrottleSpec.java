/*
* Copyright 2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fi.reaktor.log4j.emailthrottle;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorEmailThrottleSpec {

    protected ErrorEmailThrottle throttle;

    @Nested
    public class EmailThrottleWithDefaultIntervals {

        @BeforeEach
        void createInstance() {
            throttle = new ErrorEmailThrottle();
        }

        @Test
        public void doesNotThrottleFatalErrors() {
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.FATAL, System.currentTimeMillis())));
            assertFalse(throttle.isInThrottleMode());
        }

        @Test
        public void doesNotThrottleFirstErrorEvent() {
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, System.currentTimeMillis())));
            assertFalse(throttle.isInThrottleMode());
        }

        @Test
        public void lowerThanErrorLevelEventsAreAreNotTriggering() {
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.INFO, System.currentTimeMillis())));
        }

        @Test
        public void throttlesSecondAndThirdErrorIfSecondIsUnderOneMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 2 * 60 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
            assertTrue(throttle.isInThrottleMode());
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time3)));
            assertTrue(throttle.isInThrottleMode());
        }

        @Test
        public void throttleModeIsDisabledAndEmailSentIfThrottleTimeIsExceededEvenIfLastEventIsNotErrorLevel() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 61 * 60 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
            assertTrue(throttle.isInThrottleMode());
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time3)));
            assertFalse(throttle.isInThrottleMode());
        }

        @Test
        public void doesNotThrottleSecondErrorIfItIsOverOneMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 2 * 60 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
        }

        @Test
        public void disablesThrottleModeIfThirdErrorIsMoreThanAnHourApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 90 * 60 * 1000;
            long time4 = time3 + 2 * 60 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
            assertTrue(throttle.isInThrottleMode());
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time3)));
            assertFalse(throttle.isInThrottleMode());
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time4)));
            assertFalse(throttle.isInThrottleMode());
        }

    }

    @Nested
    public class EmailThrottleWithCustomIntervals {

        @BeforeEach
        void createInstance() {
            throttle = new ErrorEmailThrottle(15 * 1000, 60 * 60 * 1000, 60 * 1000);
        }

        public void throttlesSecondAndThirdErrorIfSecondIsUnder15SecondsApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 45 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
            assertTrue(throttle.isInThrottleMode());
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time3)));
            assertTrue(throttle.isInThrottleMode());
        }

        public void doesNotThrottleSecondErrorIfItIsOver15SecondsApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 45 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
        }

        public void disablesThrottleModeIfThirdErrorIsMoreThanAMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 15 * 60 * 1000;
            long time4 = time3 + 2 * 60 * 1000;
            throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            assertFalse(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time2)));
            assertTrue(throttle.isInThrottleMode());
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time3)));
            assertFalse(throttle.isInThrottleMode());
            assertTrue(throttle.isTriggeringEvent(createLogEvent(Level.ERROR, time4)));
            assertFalse(throttle.isInThrottleMode());
        }
    }

    private LoggingEvent createLogEvent(Level level, long time) {
        return new LoggingEvent("test", new RootLogger(Level.INFO) , time, level, "test msg", null);
    }
}
