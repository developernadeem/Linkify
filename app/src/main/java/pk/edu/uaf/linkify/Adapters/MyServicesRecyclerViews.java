package pk.edu.uaf.linkify.Adapters;

import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.List;

import pk.edu.uaf.linkify.R;

public class MyServicesRecyclerViews extends RecyclerView.Adapter<MyServicesRecyclerViews.MyViewHolder> {

    List<NsdServiceInfo> myServices;

    public MyServicesRecyclerViews(List<NsdServiceInfo> myServices) {
        this.myServices = myServices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_found_services,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        NsdServiceInfo info = myServices.get(i);
        myViewHolder.port.setText(String.valueOf(info.getPort()));
        myViewHolder.ipAddress.setText(info.getHost().toString());
//        int port = serviceInfo.getPort();
//        InetAddress host = serviceInfo.getHost();

    }

    @Override
    public int getItemCount() {
        return myServices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView ipAddress;
        TextView port;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ipAddress = itemView.findViewById(R.id.ip_address);
            port = itemView.findViewById(R.id.port_number);
        }
    }
}
