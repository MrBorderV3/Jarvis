package me.romeo.jarvis;

import edu.cmu.sphinx.api.Configuration;
import me.romeo.jarvis.systems.Jarvis;

import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private static Jarvis jarvis;

    public static void main(String[] args) throws InterruptedException {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        //configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        configuration.setGrammarPath("resource:/dialog/");
        configuration.setUseGrammar(true);

        configuration.setGrammarName("words.grxml");

        jarvis = new Jarvis(configuration);
        jarvis.setup();

        while (true){
            queue.take().run();
        }
    }

    public static LinkedBlockingQueue<Runnable> getMainThreadQueue() {
        return queue;
    }
}
