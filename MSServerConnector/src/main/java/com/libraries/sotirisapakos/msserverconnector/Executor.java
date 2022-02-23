package com.libraries.sotirisapakos.msserverconnector;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
            new Handler(Looper.getMainLooper()).post(() -> onPostExecute(executors));
        });
    }

    public void execute() {
        startBackground();
    }
    public boolean isShutdown() { return executors.isShutdown(); }
    public void shutdown(){
        executors.shutdown();
    }

    public abstract void onPreExecute();
    public abstract void doInBackground();

    /**
     * Pass {@link ExecutorService} to {@code onPostExecute()} method
     * enable {@code shutdown()} functionality to Helper classes
     * @param executor {@link ExecutorService} class
     */
    public abstract void onPostExecute(ExecutorService executor);

}
