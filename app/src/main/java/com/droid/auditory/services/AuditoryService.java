package com.droid.auditory.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;

import com.droid.auditory.application.Dagger;
import com.droid.auditory.application.RxBus;
import com.droid.auditory.events.StartListeningEvent;
import com.droid.auditory.receivers.AuditoryBroadcastReceiver;
import com.droid.lib.models.memory.MemoryCache;
import com.droid.lib.utils.IntentUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_PING;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_SEND_TEXT_HEARD;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_STARTUP;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_STARTUP_RECEIVED;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_START_LISTENING;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_START_LISTENING_RECEIVED;
import static com.droid.lib.constants.IntentConstants.EXTRA_AUDITORY_EXTRA_CONFIDENCE_SCORE;
import static com.droid.lib.constants.IntentConstants.EXTRA_AUDITORY_EXTRA_TEXT_HEARD;

public class AuditoryService extends Service implements RecognitionListener {

    @Inject
    RxBus bus;
    @Inject
    IntentUtils intentUtils;
    @Inject
    MemoryCache memoryCache;
    @Inject
    SpeechRecognizer speechRecognizer;
    @Inject
    AudioManager audioManager;

    AuditoryBroadcastReceiver auditoryBroadCastReceiver;
    Intent recognizerIntent;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Dagger.getDagger().inject(this);
        super.onCreate();
        Timber.i("AuditoryService - onCreate()");
        auditoryBroadCastReceiver = new AuditoryBroadcastReceiver(bus, intentUtils);
        registerAuditoryBroadcastReceiver();
        initSubscriptions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int defaultCommand = super.onStartCommand(intent, flags, startId);

        Timber.i("onStartCommand()");
        onHandleIntent(intent);

        return defaultCommand;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(auditoryBroadCastReceiver);
    }

    void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                Timber.i("AuditoryService onHandleIntent = " + action);
                switch (action) {
                    case ACTION_AUDITORY_STARTUP:
                        final Intent auditoryStartupIntent =
                                intentUtils.createBroadcastIntent(ACTION_AUDITORY_STARTUP_RECEIVED, new Bundle());
                        intentUtils.sendBroadcast(getApplicationContext(), auditoryStartupIntent);
                }
            }
        }
    }

    public void initSubscriptions() {
        Timber.i("AuditoryService - initSubscriptions()");
        CompositeSubscription subscriptions = new CompositeSubscription();
        subscriptions.add(bus.setSubscriptions(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof StartListeningEvent) {
                    destroyAndRestartListeningRecognizer();
                    sendStartListeningReceived();
                }
            }
        }));
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Timber.i("onReadyForSpeech()");
    }

    @Override
    public void onBeginningOfSpeech() {
        Timber.i("onBeginningOfSpeech()");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        speechRecognizer.stopListening();
        Timber.i("onEndOfSpeech() - recognizer.stopListening");
    }

    @Override
    public void onError(int error) {
        String message;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                destroyAndRestartListeningRecognizer();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
//                currentConversation.onEnd();
                destroyAndRestartListeningRecognizer();
                break;
            default:
                message = "Not recognized";
                destroyAndRestartListeningRecognizer();
                break;
        }
        Timber.i("SpeechRecognizer onError code:" + error + " message: " + message);
    }

    @Override
    public void onResults(Bundle results) {
        if ((results != null) && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            ArrayList<String> wordsRecognized = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            Timber.i("Recognized words - " + wordsRecognized);

            //SEND HEARD WORDS TO COORDNATOR
            Bundle wordsRetrieved = new Bundle();
            wordsRetrieved.putStringArrayList(EXTRA_AUDITORY_EXTRA_TEXT_HEARD, wordsRecognized);
            wordsRetrieved.putFloatArray(EXTRA_AUDITORY_EXTRA_CONFIDENCE_SCORE, confidenceScores);
            final Intent wordRetrievalCompleteIntent = intentUtils.createBroadcastIntent(ACTION_AUDITORY_SEND_TEXT_HEARD, wordsRetrieved);
            intentUtils.sendBroadcast(getApplicationContext(), wordRetrievalCompleteIntent);

        } else {
            Timber.i("no results");
            destroyAndRestartListeningRecognizer();
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    void createRecognizerIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    }

    void destroyAndRestartListeningRecognizer() {
        createRecognizerIntent();
        Timber.i("destroyAndRestartListeningRecognizer()");
        Timber.i("speechRecognizer is null? " + (speechRecognizer == null));
        if (speechRecognizer != null) {
            Timber.i("speechRecognizer cancel/destroy");
            //TODO: Test
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
        speechRecognizer = null;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechRecognizer.startListening(recognizerIntent);
    }

    void registerAuditoryBroadcastReceiver() {
        Timber.i("CoordinatorService - registerMasterBroadcastReceiver()");
        registerReceiver(auditoryBroadCastReceiver, getAuditoryIntentFilter());
    }

    IntentFilter getAuditoryIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_AUDITORY_PING);
        intentFilter.addAction(ACTION_AUDITORY_START_LISTENING);

        return intentFilter;
    }

    void sendStartListeningReceived() {
        final Intent auditoryStartListeningReceivedIntent =
                intentUtils.createBroadcastIntent(ACTION_AUDITORY_START_LISTENING_RECEIVED, new Bundle());
        intentUtils.sendBroadcast(getApplicationContext(), auditoryStartListeningReceivedIntent);
    }
}
