package pk.edu.uaf.linkify.Fragments;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Adapter.ChatsAdapter;
import pk.edu.uaf.linkify.ChatActivity;
import pk.edu.uaf.linkify.HomeActivity.MainViewModel;
import pk.edu.uaf.linkify.Modal.CustomResponse;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.R;

import static pk.edu.uaf.linkify.Utils.AppConstant.ACTION_CONNECT_INFO;

public class ChatsFragment extends Fragment {

    @BindView(R.id.recyclerView_chats_id)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view_container)
    ConstraintLayout emptyView;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    public List<CustomResponse> list;
    public View view;
    private ChatsAdapter adapter;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.chats_fragment, container, false);
        ButterKnife.bind(this, view);
        list = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChatsAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        setUpViewModel();
        if (!list.isEmpty()) {
            animationView.pauseAnimation();
            emptyView.setVisibility(View.GONE);
        }
    }

    private void setUpViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAllChats().observe(this, linkifyChats -> {
            list = linkifyChats;
            if (!list.isEmpty()) {
                animationView.pauseAnimation();
                emptyView.setVisibility(View.GONE);
            }
            adapter.updateDataSet(linkifyChats);
            adapter.notifyDataSetChanged();

        });
    }

    public void onNewChat(NsdServiceInfo info, LinkifyUser user) {
        long chatId = -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<CustomResponse> result = list.stream()
                    .filter(a -> Objects.equals(a.getUser().getId(), user.getId()))
                    .collect(Collectors.toList());
            if (!result.isEmpty()) {
                chatId = result.get(0).getChat().getChatId();
            }
        } else {

            for (CustomResponse response : list) {
                if (response.getUser().getId().equals(user.getId())) {
                    chatId = response.getChat().getChatId();
                }
            }
        }
        if (chatId == -1) {
            viewModel.insertUser(user);
            long id = viewModel.insertChat(new LinkifyChat(user.getId(), new Date(), 0, ""));
            if (id != -1) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.setAction(ACTION_CONNECT_INFO);
                intent.putExtra("id",id);
                intent.putExtra("userId",user.getId());
                intent.putExtra("info", info);
                intent.putExtra("name",user.getName());
                intent.putExtra("avatar",user.getAvatar());
                startActivity(intent);
            }
        }else {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.setAction(ACTION_CONNECT_INFO);
            intent.putExtra("id",chatId);
            intent.putExtra("userId",user.getId());
            intent.putExtra("info", info);
            intent.putExtra("name",user.getName());
            intent.putExtra("avatar",user.getAvatar());
            startActivity(intent);
        }
    }
}
