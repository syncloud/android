package org.syncloud.android.log;

import com.google.common.base.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    private final Pattern pattern;

    public LogParser() {
        String timestamp = "(\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d)";
        String level = "(.)";
        String pid = "(\\d+)";
        String message = "(.*)";
        String tag = "(.*)";
        String regex = timestamp + " " + level + "/" + tag + "\\( *" + pid + "\\): " + message;
        pattern = Pattern.compile(regex);
    }

    public Optional<LogEvent> parse(String log) {

        Matcher matcher = pattern.matcher(log);

        LogEvent result = null;
        if (matcher.find()) {
            result = new LogEvent(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    Integer.parseInt(matcher.group(4)),
                    matcher.group(5)
            );
        }

        return Optional.fromNullable(result);
    }
}
