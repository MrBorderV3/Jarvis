package me.romeo.jarvis.systems;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import me.coley.simplejna.Windows;
import me.romeo.jarvis.Main;
import me.romeo.jarvis.User32;

import javax.sound.sampled.LineEvent;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static me.romeo.jarvis.utils.Applications.*;

public class Jarvis {

    protected static final File dir = new File(System.getProperty("user.home") + File.separator + "Jarvis");

    private final Configuration configuration;
    private final PlaybackSystem playbackSystem;

    private final JarvisSettings settings = new JarvisSettings("settings", dir);

    public Jarvis(Configuration configuration) {
        this.configuration = configuration;
        this.playbackSystem = new PlaybackSystem();
        if (!dir.exists())
            dir.mkdirs();
    }

    /**
     * Used to play the startup sound and start up Jarvis or if sound is disabled simply start it up.
     */
    public void setup() {
        if (settings.getSound()) {
            playbackSystem.playSound("intro", (event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    Main.getMainThreadQueue().add(() -> {
                        try {
                            start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }));

        } else {
            try {
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        if (settings.getSound()) {
            playbackSystem.playSound("shutdown", event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    System.exit(0);
                }
            });
        } else {
            System.exit(0);
        }
    }

    private void start() throws IOException {
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
        recognizer.startRecognition(true);

        while (true) {
            String utterance = recognizer.getResult().getHypothesis();

            System.out.println("**Utterance**: " + utterance);
            if (!utterance.startsWith("jarvis"))
                continue;

            if (utterance.equalsIgnoreCase("jarvis shutdown")) {
                shutdown();
                continue;
            }

            if (utterance.equalsIgnoreCase("jarvis power sound")) {
                if (settings.getSound()) {
                    settings.setSound(false);
                    playbackSystem.playSound("playbackstop");
                } else {
                    settings.setSound(true);
                    playbackSystem.playSound("playbackstart");
                }
                continue;
            }
            User32 user = User32.INSTANCE;

            String[] words = utterance.split(" ");
            if (words.length >= 3) {
                if (words[1].equalsIgnoreCase("open")){

                }

                AtomicReference<WinDef.HWND> hwnd = new AtomicReference<>();
                switch (words[2]) {
                    case DISCORD: {
                        hwnd.set(user.findWindow("- Discord"));
                        break;
                    }
                    case CHROME: {
                        hwnd.set(user.findWindow("- Google Chrome"));
                        break;
                    }
                    case SPOTIFY: {
                        Kernel32 kernel32 = Native.load(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
                        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
                        WinNT.HANDLE snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));

                        // Loop through all processes
                        while (kernel32.Process32Next(snapshot, processEntry)) {
                            String processName = Native.toString(processEntry.szExeFile);
                            int pid = processEntry.th32ProcessID.intValue();

                            // If a process is a spotify process then check to see if its PID matches the PID of the currently visible spotify HWND
                            if (processName.equalsIgnoreCase("Spotify.exe")){
                                com.sun.jna.platform.win32.User32.INSTANCE.EnumWindows((hWnd, data) -> {
                                    IntByReference currentPID = new IntByReference();
                                    com.sun.jna.platform.win32.User32.INSTANCE.GetWindowThreadProcessId(hWnd, currentPID);

                                    if (currentPID.getValue() == pid && com.sun.jna.platform.win32.User32.INSTANCE.IsWindowVisible(hWnd)) {
                                        hwnd.set(hWnd);
                                        return false;
                                    }

                                    return true;
                                }, null);
                            }

                        }

                    }
                }

                if (hwnd.get() == null) {
                    errorSound();
                    continue;
                }

                switch (words[1]){
                    case "maximize": {
                        Windows.showWindow(hwnd.get(), 6);
                        Windows.showWindow(hwnd.get(), 3);
                        // Play success sound?
                        break;
                    }

                    case "minimize": {
                        Windows.showWindow(hwnd.get(), 6);
                        // Play success sound?
                        break;
                    }

                    case "focus": {
                        Windows.showWindow(hwnd.get(), 6);
                        Windows.showWindow(hwnd.get(), 1);
                        // Play success sound?
                        break;
                    }

                    case "hide": {
                        Windows.showWindow(hwnd.get(), 0);
                        // Play success sound?
                        break;
                    }

                    case "move": {
                        JNATools.switchMonitor(hwnd.get());
                        // Play success sound?
                        break;
                    }

                }
            }
        }
    }

    /**
     * Utility method to play error sound
     */
    private void errorSound(){
       playbackSystem.playSound("unavailable");
    }
}
