package ug.drewb.rawnfc;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final String[] values = new String[] {"NFC-A (ISO 14443-3A)", "NFC-B (ISO 14443-3B)", "NFC-F (JIS 6319-4)", "NFC-V (ISO 15693)", "ISO-DEP (ISO 14443-4)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Class[] values = new Class[] {NfcAActivity.class, NfcBActivity.class, NfcFActivity.class, NfcVActivity.class, IsoDepActivity.class};
        Intent intent = new Intent(this, values[position]);
        startActivity(intent);
    }
}
