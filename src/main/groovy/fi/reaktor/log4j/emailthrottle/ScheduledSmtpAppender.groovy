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

import groovy.transform.CompileStatic
import org.apache.log4j.net.SMTPAppender

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@CompileStatic
/**
 * Extension of the SMTPAppender that will periodically (default of 10 mins) send the buffer
 *
 * Configure in your Log4j settings file with:
 * log4j.appender.email=fi.reaktor.log4j.emailthrottle.ScheduledSmtpAppender
 *
 * All other settings are applicable as for SMTPAppender
 *
 * Necessary with the ErrorEmailThrottle, when timeliness of the exceptions is important.
 */
class ScheduledSmtpAppender extends SMTPAppender {

    long minIntervalInSeconds = 10 * 60

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)

    ScheduledSmtpAppender(){
        super()
        sendAtMinInterval()
    }

    /**
     * Check every so many minutes to see if we should send in case another event hasn't come in since.
     */
    void sendAtMinInterval() {
        Runnable emailSender = {
            if(cb.length()){
                sendBuffer()
            }
        } as Runnable

        scheduler.scheduleAtFixedRate(emailSender, (minIntervalInSeconds/2).toLong(), minIntervalInSeconds, TimeUnit.SECONDS)
    }

}
