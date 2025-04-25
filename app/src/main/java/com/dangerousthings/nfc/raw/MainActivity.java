package com.dangerousthings.nfc.raw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements NfcAdapter.ReaderCallback {

    private static final String[] BASE_TECHNOLOGIES = {
        "NFC-A (ISO 14443-3A)",
        "NFC-B (ISO 14443-3B)",
        "NFC-F (JIS 6319-4)",
        "NFC-V (ISO 15693)",
        "ISO-DEP (ISO 14443-4)"
    };
    private static final String[] MIFARE_TECHNOLOGIES = {
        "MIFARE Classic",
        "MIFARE Ultralight"
    };
    private static final String[] TECH_CLASS_SIMPLE_NAMES = {
        "NfcA",
        "NfcB",
        "NfcF",
        "NfcV",
        "IsoDep",
        "MifareClassic",
        "MifareUltralight"
    };

    private List<String> technologies;
    private Integer selectedTechnology;
    private EditText inputView;
    private EditText outputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_info_details);

        inputView = findViewById(R.id.input);
        outputView = findViewById(R.id.output);
        inputView.addTextChangedListener(new HexTextWatcher());

        outputView.setOnLongClickListener(v -> {
            shareDump();
            return true;
        });

        initializeTechnologies();

        handleIntent(getIntent());

        if (selectedTechnology == null) {
            showTechnologySelectionDialog();
        }

        enableNfcReaderMode();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            Uri uri = intent.getData();
            if ("dnfc".equals(uri.getScheme())) {
                int techIndex = Arrays.asList(TECH_CLASS_SIMPLE_NAMES).indexOf(uri.getHost());
                if (techIndex != -1) {
                    selectedTechnology = techIndex;
                }
                String path = uri.getPath();
                if (path != null) {
                    String[] pathParts = path.split("/+");
                    for (int i = 1; i < pathParts.length; i++) {
                        inputView.append(i < pathParts.length - 1 ? pathParts[i] + "\n" : pathParts[i]);
                    }
                }
            }
        }
    }

    private void initializeTechnologies() {
        technologies = new ArrayList<>(Arrays.asList(BASE_TECHNOLOGIES));
        if (getPackageManager().hasSystemFeature("com.nxp.mifare")) {
            technologies.addAll(Arrays.asList(MIFARE_TECHNOLOGIES));
        }
    }

    private void enableNfcReaderMode() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            int flags = NfcAdapter.FLAG_READER_NFC_A |
                        NfcAdapter.FLAG_READER_NFC_B |
                        NfcAdapter.FLAG_READER_NFC_F |
                        NfcAdapter.FLAG_READER_NFC_V |
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
            nfcAdapter.enableReaderMode(this, this, flags, null);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        TagTechnology tagTech = getTagTechnology(tag);
        if (tagTech == null) return;

        try {
            tagTech.connect();
            SystemClock.sleep(250);
            processCommands(tagTech);
            tagTech.close();
        } catch (IOException e) {
            appendOutputText(e.getMessage());
        }
    }

    private void processCommands(TagTechnology tagTech) throws IOException {
        clearOutputText();
        String[] commands = getInputText().split("\\r?\\n");
        for (String command : commands) {
            String trimmed = command.trim();
            if (!trimmed.isEmpty()) {
                byte[] commandBytes = hexStringToByteArray(trimmed);
                byte[] response = transceive(tagTech, commandBytes);
                appendOutputText(Hex.encodeHexString(response, false));
            }
        }
    }

    private void showTechnologySelectionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Select Tag Technology")
            .setItems(technologies.toArray(new String[0]), (dialog, which) -> {
                selectedTechnology = which;
                invalidateOptionsMenu();
                clearOutputText();
            })
            .setCancelable(false)
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (selectedTechnology != null) {
            menu.add(0, 1, 0, technologies.get(selectedTechnology))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dngr.us/rawnfc"));
            startActivity(browserIntent);
        } else if (item.getItemId() == 1) {
            selectedTechnology = null;
            showTechnologySelectionDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private TagTechnology getTagTechnology(Tag tag) {
        if (selectedTechnology == null) return null;
        String tech = technologies.get(selectedTechnology);
        switch (tech) {
            case "NFC-A (ISO 14443-3A)": return NfcA.get(tag);
            case "NFC-B (ISO 14443-3B)": return NfcB.get(tag);
            case "NFC-F (JIS 6319-4)": return NfcF.get(tag);
            case "NFC-V (ISO 15693)": return NfcV.get(tag);
            case "ISO-DEP (ISO 14443-4)": return IsoDep.get(tag);
            case "MIFARE Classic": return MifareClassic.get(tag);
            case "MIFARE Ultralight": return MifareUltralight.get(tag);
            default: return null;
        }
    }

    private static byte[] transceive(TagTechnology handle, byte[] payload) throws IOException {
        if (handle instanceof NfcA) return ((NfcA) handle).transceive(payload);
        if (handle instanceof NfcB) return ((NfcB) handle).transceive(payload);
        if (handle instanceof NfcF) return ((NfcF) handle).transceive(payload);
        if (handle instanceof NfcV) return ((NfcV) handle).transceive(payload);
        if (handle instanceof IsoDep) return ((IsoDep) handle).transceive(payload);
        if (handle instanceof MifareClassic) return ((MifareClassic) handle).transceive(payload);
        if (handle instanceof MifareUltralight) return ((MifareUltralight) handle).transceive(payload);
        throw new IOException("Unsupported technology");
    }

    private void shareDump() {
        if (selectedTechnology == null || outputView.getText().toString().isEmpty()) return;

        String techText = "android.nfc.tech." + TECH_CLASS_SIMPLE_NAMES[selectedTechnology];
        String dumpText = formatDump(techText, inputView.getText().toString(), outputView.getText().toString());

        Intent shareIntent = new Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, dumpText);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private String formatDump(String techText, String inputText, String outputText) {
        String[] inputLines = inputText.split("\\r?\\n");
        String[] outputLines = outputText.split("\\r?\\n");
        StringBuilder result = new StringBuilder("-----BEGIN RAW NFC DUMP-----\n")
            .append(techText).append('\n');

        int lines = outputLines.length;
        for (int i = 0; i < lines; i++) {
            if (i < inputLines.length) {
                result.append("> ").append(inputLines[i].replaceAll(" ", "")).append('\n');
            }
            result.append("< ").append(outputLines[i]).append('\n');
        }

        return result.append("-----END RAW NFC DUMP-----").toString().trim();
    }

    private String getInputText() {
        return inputView.getText().toString();
    }

    private void clearOutputText() {
        runOnUiThread(() -> outputView.setText(""));
    }

    private void appendOutputText(final String text) {
        runOnUiThread(() -> {
            if (outputView.getText().toString().isEmpty()) {
                outputView.append(text);
            } else {
                outputView.append("\n" + text);
            }
        });
    }

    private static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll(" ", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private class HexTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            clearOutputText();
        }

        @Override
        public void afterTextChanged(Editable s) {
            StringBuilder sanitized = new StringBuilder();
            for (char c : s.toString().toCharArray()) {
                if (Character.isWhitespace(c) || Character.toString(c).matches("[0-9A-Fa-f]")) {
                    sanitized.append(Character.toUpperCase(c));
                }
            }
            String newText = sanitized.toString();
            if (!newText.equals(s.toString())) {
                s.replace(0, s.length(), newText);
            }
        }
    }
}

