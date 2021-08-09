/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.krlvm.powertunnel.system.windows;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.system.SystemProxy;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.SystemUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;

public class WindowsProxyHandler implements SystemProxy.SystemProxyHandler {

    public static boolean USE_WINDOWS_NATIVE_API = !SystemUtility.OLD_OS;

    @Override
    public void enableProxy() {
        executeAndApply("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 1 /f",
                "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d " + PowerTunnel.SERVER_IP_ADDRESS + ":" + PowerTunnel.SERVER_PORT + " /f");
    }

    @Override
    public void disableProxy() {
        executeAndApply("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 0 /f");
    }

    public static void executeAndApply(final String... commands) {
        new Thread(() -> {
            try {
                for (String command : commands) {
                    SystemUtility.executeWindowsCommand(command);
                }
                if (USE_WINDOWS_NATIVE_API) {
                    Wininet wininet = Wininet.INSTANCE;
                    wininet.InternetSetOptionW(0,
                            wininet.INTERNET_OPTION_SETTINGS_CHANGED, null, 0);
                    wininet.InternetSetOptionW(0,
                            wininet.INTERNET_OPTION_REFRESH, null, 0);
                } else {
                    //we need to start Internet Explorer for apply these changes
                    forceUpdateProxySettings();
                }
                Utility.print("[*] System proxy setup has finished");
            } catch (IOException ex) {
                Utility.print("[x] Failed to setup system proxy: " + ex.getMessage());
                Debugger.debug(ex);
            }
        }).start();
    }

    public static void forceUpdateProxySettings() throws IOException {
        SystemUtility.executeWindowsCommand("start iexplore && ping -n 5 127.0.0.1 > NUL && taskkill /f /im iexplore.exe");
    }
}
