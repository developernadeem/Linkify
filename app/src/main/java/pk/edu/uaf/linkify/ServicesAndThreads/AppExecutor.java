package pk.edu.uaf.linkify.ServicesAndThreads;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor  {
    private ExecutorService singleThreadExecutor;
    private Executor mainThread;
    private static AppExecutor appExecutor;
    private ExecutorService networkExecutor;
    public static AppExecutor getInstance(){
        if (appExecutor == null){
            appExecutor = new AppExecutor();
        }
        return appExecutor;
    }
    private AppExecutor(){
        singleThreadExecutor =  Executors.newFixedThreadPool(3);
        networkExecutor =  Executors.newFixedThreadPool(3);
        mainThread = new MainThreadExecutor();

    }

    public ExecutorService getSingleThreadExecutor() {
        return singleThreadExecutor;
    }
    public ExecutorService getNetworkExecutor(){  return networkExecutor; }

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
