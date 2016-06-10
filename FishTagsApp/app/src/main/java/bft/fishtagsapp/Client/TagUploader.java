package bft.fishtagsapp.Client;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.HashMap;

/**
 * TagUploader will receive data in the form of a hasmap from each tag report submission and attempt to upload it to the database.
 * And IntentService was chosen for this purpose because the uploads do not have to be uploaded in parallel and therefore simply
 * need to be uploaded one after the other. The IntentService also terminates when it itself done, independent of the lifecycle
 * of the activity that calls it, ensuring that the uploading will continue until all pending requests are submitted.
 */
public class TagUploader extends IntentService {
    private static final String ACTION_UPLOAD = "Upload";

    public TagUploader() {
        super("TagUploader");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionUpload(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TagUploader.class);
        intent.setAction(ACTION_UPLOAD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                final HashMap<String, String> map = (HashMap<String, String>) intent.getSerializableExtra("map");
                handleActionUpload(map);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(HashMap<String, String> map) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
