package pk.edu.uaf.linkify.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import pk.edu.uaf.linkify.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OutGoingCallFragment.OnOutDropClickListner} interface
 * to handle interaction events.
 * Use the {@link OutGoingCallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutGoingCallFragment extends Fragment {

    private OnOutDropClickListner mListener;

    public OutGoingCallFragment() {
        // Required empty public constructor
    }


    public static OutGoingCallFragment newInstance() {
        OutGoingCallFragment fragment = new OutGoingCallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.outgoing_call,container,false);
        view.findViewById(R.id.drop).setOnClickListener(v -> {
            mListener.onOutDrop();
        });
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOutDropClickListner) {
            mListener = (OnOutDropClickListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnOutDropClickListner {
        void onOutDrop();
    }
}
