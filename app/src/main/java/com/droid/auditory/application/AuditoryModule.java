package com.droid.auditory.application;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.speech.SpeechRecognizer;

import com.droid.lib.models.memory.MemoryCache;
import com.droid.lib.models.memory.WordMatcher;
import com.droid.lib.utils.IntentUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AuditoryModule {

    private final Application application;

    public AuditoryModule(Application application) {
        this.application = application;
    }

    @Provides
    public Context getApplicationContext() {
        return application;
    }

    @Provides
    public ContentResolver getContentResolver() {
        return application.getContentResolver();
    }

    @Provides
    @Singleton
    public RxBus getBus() {
        return new RxBus();
    }

    @Provides
    @Singleton
    public IntentUtils getIntentUtils() {
        return new IntentUtils();
    }

    @Provides
    @Singleton
    public MemoryCache getMemoryCache() {
        return new MemoryCache();
    }

    @Provides
    @Singleton
    public SpeechRecognizer getSpeechRecognizer() {
        return SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
    }

    @Provides
    @Singleton
    public AudioManager getAudioManager() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        return audioManager;
    }

    @Provides
    @Singleton
    public WordMatcher getWordMatcher() {
        return new WordMatcher();
    }
}
