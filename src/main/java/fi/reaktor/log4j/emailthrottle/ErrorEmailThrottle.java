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

    private static final long THROTTLE_TIME = 15 * 60 * 1000;
    private static final long THROTTLE_MODE_ON_TRESHOLD = 1 * 60 * 1000;
    private static final long THROTTLE_MODE_OFF_TRESHOLD = 4 * THROTTLE_TIME;
    private long lastTriggerTime = 0;
    private long lastEventTime = 0;
    boolean inThrottleMode = false;

    public boolean isInThrottleMode() {
        return inThrottleMode;
    }

    @Override
    public synchronized boolean isTriggeringEvent(LoggingEvent event) {
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
        return lastEventTime + THROTTLE_MODE_ON_TRESHOLD > event.timeStamp;
    }

    private boolean isThrottleTimeExceeded(LoggingEvent event) {
        if(lastTriggerTime + THROTTLE_TIME < event.timeStamp) {
            if(shouldDisableThrottle(event)) {
                inThrottleMode = false;
            }
            return triggeringEvent(event);
        }
        return notTriggeringEvent(event);
    }

    private boolean shouldDisableThrottle(LoggingEvent event) {
        return lastEventTime + THROTTLE_MODE_OFF_TRESHOLD < event.timeStamp;
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
