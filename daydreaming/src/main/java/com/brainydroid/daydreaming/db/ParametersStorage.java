package com.brainydroid.daydreaming.db;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.brainydroid.daydreaming.background.ErrorHandler;
import com.brainydroid.daydreaming.background.Logger;
import com.brainydroid.daydreaming.background.StatusManager;
import com.brainydroid.daydreaming.network.HttpConversationCallback;
import com.brainydroid.daydreaming.network.HttpGetData;
import com.brainydroid.daydreaming.network.HttpGetTask;
import com.brainydroid.daydreaming.network.ParametersStorageCallback;
import com.brainydroid.daydreaming.network.ServerConfig;
import com.brainydroid.daydreaming.sequence.Sequence;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
public class ParametersStorage {

    private static String TAG = "ParametersStorage";

    public static String QUESTIONS_SCHEDULING_MIN_DELAY = "schedulingMinDelay";
    public static String QUESTIONS_SCHEDULING_MEAN_DELAY = "schedulingMeanDelay";

    public static String BACKEND_EXP_ID = "backendExpId";
    public static String BACKEND_DB_NAME = "backendDbName";
    public static String EXP_DURATION = "expDuration";
    public static String BACKEND_API_URL = "backendApiUrl";
    public static String RESULTS_PAGE_URL = "resultsPageUrl";

    public static String GLOSSARY = "glossary";
    public static String QUESTIONS = "questions";
    public static String SEQUENCES = "sequences";

    public static String USER_POSSIBILITIES = "userPossibilities";

    private ArrayList<QuestionDescription> questionsCache;
    private ArrayList<SequenceDescription> sequencesCache;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor eSharedPreferences;

    @Inject Json json;
    @Inject NotificationManager notificationManager;
    @Inject SequencesStorage sequencesStorage;
    @Inject ProfileStorage profileStorage;
    @Inject StatusManager statusManager;
    @Inject ErrorHandler errorHandler;
    @Inject Context context;

    @SuppressLint("CommitPrefEdits")
    @Inject
    public ParametersStorage(SharedPreferences sharedPreferences, StatusManager statusManager) {
        Logger.d(TAG, "{} - Building ParametersStorage", statusManager.getCurrentModeName());

        this.sharedPreferences = sharedPreferences;
        eSharedPreferences = sharedPreferences.edit();
    }

    private synchronized void setBackendExpId(String backendExpId) {
        Logger.d(TAG, "{} - Setting backendExpId to {}", statusManager.getCurrentModeName(), backendExpId);
        eSharedPreferences.putString(statusManager.getCurrentModeName() + BACKEND_EXP_ID, backendExpId);
        eSharedPreferences.commit();
    }

