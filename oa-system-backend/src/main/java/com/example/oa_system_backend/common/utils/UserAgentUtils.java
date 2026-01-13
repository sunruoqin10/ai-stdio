package com.example.oa_system_backend.common.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Component;

@Component
public class UserAgentUtils {

    public DeviceInfo parseUserAgent(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return new DeviceInfo("Unknown", "Unknown", "Unknown");
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
        Browser browser = userAgent.getBrowser();
        OperatingSystem os = userAgent.getOperatingSystem();

        String browserName = browser != null ? browser.getName() : "Unknown";
        String osName = os != null ? os.getName() : "Unknown";
        String deviceType = os != null ? os.getDeviceType().getName() : "Unknown";

        return new DeviceInfo(browserName, osName, deviceType);
    }
}
