package com.your_namespace.your_app.util.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class YourMessageForegroundConverter extends ForegroundCompositeConverterBase<ILoggingEvent>
{
    @Override
    protected String getForegroundColorCode(ILoggingEvent event)
    {
        // TODO - set the package name for which you want log messages highlighted
        if (event.getLoggerName().contains("com.your_namespace"))
        {
            return ANSIConstants.DEFAULT_FG;
        }
        return ANSIConstants.WHITE_FG;
    }
}