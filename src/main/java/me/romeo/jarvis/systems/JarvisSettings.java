package me.romeo.jarvis.systems;

import me.border.utilities.file.AbstractSerializedFile;
import me.border.utilities.scheduler.AsyncTasker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JarvisSettings extends AbstractSerializedFile<Map<String, Object[]>> {

    private boolean sound;

    public JarvisSettings(String file, File path) {
        super(file, path, new HashMap<>());

        setup();
        // If the map is empty then it means the settings have to be initialized from their default values, otherwise just load them.
        initializeSettings(getItem().isEmpty());
    }

    private void initializeSettings(boolean defaultValues){
        if (defaultValues){
            Object[] soundSetting = new Object[1];
            soundSetting[0] = true;
            getItem().put("Sound", soundSetting);
            sound = true;
            save();
        } else {
            sound = (boolean) getItem().get("Sound")[0];
        }
    }

    public boolean getSound(){
        return sound;
    }

    public void setSound(boolean sound){
        this.sound = sound;

        AsyncTasker.runTaskAsync(() -> {
            Object[] soundSetting = new Object[1];
            soundSetting[0] = sound;

            getItem().put("Sound", soundSetting);
            save();
        });
    }
}
