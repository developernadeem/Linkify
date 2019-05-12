package pk.edu.uaf.linkify.Fragments;


import android.app.Dialog;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Adapter.MyServicesRecyclerAdapter;
import pk.edu.uaf.linkify.R;

/**
 * @author Muhammad Nadeem
 * @Date 5/12/2019.
 */
public class PeersDialog extends DialogFragment  {
    private static final String TAG = "PeersDialog";
    private OnPeerItemClickListener mListener;

    public interface OnPeerItemClickListener {
        void onPeerClick(NsdServiceInfo info, int which);
    }


    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    NsdManager.DiscoveryListener mDiscoveryListener;
    List<NsdServiceInfo> serviceInfos = new ArrayList<>();
    private NsdManager mNsdManager;
    private MyServicesRecyclerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.activity_discover_service, container, false);
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

    /**
     * The system calls this only when creating the layout in a dialog.
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

                /*if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }*/
                        //mService = serviceInfo;
                        serviceInfos.add(serviceInfo);
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());


                    }
                });
                /*if (!service.getServiceType().equals("_http._tcp.")) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals("Nadeem Chat")) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + "Nadeem Chat");
                } else if (service.getServiceName().contains("Nadeem Chat")){

                }*/
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
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

