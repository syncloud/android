package org.syncloud.android.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.syncloud.android.R;

public class CommunicationDialog extends AlertDialog {
    private ProgressBar progress;
    private Context context;
    private TextView messageView;
    private CharSequence message;
    private Button reportBtn;
    private CharSequence title;
    private String emailErrorBoundary = "========= ERROR =========";

    public CommunicationDialog(Context context) {
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


    public void setError(String error) {
        setMessage(error);
        enableErrorButton();
        setCancelable(true);
        progress.setVisibility(View.INVISIBLE);
    }

    public void show(String message) {
        setTitle(message);
        setMessage("");
        setCancelable(false);
        show();
    }
}