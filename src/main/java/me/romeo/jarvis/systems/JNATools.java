package me.romeo.jarvis.systems;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import me.coley.simplejna.Windows;

public class JNATools {

    /**
     * Switch the matching window between the monitors retaining size and position except in the other monitor.
     * This currently only works with two monitors and the second monitor is considered to be positive position values and not negative.
     *
     * @param hwnd The {@link com.sun.jna.platform.win32.WinDef.HWND} of the window that is to be moved between the monitors
     * @return A number representing the monitor it has been moved to `1` being the primary monitor and `2` being the secondary one.
     */
    public static int switchMonitor(WinDef.HWND hwnd){
        WinUser.WINDOWINFO info = Windows.getInfo(hwnd);
        boolean add = JNATools.isPrimaryMonitor(hwnd);

        int x = info.rcWindow.left;

        Windows.setWindowPos(hwnd, add ? x + 1920 : x - 1920, info.rcWindow.top, info.cxWindowBorders, info.cyWindowBorders, 0x0001);

        return add ? 2 : 1;
    }

    /**
     * Check whether a window is on the primary monitor
     *
     * @param hwnd The {@link com.sun.jna.platform.win32.WinDef.HWND} of the window.
     * @return Whether its on the primary monitor or not {@code true} being it is and {@code false} being it's not.
     */
    public static boolean isPrimaryMonitor(WinDef.HWND hwnd){
        WinUser.HMONITOR hmonitor = com.sun.jna.platform.win32.User32.INSTANCE.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        WinUser.MONITORINFO monitorinfo = new WinUser.MONITORINFO();
        com.sun.jna.platform.win32.User32.INSTANCE.GetMonitorInfo(hmonitor, monitorinfo);

        return monitorinfo.dwFlags == WinUser.MONITORINFOF_PRIMARY;
    }
}
