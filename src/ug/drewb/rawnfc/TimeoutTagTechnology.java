package ug.drewb.rawnfc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class TimeoutTagTechnology extends TagTechnology
{
    int timeout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeout_tag_technology_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_timeout:
                openTimeout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openTimeout()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
            .setTitle("Set Timeout");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (timeout != 0) {
            input.setText(Integer.toString(timeout));
        } else {
            input.setHint("200 milliseconds");
        }
        alert.setView(input);

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                timeout = Integer.parseInt(input.getText().toString());
            }
        });

        alert.show();
    }
}
