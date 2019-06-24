package pk.edu.uaf.linkify.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.nsd.NsdServiceInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.R;

import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getName;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getSurname;

public class MyServicesRecyclerAdapter extends RecyclerView.Adapter<MyServicesRecyclerAdapter.MyViewHolder> {

    private List<NsdServiceInfo> myServices;
    private ClickListener mListener;
    private Context mContext;

    public interface ClickListener {
        void ItemClickListener(NsdServiceInfo info, LinkifyUser user);
    }

    public MyServicesRecyclerAdapter(List<NsdServiceInfo> myServices, Context context) {
        mContext = context;
        mListener = (ClickListener) context;
        this.myServices = myServices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_found_services, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final NsdServiceInfo info = myServices.get(i);
        String name = info.getServiceName();
        String[] nameParts = name.split("/");
        GradientDrawable magnitudeCircle = (GradientDrawable) myViewHolder.circle.getBackground();
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        magnitudeCircle.setColor(color);
        String builder = getName(nameParts[0]).substring(0, 1) +
                getSurname(nameParts[0]).substring(0, 1);
        myViewHolder.circle.setText(builder);
        myViewHolder.userName.setText(nameParts[0]);
        myViewHolder.userNumber.setText(nameParts[1]);
        myViewHolder.container.setOnClickListener(v -> {
            LinkifyUser user = new LinkifyUser(nameParts[2],nameParts[0],builder,nameParts[1]);
            mListener.ItemClickListener(info, user);
        });
    }

    @Override
    public int getItemCount() {
        return myServices.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userNumber;
        TextView circle;
        LinearLayout container;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userNumber = itemView.findViewById(R.id.userNumber);
            circle = itemView.findViewById(R.id.circleTextViewIncomingCall);
            container = itemView.findViewById(R.id.container);
        }
    }
}
