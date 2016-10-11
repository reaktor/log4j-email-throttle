Simple TriggeringEventEvaluator for Log4j SMTPAppender
======================================================

Trigger throttles amount of emails so that in persistent failure situation,
[SMTPAppender] won't send thousands of separate emails but a consolidated one of all errors on configured interval.

How To Install It?
-----------------

Clone the source and build it yourself:
  git clone repoUrl
  gradle install

Once you've built the project and installed to your local repo via the above, just add the following dependency block into your POM file (if using maven):
    <dependency>
      <groupId>fi.reaktor.log4j</groupId>
      <artifactId>log4j-email-throttle</artifactId>
      <version>2.0.1</version>
    </dependency>

Dependencies on Groovy 2.4, log4j, javax.mail-api and Java 7

How To Use It?
--------------

Configure in your SMTPAppender as usual but set ErrorEmailThrottle as EvaluatorClass

For example:

    log4j.appender.email=org.apache.log4j.net.SMTPAppender
    # WARN means that WARN messages are saves as context in the alert mail -> triggering error is the last in mail.
    log4j.appender.email.threshold=WARN
    # amount of log events to be sent as context (also max log event count in buffered emails)
    log4j.appender.email.bufferSize=50
    log4j.appender.email.EvaluatorClass=fi.reaktor.log4j.emailthrottle.ErrorEmailThrottle
    log4j.appender.email.SMTPHost=localhost
    log4j.appender.email.from=my-server@mydomain.com
    log4j.appender.email.to=error-account@@mydomain.com
    log4j.appender.email.subject=My application error alert
    log4j.appender.email.layout=org.apache.log4j.PatternLayout
    log4j.appender.email.layout.conversionPattern=%p %d{ISO8601} %-16.16t %c{1} - %m\n

Alternately, use the ScheduledSmtpAppender in place of SMTPAppender
    log4j.appender.email.EvaluatorClass=fi.reaktor.log4j.emailthrottle.ScheduledSmtpAppender

How Does It Work?
-----------------

[TriggeringEventEvaluator] of [SMTPAppender] is meant for deciding if a logging event should trigger
sending an alert email with current buffer as context or not.
Buffer of [SMTPAppender] contains max previous buffer size log entries which are at least threshold level.

By default with [SMTPAppender] ERROR or more severe event always triggers email sending.
This can cause thousands of emails for instance if your database or name server goes down.

When errors occur only occasionally ErrorEmailThrottle works same way as default and sends all errors directly (with context).

But if previous error was under only some time (by default 1 minute: `throttleIfUnderSecs`) ago it goes into Throttle mode .
In Throttle mode ErrorEmailThrottle triggers email sending only time to time (by default every 10 minutes: `emailIntervalInSecs`).

If no error occurs in a longer time (by default after 30 mins: `normalAfterSecs`) ErrorEmailThrottle
enters back to normal mode and sends errors in buffer.

You can change default values by setting these System properties (times in seconds):

    fi.reaktor.log4j.emailthrottle.throttleIfUnderSecs=60
    fi.reaktor.log4j.emailthrottle.emailIntervalInSecs=600
    fi.reaktor.log4j.emailthrottle.normalAfterSecs=1800

Note! Check of returning to normal mode is made only when ErrorEmailThrottle receives a logging event for evaluating.
So after being in throttle mode you may receive with a new error some old errors which were buffered in previous error situation.
Especially if you have configured threshold=ERROR for SMTPAppender.
You can alleviate this by using the included ScheduledSmtpAppender which will try to flush the buffer every 10 minutes.  Config shown above.


[SMTPAppender]: http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/net/SMTPAppender.html
[TriggeringEventEvaluator]: http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/spi/TriggeringEventEvaluator.html

License
-------

Copyright © 2010 original author or authors

Licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
