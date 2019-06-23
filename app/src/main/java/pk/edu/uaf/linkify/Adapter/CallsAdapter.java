package pk.edu.uaf.linkify.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pk.edu.uaf.linkify.R;


public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.CallsViewHolder> {

    private String[] data;
    //create constructor
    public CallsAdapter(String[] data) {
        this.data = data;
    }
    //implement methods sy ye aa jaye ga baqi changes phr kry
    @NonNull
    @Override
    public CallsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.callsitem,viewGroup,false);
        return new CallsViewHolder(view);
    }
    //implement methods sy ye aa jaye ga baqi changes phr kry
    @Override
    public void onBindViewHolder(@NonNull CallsViewHolder holder, int position) {
        String title = data[position];
        holder.textTitle.setText(title);

    }
    //implement methods sy ye aa jaye ga baqi changes phr kry
    @Override
    public int getItemCount() {
        //yahan zero tha wo badal k data ki lenght jitni hai wo btai
        return data.length;
    }
    //ye aik nested class bnai hai jo data ko hold krti hai
    public class CallsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon,arrow,call;
        TextView textTitle,txtdetails;
       // View view;
        public CallsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.user_dp);
            arrow = itemView.findViewById(R.id.incoming_calls);
            call = itemView.findViewById(R.id.call);
            textTitle = itemView.findViewById(R.id.nameIncommingCall);
            txtdetails = itemView.findViewById(R.id.detail);
            //view = itemView.findViewById(R.id.divider);

        }
    }


}

