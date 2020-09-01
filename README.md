Simple TriggeringEventEvaluator for Log4j SMTPAppender
======================================================

Trigger throttles amount of emails so that in persistent failure situation,
[SMTPAppender] won't send thousands of separate emails but a consolidated one of all errors on configured interval.

How To Install It?
-----------------

If you're using Maven 2, just add the following dependency block into your POM file :

    <dependency>
      <groupId>fi.reaktor.log4j</groupId>
      <artifactId>log4j-email-throttle</artifactId>
      <version>1.1.0</version>
    </dependency>

  and Github packages repository:

    <repository>
      <id>github</id>
      <name>GitHub Reaktor Apache Maven Packages</name>
      <url>https://maven.pkg.github.com/reaktor/log4j-email-throttle</url>
    </repository>

..or you can just clone the source and build it yourself.

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

How Does It Work?
-----------------

[TriggeringEventEvaluator] of [SMTPAppender] is meant for deciding if a loging event should trigger sending an alert email with current buffer as context or not.
Buffer of [SMTPAppender] contains max previous buffersize log entires which are at least treshold leve.

By default with [SMTPAppender] ERROR or more severe event always triggers email sending.
This can cause thousands of emails for instance if your database or name server goes down.

When erros occur only occasionally ErrorEmailThrottle works same way as default and sends all errors directly (with context).

But if previous error was under only some time (by default 1 minute: `throttleIfUnderSecs`) ago it goes into Throttle mode .
In Throttle mode ErrorEmailThrottle triggers email sending only time to time (by default every 15 minutes: `emailIntervalInSecs`).

If no error occurs in a longer time (by default after 1 hour: `normalAfterSecs`) ErrorEmailThrottle enters back to normal mode and sends errors in buffer.

You can change default values by setting these System properties (times in seconds):

    fi.reaktor.log4j.emailthrottle.throttleIfUnderSecs=60
    fi.reaktor.log4j.emailthrottle.emailIntervalInSecs=900
    fi.reaktor.log4j.emailthrottle.normalAfterSecs=3600

Note! Check of returning to normal mode is made only when ErrorEmailThrottle receives a logging event for evaluating.
So after being in throttle mode you may receive with a new error some old erros which were buffered in previous error situation.
Especially if you have configured treshold=ERROR for SMTPAppender.


[SMTPAppender]: http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/net/SMTPAppender.html
[TriggeringEventEvaluator]: http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/spi/TriggeringEventEvaluator.html

License
-------

Copyright © 2010 original author or authors

Licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
