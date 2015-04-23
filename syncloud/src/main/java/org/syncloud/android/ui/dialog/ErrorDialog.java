package org.syncloud.android.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;

public class ErrorDialog extends AlertDialog {
    private final SyncloudApplication application;
    private Activity context;
    private String message;

    public ErrorDialog(Activity context, String message) {
        super(context);
        this.context = context;
        application = (SyncloudApplication) context.getApplication();
        this.message = message;
        this.setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_error, null);

        TextView viewMessage = (TextView) view.findViewById(R.id.view_message);
        viewMessage.setText(message);

        Button btnReport = (Button) view.findViewById(R.id.btn_report);
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportError();
            }
        });

        setView(view);
        super.onCreate(savedInstanceState);
    }

    public void reportError() {
        application.reportError();
    }
}
