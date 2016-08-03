/*
 * Copyright (C) 2016 Florian Frankenberger.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.darkblue.log.formatters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A simple log formatter that formats log entries as one liner:
 * <pre>
 *  08:17:12                           d.a.b.SomeClass.doSth() :: some info text
 *  08:17:13                             d.a.b.SomeClass.foo() !! some severe text
 * </pre>
 * These are the level symbols:
 * <pre>
 *     .. FINEST
 *     .- FINER
 *     -- FINE
 *     :: INFO
 *     !? WARNING
 *     !! SEVERE
 * </pre>
 * @author Florian Frankenberger <f.frankenberger@darkblue.de>
 */
public class SingleLineFormatter extends Formatter {

    private static final Map<String, String> LEVEL_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {{
        this.put(Level.FINEST.getName(), "..");
        this.put(Level.FINER.getName(), ".-");
        this.put(Level.FINE.getName(), "--");
        this.put(Level.INFO.getName(), "::");
        this.put(Level.WARNING.getName(), "!?");
        this.put(Level.SEVERE.getName(), "!!");
    }});

    private final Locale locale;
    private final DateFormat dateFormatter;

    /**
     *
     * @param dateStyle the given date formatting style
     * @param timeStyle the given time formatting style
     * @param locale the locale to use
     */
    public SingleLineFormatter(int dateStyle, int timeStyle, Locale locale) {
        this.dateFormatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        this.locale = locale;
    }

    public SingleLineFormatter(String dateFormat, Locale locale) {
        this.dateFormatter = new SimpleDateFormat(dateFormat, locale);
        this.locale = locale;
    }

    @Override
    public String format(LogRecord record) {
        String signature = simplifyQualifiedClassName(record.getSourceClassName()) + "." + record.getSourceMethodName() + "()";
        String message;

        message = (record.getParameters() != null ? MessageFormat.format(record.getMessage(), record.getParameters()) : record.getMessage());
        if (message == null) {
            message = "";
            if (record.getThrown() != null) {
                message = record.getThrown().getMessage();
            }
        }

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.flush();
            message += "\n" + sw.toString();
        }

        final String level = LEVEL_MAP.get(record.getLevel().getName());
        return String.format(locale, "%s %-50s  %s %s\n", dateFormatter.format(record.getMillis()), signature, level, message);
    }

    private String simplifyQualifiedClassName(String className) {
        String[] parts = className.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            if (i > 0) {
                sb.append(".");
            }
            final String part = i == parts.length - 1 ? parts[i] : parts[i].substring(0, 1);
            sb.append(part);
        }
        return sb.toString();
    }

}
