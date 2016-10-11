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
package fi.reaktor.log4j.emailthrottle

import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.spi.RootLogger
import org.junit.Test

class ErrorEmailThrottleSpec {

    @Test
    void doesNotThrottleFatalErrors() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        assert(context.isTriggeringEvent(createLogEvent(Level.FATAL, System.currentTimeMillis())))
        assert(!context.isInThrottleMode())
    }

    @Test
    void doesNotThrottleFirstErrorEvent() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, System.currentTimeMillis())))
        assert(!context.isInThrottleMode())
    }

    @Test
    void lowerThanErrorLevelEventsAreAreNotTriggering() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        assert(!context.isTriggeringEvent(createLogEvent(Level.INFO, System.currentTimeMillis())))
    }

    @Test
    void throttlesSecondAndThirdErrorIfSecondIsUnderOneMinuteApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 500
        long time3 = time2 + 2 * 60 * 1000

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
        assert(context.isInThrottleMode())
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)))
        assert(context.isInThrottleMode())
    }

    @Test
    void throttleModeIsDisabledAndEmailSentIfThrottleTimeIsExceededEvenIfLastEventIsNotErrorLevel() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 500
        long time3 = time2 + 61 * 60 * 1000

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
        assert(context.isInThrottleMode())
        assert(context.isTriggeringEvent(createLogEvent(Level.WARN, time3)))
        assert(!context.isInThrottleMode())
    }

    @Test
    void doesNotThrottleSecondErrorIfItIsOverOneMinuteApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 2 * 60 * 1000

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
    }

    @Test
    void disablesThrottleModeIfThirdErrorIsMoreThanAnHourApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle()
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 500
        long time3 = time2 + 90 * 60 * 1000
        long time4 = time3 + 2 * 60 * 1000

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
        assert(context.isInThrottleMode())
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)))
        assert(!context.isInThrottleMode())
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time4)))
        assert(!context.isInThrottleMode())
    }

    @Test
    void throttlesSecondAndThirdErrorIfSecondIsUnder15SecondsApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle(timeInMillis(15) , timeInMillis(60 * 60), timeInMillis(60))
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 500
        long time3 = time2 + timeInMillis(45)

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
        assert(context.isInThrottleMode())
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)))
        assert(context.isInThrottleMode())
    }

    @Test
    void doesNotThrottleSecondErrorIfItIsOver15SecondsApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle(timeInMillis(15) , timeInMillis(60 * 60), timeInMillis(60))
        long time1 = System.currentTimeMillis()
        long time2 = time1 + timeInMillis(45)

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
    }

    @Test
    void disablesThrottleModeIfThirdErrorIsMoreThanAMinuteApart() {
        ErrorEmailThrottle context = new ErrorEmailThrottle(timeInMillis(15) , timeInMillis(60 * 60), timeInMillis(60))
        long time1 = System.currentTimeMillis()
        long time2 = time1 + 500
        long time3 = time2 + timeInMillis(15 * 60)
        long time4 = time3 + timeInMillis(2 * 60)

        context.isTriggeringEvent(createLogEvent(Level.ERROR, time1))
        assert(!context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)))
        assert(context.isInThrottleMode())
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)))
        assert(!context.isInThrottleMode())
        assert(context.isTriggeringEvent(createLogEvent(Level.ERROR, time4)))
        assert(!context.isInThrottleMode())
    }

    private LoggingEvent createLogEvent(Level level, long time) {
        return new LoggingEvent("test", new RootLogger(Level.INFO) , time, level, "test msg", null)
    }

    private Long timeInMillis(long seconds){
        return seconds * 1000
    }
}
