package pk.edu.uaf.linkify.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.SignUpActivity;
import pk.edu.uaf.linkify.Utils.UtilsFunctions;

public class PermissionFragment extends Fragment {


    public PermissionFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.permission_layout,container,false);
      view.findViewById(R.id.btn_all_permissions).setOnClickListener(v -> {

          UtilsFunctions.requestPermissions(getActivity());

      });
      return view;
    }

    // TODO: Rename method, update argument and hook method into UI event




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

}
