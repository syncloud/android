package org.syncloud.apps.sam;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public enum Command {

    Install, Verify, Upgrade, Update, Remove, List, Upgrade_All;

    public String cmd(String... arguments) {
        java.util.List<String> cmd = new ArrayList<String>(asList("sam", name().toLowerCase()));
        cmd.addAll(asList(arguments));
        return join(cmd, " ");
    }
}
