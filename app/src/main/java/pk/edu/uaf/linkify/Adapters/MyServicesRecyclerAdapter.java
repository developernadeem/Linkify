package pk.edu.uaf.linkify.Adapters;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pk.edu.uaf.linkify.R;

public class MyServicesRecyclerAdapter extends RecyclerView.Adapter<MyServicesRecyclerAdapter.MyViewHolder> {

    List<NsdServiceInfo> myServices;
    private  ClickListener mListener;
    public interface ClickListener{
        void ItemClickListener(NsdServiceInfo info);
    }

    public MyServicesRecyclerAdapter(List<NsdServiceInfo> myServices , Context context) {
        mListener = (ClickListener)context;
        this.myServices = myServices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_found_services,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final NsdServiceInfo info = myServices.get(i);
        myViewHolder.port.setText(String.valueOf(info.getPort()));
        myViewHolder.ipAddress.setText(info.getHost().toString());
        myViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ItemClickListener(info);
                /*Thread msg = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Socket mSocket = new Socket(info.getHost(),info.getPort());
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(mSocket.getOutputStream())),
                                    true);
                            out.println("You Got A message from Nadeem. Happy Coding!");
                            out.close();
                            mSocket.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                msg.start();*/
            }
        });
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
        ConstraintLayout container;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ipAddress = itemView.findViewById(R.id.ip_address);
            port = itemView.findViewById(R.id.port_number);
            container = itemView.findViewById(R.id.container);
        }
    }
}
