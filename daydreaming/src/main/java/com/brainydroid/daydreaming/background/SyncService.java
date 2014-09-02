package com.brainydroid.daydreaming.background;

import android.content.Intent;
import android.os.IBinder;

import com.brainydroid.daydreaming.db.Json;
import com.brainydroid.daydreaming.db.LocationPoint;
import com.brainydroid.daydreaming.db.LocationPointsStorage;
import com.brainydroid.daydreaming.db.ParametersStorage;
import com.brainydroid.daydreaming.db.Poll;
import com.brainydroid.daydreaming.db.PollsStorage;
import com.brainydroid.daydreaming.db.ProfileStorage;
import com.brainydroid.daydreaming.network.CryptoStorage;
import com.brainydroid.daydreaming.network.CryptoStorageCallback;
import com.brainydroid.daydreaming.network.HttpConversationCallback;
import com.brainydroid.daydreaming.network.ParametersStorageCallback;
import com.brainydroid.daydreaming.network.ProfileWrapper;
import com.brainydroid.daydreaming.network.ResultsWrapper;
import com.brainydroid.daydreaming.network.ResultsWrapperFactory;
import com.brainydroid.daydreaming.network.ServerTalker;
import com.google.inject.Inject;

import java.util.ArrayList;

import roboguice.service.RoboService;

// FIXME: Worker thread restart on runtime configuration change.
// There might be a problem if the service is started from an
// Activity, and the orientation of the display changes. That will stop and
// restart the worker thread. See
// http://developer.android.com/guide/components/processes-and-threads.html
// right above the "Thread-safe methods" title.

/**
 * Update the parameter  pool, upload answers and location points.
 *
 * @author Sébastien Lerique
 * @author Vincent Adam
 * @see ProbeService
 * @see SchedulerService
 */
public class SyncService extends RoboService {

    protected static String TAG = "SyncService";

    public static String DEBUG_SYNC = "debugSync";
    private boolean isDebugSync = false;
    private String startSyncAppMode;

    @Inject StatusManager statusManager;
    @Inject PollsStorage pollsStorage;
    @Inject LocationPointsStorage locationPointsStorage;
    @Inject ParametersStorage parametersStorage;
    @Inject ProfileStorage profileStorage;
    @Inject CryptoStorage cryptoStorage;
    @Inject ServerTalker serverTalker;
    @Inject Json json;
    @Inject ResultsWrapperFactory<Poll> pollsWrapperFactory;
    @Inject ResultsWrapperFactory<LocationPoint> locationPointsWrapperFactory;

    ParametersStorageCallback parametersStorageCallback =
            new ParametersStorageCallback() {

        private String TAG = "ParametersStorageCallback";

        @Override
        public void onParametersStorageReady(boolean areParametersUpdated) {
            Logger.d(TAG, "ParametersStorage is ready");

            // Only launch the rest of the synchronization tasks if ParametersStorage is
            // really ready and we have Internet access.
            if (areParametersUpdated && statusManager.isDataEnabled()) {
                Logger.d(TAG, "Parameters have been update, and data is enabled");
                cryptoStorage.onReady(cryptoStorageCallback);
            } else {
                Logger.v(TAG, "Either parameters were not updated, or data is disabled "
                + "-> doing nothing");
            }

        }
    };

