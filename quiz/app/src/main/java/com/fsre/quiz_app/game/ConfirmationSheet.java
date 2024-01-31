package com.fsre.quiz_app.game;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import com.fsre.quiz_app.R;

public class ConfirmationSheet extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_sheet, container, false);

        // Find the Button
        ImageView button = view.findViewById(R.id.confirmation_sheet_button);

        // Set OnClickListener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger the function in the activity
                if (getActivity() instanceof Game) {
                    ((Game) getActivity()).onConfirmationSheetButtonClick();
                }
            }
        });
        return view;
    }
}
