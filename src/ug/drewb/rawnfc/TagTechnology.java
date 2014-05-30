package ug.drewb.rawnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;

public class TagTechnology extends Activity
{
    LinearLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_technology);

        layout = (LinearLayout) findViewById(R.id.layout);

        findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layout.getChildCount() > 3) {
                    layout.removeViewAt(layout.getChildCount()-3);
                }
            }
        });

        findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText input = new EditText(v.getContext());
                input.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                layout.addView(input, layout.getChildCount()-2);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        NfcAdapter.getDefaultAdapter().enableForegroundDispatch(this, pending, null, null);
    }

    public byte[][] getCommands()
    {
        byte[][] commands = new byte[layout.getChildCount()-2][];
        for (int i = 0; i < commands.length; i++) {
            EditText input = (EditText) layout.getChildAt(i);
            commands[i] = HexUtils.hexToBytes(input.getText().toString());
        }
        return commands;
    }

    public void showResponses(byte[][] bytes)
    {
        String[] strings = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            strings[i] = HexUtils.bytesToHex(bytes[i]);
        }

        AlertDialog alert = new AlertDialog.Builder(this).setCancelable(true).create();
        alert.setMessage(TextUtils.join("\n", strings));
        alert.show();
    }

    public void showException(IOException e)
    {
        AlertDialog alert = new AlertDialog.Builder(this).setCancelable(true).create();
        alert.setMessage(e.toString());
        alert.show();
    }
}
