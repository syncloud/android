package org.syncloud.android.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.syncloud.android.Progress;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;

public class CommunicationDialog extends AlertDialog implements Progress {
    private final SyncloudApplication application;
    private ProgressBar progress;
    private Activity context;
    private TextView messageView;
    private CharSequence message;
    private Button reportBtn;

    public CommunicationDialog(Activity context) {
        super(context);
        this.context = context;
        application = (SyncloudApplication) context.getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_communication, null);
        progress = (ProgressBar) view.findViewById(R.id.communication_progress);
        messageView = (TextView) view.findViewById(R.id.message);
        reportBtn = (Button) view.findViewById(R.id.report_button);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportError();
            }
        });
        setView(view);

        if (message != null)
            setMessage(message);
        setCancelable(false);
        progress.setVisibility(View.VISIBLE);
        reportBtn.setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMessage(CharSequence text) {
        message = text;
        if (messageView != null)
            messageView.setText(text);
    }

    public void reportError() {
        application.reportError();
    }


    private void setError(String error) {
        setMessage(error);
        progress.setVisibility(View.INVISIBLE);
        reportBtn.setVisibility(View.VISIBLE);
        setCancelable(true);
    }

    public void start() {
        setMessage("Connecting to the device");
        setCancelable(false);
        show();
        progress.setVisibility(View.VISIBLE);
        reportBtn.setVisibility(View.GONE);
    }

    public void stop() {
        hide();
    }

    public void error(final String error) {
        setError(error);    }

    public void title(final String message) {
        setMessage(message);
    }

    public void progress(final String progress) {
        setMessage(message + "\n" + progress);
    }
}