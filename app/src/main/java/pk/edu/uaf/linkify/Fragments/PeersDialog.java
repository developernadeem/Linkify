package pk.edu.uaf.linkify.Fragments;


import android.app.Dialog;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Adapter.MyServicesRecyclerAdapter;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;

/**
 * @author Muhammad Nadeem
 * @Date 5/12/2019.
 */
public class PeersDialog extends DialogFragment {
    private static final String TAG = "PeersDialog";
    private OnPeerItemClickListener mListener;

    public interface OnPeerItemClickListener {
        void onPeerClick(NsdServiceInfo info, int which);
    }


    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.lottie_view)
    LottieAnimationView lottieView;
    NsdManager.DiscoveryListener mDiscoveryListener;
    List<NsdServiceInfo> serviceInfos = new ArrayList<>();
    private NsdManager mNsdManager;
    private MyServicesRecyclerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.discover_service_fragment, container, false);
        ButterKnife.bind(this, v);
        mNsdManager = (NsdManager) getContext().getSystemService(Context.NSD_SERVICE);
        adapter = new MyServicesRecyclerAdapter(serviceInfos, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                "_http._tcp.", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler().postDelayed(() -> {
            if (serviceInfos.isEmpty()) {
                lottieView.setAnimation("no_nearby.json");
                lottieView.playAnimation();
            }
        }, 10000);
    }

    /**
     * The system calls_fragment this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        // Called when the resolve fails. Use the error code to debug.
                        Log.e(TAG, "Resolve failed: " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                        if (serviceInfo.getServiceName().equals(PrefUtils.getStringPref(getContext(), USER_SERVICE_NAME))) {
                            Log.d(TAG, "Same IP.");
                            return;
                        }
                        //mService = serviceInfo;
                        boolean add = true;
                        for (NsdServiceInfo info : serviceInfos) {
                            if (info.getServiceName().equals(serviceInfo.getServiceName())){
                                add = false;
                            }
                        }
                        if (add) {

                            serviceInfos.add(serviceInfo);
                            AppExecutor.getInstance().getMainThread().execute(() -> {
                                switch (lottieView.getVisibility()) {
                                    case View.VISIBLE:
                                        recyclerView.setVisibility(View.VISIBLE);
                                        lottieView.pauseAnimation();
                                        lottieView.setVisibility(View.GONE);

                                    case View.GONE:
                                        break;
                                    case View.INVISIBLE:
                                        break;
                                }
                                adapter.notifyDataSetChanged();
                            });
                        }


                    }
                });

            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                for (int i = 0; i < serviceInfos.size(); i++) {
                    NsdServiceInfo serviceInfo = serviceInfos.get(i);
                    if (serviceInfo.getServiceName().equals(service.getServiceName())){
                        serviceInfos.remove(i);
                        AppExecutor.getInstance().getMainThread().execute(() -> adapter.notifyDataSetChanged());
                        return;
                    }
                }

                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
}

