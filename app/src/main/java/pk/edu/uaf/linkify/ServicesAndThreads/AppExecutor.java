package pk.edu.uaf.linkify.ServicesAndThreads;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor  {
    private Executor singleThreadExecutor;
    private Executor mainThread;
    private static AppExecutor appExecutor;
    private Executor networkExecutor;
    public static AppExecutor getInstance(){
        if (appExecutor == null){
            appExecutor = new AppExecutor();
        }
        return appExecutor;
    }
    private AppExecutor(){
        singleThreadExecutor =  Executors.newSingleThreadExecutor();
        networkExecutor =  Executors.newFixedThreadPool(3);
        mainThread = new MainThreadExecutor();

    }

    public Executor getSingleThreadExecutor() {
        return singleThreadExecutor;
    }
    public Executor getNetworkExecutor(){  return networkExecutor; }

    public Executor getMainThread() {
        return mainThread;
    }


    private static  class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler= new Handler(Looper.getMainLooper());
        @Override
        public void execute(Runnable command) {
                mainThreadHandler.post(command);
        }
    }
}
