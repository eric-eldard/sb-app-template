package com.your_namespace.your_app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils
{
    public static String as_yyyyMMddhhmmss(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
    }

    private DateUtils()
    {
        // util ctor
    }
}