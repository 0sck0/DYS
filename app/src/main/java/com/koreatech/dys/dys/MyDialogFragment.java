package com.koreatech.dys.dys;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class MyDialogFragment extends DialogFragment {

    private MyDialogListener myListener;

    public interface MyDialogListener {
        public void myCallback(String cityName);
    }

    public MyDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            myListener = (MyDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    @Override
    public void onAttach(Activity activity) {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
//        builder.setView(inflater.inflate(R.layout.fragment_dialog, null))
//                .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        EditText edCityName = (EditText)getDialog().findViewById(R.id.city_name);
//                        String cityName = edCityName.getText().toString();
//                        myListener. myCallback(cityName);
//                    }
//                });

        return builder.create();
    }
}