    /**
     * Callback called once the {@link CryptoStorage} is ready,
     * launching the synchronization tasks.
     */
    CryptoStorageCallback cryptoStorageCallback =
            new CryptoStorageCallback() {

        private String TAG = "CryptoStorageCallback";

        @Override
        public void onCryptoStorageReady(
                boolean hasKeyPairAndMaiId) {
            Logger.d(TAG, "CryptoStorage is ready");

            // Only launch the synchronization tasks if CryptoStorage is
            // really ready and we have Internet access.
            if (hasKeyPairAndMaiId && statusManager.isDataEnabled()) {
                Logger.d(TAG, "Have keypair and id, and data is enabled");

                // We only sync the profile if stored data has been changed
                if (profileStorage.isDirty()) {
                    Logger.d(TAG, "Launching profile update");
                    asyncPutProfile();
                } else {
                    Logger.v(TAG, "Profile has not changed since last update");
                }

                Logger.d(TAG, "Launching poll and locationPoints upload");
                asyncUploadPolls();
                asyncUploadLocationPoints();
            } else {
                Logger.v(TAG, "Either no keypair or no id or no data " +
                        "connection -> doing nothing");
            }
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "SyncService started");
        super.onStartCommand(intent, flags, startId);

        // Record the current app mode for later comparison in the callbacks
        startSyncAppMode = statusManager.getCurrentModeName();

        // Launch synchronization tasks if we haven't done so not long ago
        isDebugSync = intent.getBooleanExtra(DEBUG_SYNC, false);
        if (statusManager.isLastSyncLongAgo() || isDebugSync) {
            Logger.d(TAG, "Last sync was long ago or this is a debug sync -> starting updates");
            startUpdates();
        } else {
            Logger.v(TAG, "Last sync was not long ago -> exiting");
            stopSelf();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't allow binding
        return null;
    }

    private void startUpdates() {
        Logger.d(TAG, "Initializing crypto to launch sync tasks");

        if (statusManager.isDataEnabled()) {
            Logger.i(TAG, "Data connection enabled -> starting sync tasks");
            Logger.td(this, TAG + ": starting sync...");

            statusManager.setLastSyncToNow();

            // This will launch all the calls through the callback
            parametersStorage.onReady(parametersStorageCallback, startSyncAppMode, isDebugSync);
        } else {
            Logger.i(TAG, "No data connection available -> exiting");
            Logger.td(this, TAG + ": no internet connection");
        }

        // We stop immediately, but the worker threads keep running until
        // they finish or time out.
        Logger.d(TAG, "Stopping self");
        stopSelf();
    }

    /**
     * Upload answered {@link Poll}s to the server and remove them from local
     * storage, asynchronously.
     */
    private void asyncUploadPolls() {
        Logger.d(TAG, "Syncing polls");

        // Do we have any polls to upload?
        ArrayList<Poll> uploadablePolls = pollsStorage.getUploadablePolls();
        if (uploadablePolls == null) {
            Logger.i(TAG, "No polls to upload -> exiting");
            Logger.td(this, TAG + ": no polls to upload");
            return;
        }

        // Wrap uploadable polls in a single structure to provide a root
        // node when jsonifying
        final ResultsWrapper<Poll> pollsWrap = pollsWrapperFactory.create(
                uploadablePolls);

        // Called once the HttpPostTask completes or times out
        HttpConversationCallback callback = new HttpConversationCallback() {

            private String TAG = "Polls HttpConversationCallback";

            @Override
            public void onHttpConversationFinished(boolean success,
                                                   String serverAnswer) {
                Logger.d(TAG, "Polls sync HttpConversation finished");

                // If at this point, the app has changed from one of test/prod modes
                // to the other between the POST start and finish, the polls we're about
                // to remove from the database are probably already gone. The app shouldn't
                // crash, but if it does, we should be able to see what happened in the
                // ACRA logs.

                if (success) {
                    // TODO: handle the case where returned JSON is in fact an error.
                    Logger.i(TAG, "Successfully uploaded polls to server " +
                            "(serverAnswer: {0})", serverAnswer);
                    Logger.td(SyncService.this, SyncService.TAG + ": " +
                            "uploaded polls (serverAnswer: {0})",
                            serverAnswer);

                    Logger.d(TAG, "Removing uploaded polls from db");
                    pollsStorage.remove(pollsWrap.getDatas());
                } else {
                    Logger.w(TAG, "Error while uploading polls to server");
                }
            }

        };

        // Sign our data to identify us, and upload
        Logger.d(TAG, "Signing data and launching polls sync");
        serverTalker.signAndPostResult(json.toJsonExposed(pollsWrap),
                callback);
    }

    /**
     * Upload collected {@link LocationPoint}s to the server and remove
     * them from local storage, asynchronously.
     */
    private void asyncUploadLocationPoints() {
        Logger.d(TAG, "Syncing locationPoints");

        // Do we have any location points to upload?
        ArrayList<LocationPoint> uploadableLocationPoints =
                locationPointsStorage.getUploadableLocationPoints();
        if (uploadableLocationPoints == null) {
            Logger.i(TAG, "No locationPoints to upload -> exiting");
            Logger.td(this, TAG + ": no locationItems to upload");
            return;
        }

        // Wrap uploadable location points in a single structure to provide
        // a root node when jsonifying.
        final ResultsWrapper<LocationPoint> locationPointsWrap =
                locationPointsWrapperFactory.create(uploadableLocationPoints);

        // Called when the HttpPostTask finishes or times out
        HttpConversationCallback callback = new HttpConversationCallback() {

            private final String TAG =
                    "LocationPoints HttpConversationCallback";

            @Override
            public void onHttpConversationFinished(boolean success,
                                                   String serverAnswer) {
                Logger.d(TAG, "LocationPoints HttpConversation finished");

                // If at this point, the app has changed from one of test/prod modes
                // to the other between the POST start and finish, the locationPoints we're about
                // to remove from the database are probably already gone. The app shouldn't
                // crash, but if it does, we should be able to see what happened in the
                // ACRA logs.

                if (success) {
                    // TODO: handle the case where returned JSON is in fact an error.
                    Logger.i(TAG, "Successfully uploaded locationPoints to " +
                            "server (serverAnswer: {0})", serverAnswer);
                            Logger.td(SyncService.this,
                                    SyncService.TAG + ": uploaded " +
                                            "locationPoints (serverAnswer: " +
                                            "{0})", serverAnswer);

                    Logger.d(TAG, "Removing uploaded locationPoints from " +
                            "db");
                    locationPointsStorage.remove(
                            locationPointsWrap.getDatas());
                } else {
                    Logger.w(TAG, "Error while uploading locationPoints to " +
                            "server");
                }
            }

        };

        // Sign our data to identify us, and upload
        Logger.d(TAG, "Signing data and launching locationPoints sync");
        serverTalker.signAndPostResult(json.toJsonExposed(locationPointsWrap),
                callback);
    }

    private void asyncPutProfile() {
        Logger.d(TAG, "Syncing profile data");

        profileStorage.setSyncStart();
        ProfileWrapper profileWrap = profileStorage.getProfile().buildWrapper();

        // Called when the HttpPutTask finishes or times out
        HttpConversationCallback callback = new HttpConversationCallback() {

            private final String TAG =
                    "PutProfile HttpConversationCallback";

            @Override
            public void onHttpConversationFinished(boolean success,
                                                   String serverAnswer) {

                Logger.d(TAG, "PutProfile HttpConversation finished");

                // Exit if app mode has changed before we could finish PUTing the profile
                if (! statusManager.getCurrentModeName().equals(startSyncAppMode)) {
                    Logger.i(TAG, "App mode has changed from {0} to {1} since sync started, "
                            + "aborting profile put.", startSyncAppMode,
                            statusManager.getCurrentModeName());
                    return;
                }

                if (success) {
                    // TODO: handle the case where returned JSON is in fact an error.
                    Logger.i(TAG, "Successfully uploaded Profile to " +
                            "server (serverAnswer: {0})", serverAnswer);
                    Logger.td(SyncService.this,
                            SyncService.TAG + ": uploaded " +
                                    "profile (serverAnswer: " +
                                    "{0})", serverAnswer);

                    if (profileStorage.hasChangedSinceSyncStart()) {
                        Logger.d(TAG, "Profile has changed since sync start " +
                                "-> not clearing isDirty flag");
                    } else {
                        Logger.d(TAG, "Profile untouched since sync " +
                                "start -> clearing isDirty flag");
                        profileStorage.clearIsDirtyAndCommit();
                    }
                } else {
                    Logger.w(TAG, "Error while uploading profile to " +
                            "server");
                }

            }

        };

        // Sign our data to identify us, and upload
        Logger.d(TAG, "Signing data and launching profile update");
        serverTalker.signAndPutProfile(json.toJsonExposed(profileWrap),
                callback);
    }

}
