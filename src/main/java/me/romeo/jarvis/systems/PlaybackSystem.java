package me.romeo.jarvis.systems;

import me.border.utilities.scheduler.AsyncTasker;
import me.romeo.jarvis.Main;

import javax.sound.sampled.*;
import java.util.HashSet;
import java.util.Set;

public class PlaybackSystem {

    public void playSound(String name){
        playSound(name, (Set<LineListener>) null);
    }

    public void playSound(String name, LineListener listener){
        if (listener != null)
            playSound(name, singletonSet(listener));
        else
            playSound(name, (Set<LineListener>) null);
    }

    public void playSound(String name, Set<LineListener> listeners){
        AsyncTasker.runTaskAsync(() -> {
            Clip clip = getClip("/sound/" + name + ".wav");

            if (clip == null)
                throw new NullPointerException("Clip: '" + name + "' is null");

            if (listeners != null) {
                listeners.forEach(clip::addLineListener);
            }

            clip.start();
        });
    }

    private Clip getClip(String url) {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(Main.class.getResourceAsStream(url));
            clip.open(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clip;
    }

    private Set<LineListener> singletonSet(LineListener listener){
        Set<LineListener> singletonSet = new HashSet<>(1);
        singletonSet.add(listener);

        return singletonSet;
    }
}
