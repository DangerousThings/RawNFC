package ug.drewb.rawnfc;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

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
}
