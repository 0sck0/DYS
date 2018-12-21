package com.koreatech.dys.dys;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class DatePickerDialogFragment extends DialogFragment {

    private static final String ARGUMENT_YEAR = "ARGUMENT_YEAR";
    private static final String ARGUMENT_MONTH = "ARGUMENT_MONTH";
    private static final String ARGUMENT_DATE = "ARGUMENT_DATE";
    public DatePickerDialog.OnDateSetListener listener;

    private int year;
    private int month;
    private int date;

    public static DatePickerDialogFragment newInstance(final int year, final int month, final int date){
        final DatePickerDialogFragment df = new DatePickerDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARGUMENT_YEAR, year);
        args.putInt(ARGUMENT_MONTH, month);
        args.putInt(ARGUMENT_DATE, date);
        df.setArguments(args);
        return df;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveArguments();
    }

    private void retrieveArguments() {
        final Bundle args = getArguments();
        if (args != null) {
            year = args.getInt(ARGUMENT_YEAR);
            month = args.getInt(ARGUMENT_MONTH);
            date = args.getInt(ARGUMENT_DATE);
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new DatePickerDialog(getContext(), this.listener, this.year, this.month, this.date);
    }

    public void setListener(final DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }
}
