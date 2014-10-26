package org.syncloud.android.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.common.progress.Progress;

public class CommunicationDialog extends AlertDialog implements Progress {
    private ProgressBar progress;
    private Activity context;
    private TextView messageView;
    private CharSequence message;
    private Button reportBtn;
    private CharSequence title;
    private String emailErrorBoundary = "========= ERROR =========";

    public CommunicationDialog(Activity context) {
        super(context);
        this.context = context;
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
                reportError(view);
            }
        });
        setView(view);

        if (message != null)
            setMessage(message);
        setCancelable(false);
        progress.setVisibility(View.VISIBLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMessage(CharSequence text) {
        message = text;
        if (messageView != null)
            messageView.setText(text);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        super.setTitle(title);
    }

    public void enableErrorButton() {
        reportBtn.setVisibility(View.VISIBLE);
    }


    public void reportError(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"syncloud@syncloud.it"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Syncloud error report: " + title);
        CharSequence body = emailErrorBoundary + '\n' + message + '\n' + emailErrorBoundary + '\n';
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(intent);
    }


    private void setError(String error) {
        progress(error);
        enableErrorButton();
        setCancelable(true);
        progress.setVisibility(View.INVISIBLE);
    }

    public void start() {
        setTitle("Connecting to the device");
        setMessage("");
        setCancelable(false);
        show();
    }

    public void stop() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        });
    }

    @Override
    public void error(final String error) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setError(error);
            }
        });
    }

    @Override
    public void title(final String message) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(message);
            }
        });
    }

    @Override
    public void progress(final String progress) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMessage(message + "\n" + progress);
            }
        });
    }
}