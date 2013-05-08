package com.brainydroid.daydreaming.db;

import android.util.Log;
import com.brainydroid.daydreaming.ui.Config;
import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.lang.reflect.Type;

public class QuestionDetailsDeserializer
        implements JsonDeserializer<IQuestionDetails> {

    @SuppressWarnings("FieldCanBeLocal")
    private static String TAG = "QuestionDetailsDeserializer";

    @SuppressWarnings("FieldCanBeLocal")
    private static String QUESTION_DETAILS_SUFFIX = "QuestionDetails";

    @Override
    public IQuestionDetails deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context)
            throws JsonParseException {

        // Debug
        if (Config.LOGD) {
            Log.d(TAG, "[fn] deserialize");
        }

        String PACKAGE_PREFIX = getClass().getPackage().getName() + ".";
        try {
            JsonObject obj = (JsonObject)json;
            Class klass = Class.forName(PACKAGE_PREFIX +
                    obj.get("type").getAsString() + QUESTION_DETAILS_SUFFIX);
            return context.deserialize(json, klass);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

}
