package pk.edu.uaf.linkify.Adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import pk.edu.uaf.linkify.Modal.CallCustomResponse;
import pk.edu.uaf.linkify.R;

import static pk.edu.uaf.linkify.Utils.AppConstant.INCOMING;
import static pk.edu.uaf.linkify.Utils.AppConstant.INCOMING_MISSED;
import static pk.edu.uaf.linkify.Utils.AppConstant.INCOMING_REJECTED;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_DECLINE;


public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.CallsViewHolder> {

    private List<CallCustomResponse> callList;

    //create constructor
    public CallsAdapter(List<CallCustomResponse> callList) {
        this.callList = callList;
    }
    public void updateDataSet(List<CallCustomResponse> calls) {
        callList = calls;
    }
    //implement methods sy ye aa jaye ga baqi changes phr kry
    @NonNull
    @Override
    public CallsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_call, viewGroup, false);
        return new CallsViewHolder(view);
    }

    //implement methods sy ye aa jaye ga baqi changes phr kry
    @Override
    public void onBindViewHolder(@NonNull CallsViewHolder holder, int position) {
        CallCustomResponse response = callList.get(position);
        holder.textTitle.setText(response.getUser().getName());
        holder.avatar.setText(response.getUser().getAvatar());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        holder.txtdetails.setText(formatter.format(response.getCalls().getDate()));
        GradientDrawable magnitudeCircle = (GradientDrawable) holder.avatar.getBackground();
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        magnitudeCircle.setColor(color);
        if (response.getCalls().isType()) {
            holder.callType.setImageResource(R.drawable.video_call);
        } else {
            holder.callType.setImageResource(R.drawable.call);
        }
        switch (response.getCalls().getStatus()) {
            case OUT_GOING:
                holder.imgIcon.setImageResource(R.drawable.ic_call_made);
                break;
            case OUT_GOING_DECLINE:
                holder.imgIcon.setImageResource(R.drawable.ic_call_made);
                break;
            case INCOMING:
                holder.imgIcon.setImageResource(R.drawable.callarrow);
                break;

            case INCOMING_REJECTED:
                holder.imgIcon.setImageResource(R.drawable.ic_rejected);
                break;
            case INCOMING_MISSED:
                holder.imgIcon.setImageResource(R.drawable.ic_call_missed);
                break;
        }

    }

    //implement methods sy ye aa jaye ga baqi changes phr kry
    @Override
    public int getItemCount() {
        //yahan zero tha wo badal k data ki lenght jitni hai wo btai
        return callList.size();
    }

    //ye aik nested class bnai hai jo data ko hold krti hai
    public class CallsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, callType;
        TextView textTitle, txtdetails, avatar;

        // View view;
        public CallsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.call_status);
            avatar = itemView.findViewById(R.id.incoming_calls);
            callType = itemView.findViewById(R.id.call_type);
            textTitle = itemView.findViewById(R.id.nameIncommingCall);
            txtdetails = itemView.findViewById(R.id.detail);
            //view = itemView.findViewById(R.id.divider);

        }
    }


}

