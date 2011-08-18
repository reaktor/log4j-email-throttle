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

import jdave.Specification;
import jdave.junit4.JDaveRunner;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class ErrorEmailThrottleSpec extends Specification<ErrorEmailThrottle> {
    public class EmailThrottleWithDefaultIntervals {

        public ErrorEmailThrottle create() {
            return new ErrorEmailThrottle();
        }

        public void doesNotThrottleFatalErrors() {
            specify(context.isTriggeringEvent(createLogEvent(Level.FATAL, System.currentTimeMillis())), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
        }

        public void doesNotThrottleFirstErrorEvent() {
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, System.currentTimeMillis())), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
        }

        public void lowerThanErrorLevelEventsAreAreNotTriggering() {
            specify(context.isTriggeringEvent(createLogEvent(Level.INFO, System.currentTimeMillis())), should.equal(false));
        }

        public void throttlesSecondAndThirdErrorIfSecondIsUnderOneMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 2 * 60 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
        }

        public void throttleModeIsDisabledAndEmailSentIfThrottleTimeIsExceededEvenIfLastEventIsNotErrorLevel() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 61 * 60 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
            specify(context.isTriggeringEvent(createLogEvent(Level.WARN, time3)), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
        }

        public void doesNotThrottleSecondErrorIfItIsOverOneMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 2 * 60 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(true));
        }

        public void disablesThrottleModeIfThirdErrorIsMoreThanAnHourApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 90 * 60 * 1000;
            long time4 = time3 + 2 * 60 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time4)), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
        }

    }
    public class EmailThrottleWithCustomIntervals {

        public ErrorEmailThrottle create() {
            return new ErrorEmailThrottle(15 * 1000, 60 * 60 * 1000, 60 * 1000);
        }

        public void throttlesSecondAndThirdErrorIfSecondIsUnder15SecondsApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 45 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
        }

        public void doesNotThrottleSecondErrorIfItIsOver15SecondsApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 45 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(true));
        }

        public void disablesThrottleModeIfThirdErrorIsMoreThanAMinuteApart() {
            long time1 = System.currentTimeMillis();
            long time2 = time1 + 500;
            long time3 = time2 + 15 * 60 * 1000;
            long time4 = time3 + 2 * 60 * 1000;
            context.isTriggeringEvent(createLogEvent(Level.ERROR, time1));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time2)), should.equal(false));
            specify(context.isInThrottleMode(), should.equal(true));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time3)), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
            specify(context.isTriggeringEvent(createLogEvent(Level.ERROR, time4)), should.equal(true));
            specify(context.isInThrottleMode(), should.equal(false));
        }

    }

    private LoggingEvent createLogEvent(Level level, long time) {
        return new LoggingEvent("test", new RootLogger(Level.INFO) , time, level, "test msg", null);
    }
}
