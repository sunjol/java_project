package protect.budgetwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;

class DatabaseCleanupTask  extends AsyncTask<Void, Void, Void>
{
    private static final String TAG = "BudgetWatch";

    private final Activity activity;
    private final Long receiptPurgeCutoff;

    private ProgressDialog progress;

    public DatabaseCleanupTask(Activity activity)
    {
        super();
        this.activity = activity;
        receiptPurgeCutoff = null;
    }

    public DatabaseCleanupTask(Activity activity, long receiptPurgeCutoff)
    {
        super();
        this.activity = activity;
        this.receiptPurgeCutoff = receiptPurgeCutoff;
    }

    protected void onPreExecute()
    {
        progress = new ProgressDialog(activity);
        progress.setTitle(R.string.cleaning);

        progress.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                DatabaseCleanupTask.this.cancel(true);
            }
        });

        progress.show();
    }







    protected Void doInBackground(Void... nothing)
    {
        DBHelper db = new DBHelper(activity);



        db.close();

        return null;
    }

    protected void onPostExecute(Void result)
    {
        progress.dismiss();
        Log.i(TAG, "Cleanup Complete");
    }

    protected void onCancelled()
    {
        progress.dismiss();
        Log.i(TAG, "Cleanup Cancelled");
    }
}
