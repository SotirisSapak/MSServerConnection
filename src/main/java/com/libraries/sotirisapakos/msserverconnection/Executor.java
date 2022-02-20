package com.libraries.sotirisapakos.msserverconnection;

import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

public abstract class Executor {

    private final ExecutorService executors;

    public Executor() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {onPreExecute();
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
