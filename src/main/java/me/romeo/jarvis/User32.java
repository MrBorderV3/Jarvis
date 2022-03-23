package me.romeo.jarvis;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

import java.util.concurrent.atomic.AtomicReference;

public interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.load("user32", User32.class);
    boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
    int GetWindowTextA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);

    default WinDef.HWND findWindow(String partialWindowName){
        AtomicReference<WinDef.HWND> hwnd = new AtomicReference<>();
        INSTANCE.EnumWindows((hWnd, arg1) -> {
            byte[] windowText = new byte[512];
            INSTANCE.GetWindowTextA(hWnd, windowText, 512);

            String wText = Native.toString(windowText);

            if (wText.isEmpty()) {
                return true;
            }

            if (wText.contains(partialWindowName)){
                hwnd.set(hWnd);
                return false;
            }

            return true;
        }, null);

        return hwnd.get();
    }
}