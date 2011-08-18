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
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class ErrorEmailThrottle implements TriggeringEventEvaluator {

    private final long throttledEmailIntervalMilliSecs;
    private final long throttleModeTriggerTimeMilliSecs;
    private final long throttleModeStopTimeMilliSecs;
    private long lastTriggerTime = 0;
    private long lastEventTime = 0;
    boolean inThrottleMode = false;

    private static Long getTimeIntervalPropertyInSeconds(String name, long defaultValueInSeconds) {
        long value = Long.getLong(ErrorEmailThrottle.class.getPackage().getName()+ "." + name,  defaultValueInSeconds);
        if(value <= 0) {
            value = defaultValueInSeconds;
        }
        return value * 1000;
    }

    public ErrorEmailThrottle() {
        this(
             getTimeIntervalPropertyInSeconds("emailInterval", 15 * 60),
             getTimeIntervalPropertyInSeconds("triggerThrottleTime", 1 * 60),
             getTimeIntervalPropertyInSeconds("stopThrottleTime", 60 * 60)
        );
    }

    public ErrorEmailThrottle(long throttledEmailIntervalMilliSecs, long throttleModeTriggerTimeMilliSecs, long throttleModeStopTimeMilliSecs) {
        this.throttledEmailIntervalMilliSecs = throttledEmailIntervalMilliSecs;
        this.throttleModeTriggerTimeMilliSecs =throttleModeTriggerTimeMilliSecs;
        this.throttleModeStopTimeMilliSecs = throttleModeStopTimeMilliSecs;
    }

    public boolean isInThrottleMode() {
        return inThrottleMode;
    }

    @Override
    public synchronized boolean isTriggeringEvent(LoggingEvent event) {
        if(isInThrottleMode() && shouldDisableThrottle(event)) {
            inThrottleMode = false;
            return triggeringEvent(event);
        }
        if(!eventLevelIsGreaterOrEqual(event, Level.ERROR)) {
            return false;
        }
        if(eventLevelIsGreaterOrEqual(event, Level.FATAL)) {
            return triggeringEvent(event);
        }
        if(isInThrottleMode()) {
            return isThrottleTimeExceeded(event);
        }
        if(shouldEnableThrottle(event)) {
            inThrottleMode = true;
            return notTriggeringEvent(event);
        }
        return triggeringEvent(event);
    }

    private boolean eventLevelIsGreaterOrEqual(LoggingEvent event, Level level) {
        return event.getLevel().isGreaterOrEqual(level);
    }

    private boolean shouldEnableThrottle(LoggingEvent event) {
        return lastEventTime + throttleModeTriggerTimeMilliSecs > event.timeStamp;
    }

    private boolean isThrottleTimeExceeded(LoggingEvent event) {
        if(lastTriggerTime + throttledEmailIntervalMilliSecs < event.timeStamp) {
            return triggeringEvent(event);
        }
        return notTriggeringEvent(event);
    }

    private boolean shouldDisableThrottle(LoggingEvent event) {
        return lastEventTime + throttleModeStopTimeMilliSecs < event.timeStamp;
    }

    private boolean triggeringEvent(LoggingEvent event) {
        lastEventTime = event.timeStamp;
        lastTriggerTime = lastEventTime;
        return true;
    }

    private boolean notTriggeringEvent(LoggingEvent event) {
        lastEventTime = event.timeStamp;
        return false;
    }

}