    public synchronized String getBackendExpId() {
        String backendExpId = sharedPreferences.getString(
                statusManager.getCurrentModeName() + BACKEND_EXP_ID, null);
        if (backendExpId == null) {
            Logger.e(TAG, "{} - backendExpId is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("backendExpId is asked for but not set");
        }
        Logger.d(TAG, "{0} - backendExpId is {1}", statusManager.getCurrentModeName(),
                backendExpId);
        return backendExpId;
    }

    private synchronized void clearBackendExpId() {
        Logger.d(TAG, "{} - Clearing backendExpId", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + BACKEND_EXP_ID);
    }

    private synchronized void setBackendDbName(String backendDbName) {
        Logger.d(TAG, "{} - Setting backendDbName to {}", statusManager.getCurrentModeName(), backendDbName);
        eSharedPreferences.putString(statusManager.getCurrentModeName() + BACKEND_DB_NAME, backendDbName);
        eSharedPreferences.commit();
    }

    public synchronized String getBackendDbName() {
        String backendDbName = sharedPreferences.getString(
                statusManager.getCurrentModeName() + BACKEND_DB_NAME, null);
        if (backendDbName == null) {
            Logger.e(TAG, "{} - backendDbName is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("backendDbName is asked for but not set");
        }
        Logger.d(TAG, "{0} - backendDbName is {1}", statusManager.getCurrentModeName(),
                backendDbName);
        return backendDbName;
    }

    private synchronized void clearBackendDbName() {
        Logger.d(TAG, "{} - Clearing backendDbName", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + BACKEND_DB_NAME);
    }

    private synchronized void setExpDuration(int expDuration) {
        Logger.d(TAG, "{} - Setting expDuration to {}", statusManager.getCurrentModeName(), expDuration);
        eSharedPreferences.putInt(statusManager.getCurrentModeName() + EXP_DURATION, expDuration);
        eSharedPreferences.commit();
    }

    public synchronized int getExpDuration() {
        int expDuration = sharedPreferences.getInt(
                statusManager.getCurrentModeName() + EXP_DURATION, -1);
        if (expDuration == -1) {
            Logger.e(TAG, "{} - expDuration is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("expDuration is asked for but not set");
        }
        Logger.d(TAG, "{0} - expDuration is {1}", statusManager.getCurrentModeName(),
                expDuration);
        return expDuration;
    }

    private synchronized void clearExpDuration() {
        Logger.d(TAG, "{} - Clearing expDuration", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + EXP_DURATION);
    }

    private synchronized void setBackendApiUrl(String backendApiUrl) {
        Logger.d(TAG, "{} - Setting backendApiUrl to {}", statusManager.getCurrentModeName(), backendApiUrl);
        eSharedPreferences.putString(statusManager.getCurrentModeName() + BACKEND_API_URL, backendApiUrl);
        eSharedPreferences.commit();
        eSharedPreferences.commit();
    }

    public synchronized String getBackendApiUrl() {
        String backendApiUrl = sharedPreferences.getString(
                statusManager.getCurrentModeName() + BACKEND_API_URL, null);
        if (backendApiUrl == null) {
            Logger.e(TAG, "{} - backendApiUrl is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("backendApiUrl is asked for but not set");
        }
        Logger.d(TAG, "{0} - backendApiUrl is {1}", statusManager.getCurrentModeName(),
                backendApiUrl);
        return backendApiUrl;
    }

    private synchronized void clearBackendApiUrl() {
        Logger.d(TAG, "{} - Clearing backendApiUrl", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + BACKEND_API_URL);
    }

    private synchronized void setResultsPageUrl(String resultsPageUrl) {
        Logger.d(TAG, "{} - Setting resultsPageUrl to {}", statusManager.getCurrentModeName(), resultsPageUrl);
        eSharedPreferences.putString(statusManager.getCurrentModeName() + RESULTS_PAGE_URL, resultsPageUrl);
        eSharedPreferences.commit();
    }

    public synchronized String getResultsPageUrl() {
        String resultsPageUrl = sharedPreferences.getString(
                statusManager.getCurrentModeName() + RESULTS_PAGE_URL, null);
        if (resultsPageUrl == null) {
            Logger.e(TAG, "{} - resultsPageUrl is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("resultsPageUrl is asked for but not set");
        }

        // Use test results page if in test mode
        if (statusManager.getCurrentMode() == StatusManager.MODE_TEST) {
            resultsPageUrl += "/test";
        }

        Logger.d(TAG, "{0} - resultsPageUrl is {1}", statusManager.getCurrentModeName(),
                resultsPageUrl);
        return resultsPageUrl;
    }

    private synchronized void clearResultsPageUrl() {
        Logger.d(TAG, "{} - Clearing resultsPageUrl", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + RESULTS_PAGE_URL);
    }

    private synchronized void setSchedulingMinDelay(int schedulingMinDelay) {
        Logger.d(TAG, "{0} - Setting schedulingMinDelay to {1}", statusManager.getCurrentModeName(), schedulingMinDelay);
        eSharedPreferences.putInt(statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MIN_DELAY, schedulingMinDelay);
        eSharedPreferences.commit();
    }

    public synchronized int getSchedulingMinDelay() {
        int schedulingMinDelay = sharedPreferences.getInt(
                statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MIN_DELAY, -1);
        if (schedulingMinDelay == -1) {
            Logger.e(TAG, "{} - SchedulingMinDelay is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("SchedulingMinDelay is asked for but not set");
        }
        Logger.d(TAG, "{0} - schedulingMinDelay is {1}", statusManager.getCurrentModeName(),
                schedulingMinDelay);
        return schedulingMinDelay;
    }

    private synchronized void clearSchedulingMinDelay() {
        Logger.d(TAG, "{} - Clearing schedulingMinDelay", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MIN_DELAY);
    }

    private synchronized void setSchedulingMeanDelay(int schedulingMeanDelay) {
        Logger.d(TAG, "{0} - Setting schedulingMeanDelay to {1}", statusManager.getCurrentModeName(), schedulingMeanDelay);
        eSharedPreferences.putInt(statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MEAN_DELAY, schedulingMeanDelay);
        eSharedPreferences.commit();
    }

    public synchronized int getSchedulingMeanDelay() {
        int schedulingMeanDelay = sharedPreferences.getInt(
                statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MEAN_DELAY, -1);
        if (schedulingMeanDelay == -1) {
            Logger.e(TAG, "{} - SchedulingMeanDelay is asked for but not set",
                    statusManager.getCurrentMode());
            throw new RuntimeException("SchedulingMeanDelay is asked for but not set");
        }
        Logger.d(TAG, "{0} - schedulingMeanDelay is {1}", statusManager.getCurrentModeName(),
                schedulingMeanDelay);
        return schedulingMeanDelay;
    }

    private synchronized void clearSchedulingMeanDelay() {
        Logger.d(TAG, "{} - Clearing schedulingMeanDelay", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + QUESTIONS_SCHEDULING_MEAN_DELAY);
        eSharedPreferences.commit();
    }

    public synchronized void setQuestions(ArrayList<QuestionDescription> questions) {
        Logger.d(TAG, "{} - Setting questions array (and keeping in cache)",
                statusManager.getCurrentModeName());
        questionsCache = questions;
        eSharedPreferences.putString(statusManager.getCurrentModeName() + QUESTIONS,
                json.toJsonInternal(questions));
        eSharedPreferences.commit();
    }

    public synchronized ArrayList<QuestionDescription> getQuestions() {
        Logger.d(TAG, "{} - Getting questions", statusManager.getCurrentModeName());
        if (questionsCache != null) {
            Logger.v(TAG, "{} - Cache is present -> returning questions from cache",
                    statusManager.getCurrentModeName());
            return questionsCache;
        } else {
            Logger.v(TAG, "{} - Cache not present -> getting questions from sharedPreferences",
                    statusManager.getCurrentModeName());
            TypeReference<ArrayList<QuestionDescription>> questionDescriptionsArrayType =
                    new TypeReference<ArrayList<QuestionDescription>>() {};
            String questionsJson = sharedPreferences.getString(
                    statusManager.getCurrentModeName() + QUESTIONS, null);

            if (questionsJson == null) {
                Logger.e(TAG, "{} - Questions asked for but not set",
                        statusManager.getCurrentModeName());
                throw new RuntimeException("Questions asked for but not set");
            }

            try {
                questionsCache = json.fromJson(questionsJson, questionDescriptionsArrayType);
                return questionsCache;
            } catch (JSONException e) {
                errorHandler.handleBaseJsonError(questionsJson, e);
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void clearQuestions() {
        Logger.d(TAG, "{} - Clearing questions (and clearing cache)",
                statusManager.getCurrentModeName());
        questionsCache = null;
        eSharedPreferences.remove(statusManager.getCurrentModeName() + QUESTIONS);
        eSharedPreferences.commit();
    }

    private synchronized void setGlossary(HashMap<String,String> glossary) {
        String glossaryJson = json.toJsonInternal(glossary);
        Logger.d(TAG, "{0} - Setting glossary to {1}", statusManager.getCurrentModeName(), glossaryJson);
        eSharedPreferences.putString(statusManager.getCurrentModeName() + GLOSSARY, glossaryJson);
        eSharedPreferences.commit();
    }

    public synchronized HashMap<String,String> getGlossary() {
        String glossaryJson = sharedPreferences.getString(
                statusManager.getCurrentModeName() + GLOSSARY, null);
        if (glossaryJson == null) {
            String msg = "Glossary asked for but not found";
            Logger.e(TAG, msg);
            throw new RuntimeException(msg);
        }

        try {
            HashMap<String, String> glossary = json.fromJson(glossaryJson,
                    new TypeReference<HashMap<String, String>>() {
                    });
            Logger.v(TAG, "{0} - Glossary is {1}", statusManager.getCurrentModeName(), glossaryJson);
            return glossary;
        } catch (JSONException e) {
            errorHandler.handleBaseJsonError(glossaryJson, e);
            throw new RuntimeException(e);
        }
    }

    private synchronized void clearGlossary() {
        Logger.d(TAG, "{} - Clearing glossary", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + GLOSSARY);
        eSharedPreferences.commit();
    }

    public synchronized QuestionDescription getQuestionDescription(String name) {
        Logger.d(TAG, "{0} - Looking for questionDescription {1}",
                statusManager.getCurrentModeName(), name);

        // Get list of all names
        ArrayList<QuestionDescription> questions = getQuestions();
        ArrayList<String> names = new ArrayList<String>(questions.size());
        for (QuestionDescription qd : questions) {
            names.add(qd.getQuestionName());
        }

        int questionIndex = names.indexOf(name);
        if (questionIndex == -1) {
            Logger.e(TAG, "{0} - Question {1} asked for but not found",
                    statusManager.getCurrentModeName(), name);
            throw new RuntimeException("Question asked for but not found (see logs)");
        }

        return questions.get(questionIndex);
    }

    public synchronized void setSequences(ArrayList<SequenceDescription> sequences) {
        Logger.d(TAG, "{} - Setting sequences array (and keeping in cache)",
                statusManager.getCurrentModeName());

        // Duplicate begin to end (name and type are changed)
        // To avoid having to duplicate the questionnaires in external parameters
        ArrayList<SequenceDescription> endSequences = new ArrayList<SequenceDescription>();
        for (SequenceDescription sd : sequences) {
            if (sd.getType().equals(Sequence.TYPE_BEGIN_END_QUESTIONNAIRE)) {
                SequenceDescription sdCopy = new SequenceDescription();
                sdCopy.setPageGroups(sd.getPageGroups());
                sdCopy.setNSlots(sd.getNSlots());
                sdCopy.setIntro(sd.getIntro());
                sdCopy.setType(Sequence.TYPE_END_QUESTIONNAIRE);
                sdCopy.setName(Sequence.END_PREFIX + sd.getName());
                endSequences.add(sdCopy);
                sd.setType(Sequence.TYPE_BEGIN_QUESTIONNAIRE);
                sd.setName(Sequence.BEGIN_PREFIX + sd.getName());
            }
        }
        // and save duplicate
        sequences.addAll(endSequences);
        clearSequences();
        clearBEQ();
        sequencesCache = sequences;
        eSharedPreferences.putString(statusManager.getCurrentModeName() + SEQUENCES,
                json.toJsonInternal(sequences));
        eSharedPreferences.commit();
    }

    public synchronized ArrayList<SequenceDescription> getSequences() {
        Logger.d(TAG, "{} - Getting sequences", statusManager.getCurrentModeName());
        if (sequencesCache != null) {
            Logger.v(TAG, "{} - Cache is present -> returning sequences from cache",
                    statusManager.getCurrentModeName());
        } else {
            Logger.v(TAG, "{} - Cache not present -> getting sequences from sharedPreferences",
                    statusManager.getCurrentModeName());
            TypeReference<ArrayList<SequenceDescription>> sequenceDescriptionsArrayType =
                    new TypeReference<ArrayList<SequenceDescription>>() {};

            String sequencesJson = sharedPreferences.getString(
                    statusManager.getCurrentModeName() + SEQUENCES, null);
            if (sequencesJson == null) {
                Logger.e(TAG, "{} - Sequences asked for but not set",
                        statusManager.getCurrentModeName());
                throw new RuntimeException("Sequences asked for but not set");
            }

            try {
                sequencesCache = json.fromJson(sequencesJson, sequenceDescriptionsArrayType);
                return sequencesCache;
            } catch (JSONException e) {
                errorHandler.handleBaseJsonError(sequencesJson, e);
                throw new RuntimeException(e);
            }
        }

        return sequencesCache;
    }

    private synchronized void clearSequences() {
        Logger.d(TAG, "{} - Clearing sequences (and clearing cache)",
                statusManager.getCurrentModeName());
        sequencesCache = null;
        eSharedPreferences.remove(statusManager.getCurrentModeName() + SEQUENCES);
        eSharedPreferences.commit();
    }

    public synchronized void clearBEQ() {
        Logger.d(TAG, "Clearing BEQ sequences from storage, and pending notifications");
        // clear db
        sequencesStorage.removeAllSequences(new String[] {Sequence.TYPE_BEGIN_QUESTIONNAIRE,
                Sequence.TYPE_END_QUESTIONNAIRE});
        // clear notifications
        notificationManager.cancel(Sequence.TYPE_BEGIN_END_QUESTIONNAIRE, 0);
    }

    public synchronized SequenceDescription getSequenceDescription(String name) {
        Logger.d(TAG, "{0} - Looking for sequenceDescription {1}",
                statusManager.getCurrentModeName(), name);

        // Get list of all names
        ArrayList<SequenceDescription> sequences = getSequences();
        ArrayList<String> names = new ArrayList<String>(sequences.size());
        for (SequenceDescription s : sequences) {
            names.add(s.getName());
        }

        int sequenceIndex = names.indexOf(name);
        if (sequenceIndex == -1) {
            Logger.e(TAG, "{0} - Sequence {1} asked for but not found",
                    statusManager.getCurrentModeName(), name);
            throw new RuntimeException("Sequence asked for but not found (see logs)");
        }

        return sequences.get(sequenceIndex);
    }

    public synchronized ArrayList<SequenceDescription> getSequencesByType(String type) {
        Logger.d(TAG, "{} - Getting sequences by type", statusManager.getCurrentModeName());
        ArrayList<SequenceDescription> sequences = getSequences();
        ArrayList<SequenceDescription> sequencesByType = new ArrayList<SequenceDescription>();
        for (SequenceDescription s : sequences) {
            if (s.getType().equals(type)){
                sequencesByType.add(s);
            }
        }
        return sequencesByType;
    }

    public synchronized ArrayList<SequenceDescription> getSequencesByTypes(String[] types) {
        Logger.d(TAG, "{} - Getting sequences by types", statusManager.getCurrentModeName());
        ArrayList<SequenceDescription> sequences = getSequences();
        ArrayList<SequenceDescription> sequencesByType = new ArrayList<SequenceDescription>();
        for (String type : types) {
            for (SequenceDescription s : sequences) {
                if (s.getType().equals(type)){
                    sequencesByType.add(s);
                }
            }
        }
        return sequencesByType;
    }

    private HashMap<String, ArrayList<String>> getAllUserPossibilities() {
        String allPossibilitiesJson = sharedPreferences.getString(
                statusManager.getCurrentModeName() + USER_POSSIBILITIES, null);
        if (allPossibilitiesJson == null) {
            return new HashMap<String,ArrayList<String>>();
        } else {
            try {
                return json.fromJson(allPossibilitiesJson,
                        new TypeReference<HashMap<String, ArrayList<String>>>() {});
            } catch (JSONException e) {
                errorHandler.handleBaseJsonError(allPossibilitiesJson, e);
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<String> getUserPossibilities(String questionName) {
        HashMap<String, ArrayList<String>> allPossibilities = getAllUserPossibilities();
        if (!allPossibilities.containsKey(questionName)) {
            return new ArrayList<String>();
        } else {
            return allPossibilities.get(questionName);
        }
    }

    public void addUserPossibility(String questionName, String possibility) {
        HashMap<String, ArrayList<String>> allPossibilities = getAllUserPossibilities();
        if (!allPossibilities.containsKey(questionName)) {
            allPossibilities.put(questionName, new ArrayList<String>());
        }

        ArrayList<String> questionPossibilities = allPossibilities.get(questionName);

        if (!questionPossibilities.contains(possibility)) {
            Logger.v(TAG, "{0} - Adding possibility {1} to user possibilities for question {2}",
                    statusManager.getCurrentModeName(), possibility, questionName);

            questionPossibilities.add(possibility);
            eSharedPreferences.putString(
                    statusManager.getCurrentModeName() + USER_POSSIBILITIES,
                    json.toJsonInternal(allPossibilities));
            eSharedPreferences.commit();
        }
    }

    public void addUserPossibilities(String questionName, ArrayList<String> possibilities) {
        HashMap<String, ArrayList<String>> allPossibilities = getAllUserPossibilities();
        if (!allPossibilities.containsKey(questionName)) {
            allPossibilities.put(questionName, new ArrayList<String>());
        }

        ArrayList<String> questionPossibilities = allPossibilities.get(questionName);
        for (String possibility : possibilities) {
            if (!questionPossibilities.contains(possibility)) {
                Logger.v(TAG, "{0} - Adding possibility {1} to user possibilities for question {2}",
                        statusManager.getCurrentModeName(), possibility, questionName);
                questionPossibilities.add(possibility);
            }
        }
        eSharedPreferences.putString(
                statusManager.getCurrentModeName() + USER_POSSIBILITIES,
                json.toJsonInternal(allPossibilities));
        eSharedPreferences.commit();
    }

    public void removeUserPossibility(String questionName, String possibility) {
        HashMap<String, ArrayList<String>> allPossibilities = getAllUserPossibilities();
        if (!allPossibilities.containsKey(questionName)) {
            // Nothing to remove, question isn't registered here.
            return;
        }

        ArrayList<String> questionPossibilities = allPossibilities.get(questionName);

        if (questionPossibilities.contains(possibility)) {
            Logger.v(TAG, "{0} - Removing possibility {1} from user possibilities for question {2}",
                    statusManager.getCurrentModeName(), possibility, questionName);

            questionPossibilities.remove(possibility);
            eSharedPreferences.putString(
                    statusManager.getCurrentModeName() + USER_POSSIBILITIES,
                    json.toJsonInternal(allPossibilities));
            eSharedPreferences.commit();
        }
    }

    private void clearAllUserPossibilities() {
        Logger.v(TAG, "{} - Clearing user possibilities", statusManager.getCurrentModeName());
        eSharedPreferences.remove(statusManager.getCurrentModeName() + USER_POSSIBILITIES);
    }

    public synchronized void flush() {
        Logger.d(TAG, "{} - Flushing all parameters", statusManager.getCurrentModeName());
        statusManager.clearParametersUpdated();
        statusManager.set(StatusManager.EXP_STATUS_PARAMETERS_FLUSHED);

        profileStorage.clearParametersVersion();
        clearBackendExpId();
        clearBackendDbName();
        clearExpDuration();
        clearBackendApiUrl();
        clearResultsPageUrl();
        clearSchedulingMinDelay();
        clearSchedulingMeanDelay();
        clearQuestions();
        clearSequences();
        clearGlossary();
        clearAllUserPossibilities();
    }

    // import parameters from json file into database
    public synchronized void importParameters(String jsonParametersString)
            throws ParametersSyntaxException {
        Logger.d(TAG, "{} - Importing parameters from JSON", statusManager.getCurrentModeName());
        try {
            ServerParametersJson serverParametersJson;
            try {
                serverParametersJson = json.fromJson(jsonParametersString, ServerParametersJson.class);
            } catch (JSONException e) {
                errorHandler.handleBaseJsonError(jsonParametersString, e);
                throw new JsonParametersException("Server Json was malformed, could not be parsed");
            }

            serverParametersJson.validateInitialization();

            // All is good, do the real import of all objects in the root
            flush();
            profileStorage.setParametersVersion(serverParametersJson.getVersion());
            setBackendExpId(serverParametersJson.getBackendExpId());
            setBackendDbName(serverParametersJson.getBackendDbName());
            setExpDuration(serverParametersJson.getExpDuration());
            setBackendApiUrl(serverParametersJson.getBackendApiUrl());
            setResultsPageUrl(serverParametersJson.getResultsPageUrl());
            setSchedulingMinDelay(serverParametersJson.getSchedulingMinDelay());
            setSchedulingMeanDelay(serverParametersJson.getSchedulingMeanDelay());
            setGlossary(serverParametersJson.getGlossary());

            // loading the questions
            setQuestions(serverParametersJson.getQuestions());
            setSequences(serverParametersJson.getSequences());

            // Instantiating the Begin and End Questionnaires
            sequencesStorage.instantiateBeginEndQuestionnaires();
            statusManager.setCurrentBEQType(Sequence.TYPE_BEGIN_QUESTIONNAIRE);

        } catch (JsonParametersException e) {
            Logger.e(TAG, "Parameters validation failed:");
            Logger.eRaw(TAG, e.getMessage());
            e.printStackTrace();
            throw new ParametersSyntaxException(e);
        }
    }

    public synchronized void onReady(ParametersStorageCallback callback, String startSyncAppMode,
                                     boolean isDebug) {
        if (!statusManager.areParametersUpdated()) {
            Logger.i(TAG, "{} - ParametersStorage not ready -> " +
                    "updating parameters", statusManager.getCurrentModeName());

            // If during our network request, parameters are flushed,
            // we won't import the received parameters
            Logger.v(TAG, "Clearing parameters flushed");
            statusManager.clear(StatusManager.EXP_STATUS_PARAMETERS_FLUSHED);

            asyncUpdateParameters(callback, startSyncAppMode, isDebug);
        } else {
            Logger.i(TAG, "{} - ParametersStorage ready -> calling back callback " +
                    "straight away", statusManager.getCurrentModeName());
            callback.onParametersStorageReady(true);
        }
    }

    private synchronized void asyncUpdateParameters(final ParametersStorageCallback callback,
                                                    final String startSyncAppMode,
                                                    final boolean isDebug) {
        Logger.d(TAG, "Updating parameters");

        if (statusManager.getCurrentMode() == StatusManager.MODE_TEST && isDebug) {
            Toast.makeText(context, "Reloading parameters...", Toast.LENGTH_SHORT).show();
        }

        HttpConversationCallback updateParametersCallback =
                new HttpConversationCallback() {

            private String TAG = "Parameters HttpConversationCallback";

            @Override
            public void onHttpConversationFinished(boolean success,
                                                   String serverAnswer) {
                Logger.d(TAG, "Parameters update HttpConversation finished");

                // Exit if app mode has changed before we could import parameters
                if (!statusManager.getCurrentModeName().equals(startSyncAppMode)) {
                    Logger.i(TAG, "App mode has changed from {0} to {1} since sync started, "
                                    + "aborting parameters update.", startSyncAppMode,
                            statusManager.getCurrentModeName());
                    callback.onParametersStorageReady(false);
                    statusManager.setParametersUpdated(false);
                    return;
                }

                // Exit if parameters have been flushed since we started
                if (statusManager.is(StatusManager.EXP_STATUS_PARAMETERS_FLUSHED)) {
                    Logger.i(TAG, "Parameters have been flushed since sync started, "
                            + "aborting parameters update.");
                    callback.onParametersStorageReady(false);
                    statusManager.setParametersUpdated(false);
                    return;
                }

                if (success) {
                    Logger.i(TAG, "Successfully retrieved parameters from server");
                    Logger.td(context, TAG + ": new parameters downloaded from server");

                    // Import the parameters, and remember not to update
                    // parameters again.
                    try {
                        ParametersStorage.this.importParameters(serverAnswer);
                        Logger.d(TAG, "Importing new parameters to storage");
                    } catch (ParametersSyntaxException e) {
                        e.printStackTrace();
                        Logger.e(TAG, "Downloaded parameters were malformed -> " +
                                "parameters not updated");
                        callback.onParametersStorageReady(false);
                        statusManager.setParametersUpdated(false);
                        if (statusManager.getCurrentMode() == StatusManager.MODE_TEST) {
                            Toast.makeText(context, "Test parameters from server were malformed! " +
                                    "Correct them and try again", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    Logger.i(TAG, "Parameters successfully imported");
                    statusManager.setParametersUpdated(true);
                    callback.onParametersStorageReady(true);

                    if (isDebug && statusManager.getCurrentMode() == StatusManager.MODE_TEST) {
                        Toast.makeText(context, "Parameters successfully updated",
                                Toast.LENGTH_SHORT).show();
                    }

                    Logger.d(TAG, "Starting scheduler services to take new parameters into account");
                    statusManager.launchNotifyingServices();
                } else {
                    Logger.w(TAG, "Error while retrieving new parameters from " +
                            "server");
                    callback.onParametersStorageReady(false);
                    statusManager.setParametersUpdated(false);
                    if (isDebug && statusManager.getCurrentMode() == StatusManager.MODE_TEST) {
                        Toast.makeText(context, "Error retrieving parameters from server. " +
                                "Are you connected to internet?", Toast.LENGTH_LONG).show();
                    }
                }
            }

        };

        String getUrl = MessageFormat.format(ServerConfig.PARAMETERS_URL_BASE,
                statusManager.getCurrentModeName());
        HttpGetData updateParametersData = new HttpGetData(getUrl, updateParametersCallback);
        HttpGetTask updateParametersTask = new HttpGetTask();
        updateParametersTask.execute(updateParametersData);
    }

}
