package pk.edu.uaf.linkify.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Adapter.CallsAdapter;
import pk.edu.uaf.linkify.CallsViewModel;
import pk.edu.uaf.linkify.Modal.CallCustomResponse;
import pk.edu.uaf.linkify.R;


public class CallsFragment extends Fragment {
    View view;
    private CallsViewModel viewModel;
    private List<CallCustomResponse> list;
    @BindView(R.id.empty_view_container)
    ConstraintLayout emptyView;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    private CallsAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.calls_fragment, container, false);
        ButterKnife.bind(this, view);
        list = new ArrayList<>();
        adapter = new CallsAdapter(list);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        setUpViewModel();
        return view;
    }

    private void setUpViewModel() {
        viewModel = ViewModelProviders.of(this).get(CallsViewModel.class);
        viewModel.getAllCalls().observe(this, callCustomResponses -> {
            list = callCustomResponses;
            if (!list.isEmpty()) {
                animationView.pauseAnimation();
                emptyView.setVisibility(View.GONE);
                Log.d("nhhhhh", "setUpViewModel: "+list.get(0).getCalls().getUserId());
            }
            adapter.updateDataSet(list);
            adapter.notifyDataSetChanged();
        });
    }
}
