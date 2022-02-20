package com.libraries.sotirisapakos.msserverconnector;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Class to implement any background task using {@link ExecutorService ExecutorService} class.</p>
 * <p>This style of implementation refer to {@link android.os.AsyncTask AsyncTask} abstract class
 * implementation.</p>
 * <p><b>Simple</b> and <i>straight forward!</i></p>
 */
public abstract class Executor {

    private final ExecutorService executors;

    public Executor() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {
        onPreExecute();
        executors.execute(() -> {
            doInBackground();
            new Handler(Looper.getMainLooper()).post(this::onPostExecute);
        });
    }

    public void execute() {
        startBackground();
    }

    public abstract void onPreExecute();
    public abstract void doInBackground();
    public abstract void onPostExecute();

}
