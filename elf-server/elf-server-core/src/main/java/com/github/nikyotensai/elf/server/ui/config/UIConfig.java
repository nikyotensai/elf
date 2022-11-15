package com.github.nikyotensai.elf.server.ui.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class UIConfig {

    static public Map<String, String> configMap = new HashMap() {{
        put("jar.guarantee.period.days", "2");
        put("admins", "admin");
        put("agent.proxy", "http://%s:%d/proxy/agent/get");
        put("agent.meta.refresh", "http://%s:%d/proxy/agent/metaRefresh");
        put("file.path.format", "{0}src/main/java/{1}.java");
        put("agent.lastVersion", "1.0");
    }};

    static public Map<String, String> url_redirectMap = new HashMap() {{
        put("monitor.help.url", "https://github.com/qunarcorp/elf/blob/master/docs/cn/monitor.md");
        put("debug.help.url", "https://github.com/qunarcorp/elf/blob/master/docs/cn/debug.md");
        put("jstack.help.url", "https://github.com/qunarcorp/elf/blob/master/docs/cn/jstack.md");
        put("jmap.help.url", "https://github.com/qunarcorp/elf/blob/master/docs/cn/jmap.md");
    }};

    static public Map<String, String> releaseInfo_configMap = new HashMap() {{
        put("default", "/tmp/elf/releaseInfo.properties");
    }};

    static public Map<String, String> serverMap = new HashMap() {{
        put("tomcat.port", "9021");
        put("tomcat.basedir", "/tmp/elf/tomcat/ui");
    }};

//    cat config.properties  | grep -v "#" | awk -F '=' '{print "put(\""$1"\", \""$2"\");"}'
}
