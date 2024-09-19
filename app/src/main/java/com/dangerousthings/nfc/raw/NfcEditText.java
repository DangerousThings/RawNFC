package com.dangerousthings.nfc.raw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.text.InputType;

public class NfcEditText extends EditText {
    private static final int LINE_NUMBER_PADDING = 75;
    private static final int LINE_NUMBER_MARGIN = 25;

    private final Rect rect = new Rect();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isUpdating = false;

    public NfcEditText(Context context) {
        super(context);
        init();
    }

    public NfcEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NfcEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(getTextSize());
        
        int paddingLeft = getPaddingLeft() + LINE_NUMBER_PADDING;
        setPadding(paddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFocused() || getText().length() > 0) {
            drawLineNumbers(canvas);
        }

        super.onDraw(canvas);
    }

    private void drawLineNumbers(Canvas canvas) {
        int lineCount = getLineCount();
        int lineNumber = 1;
        for (int i = 0; i < lineCount; i++) {
            int baseline = getLineBounds(i, null);
            if (i == 0 || getText().charAt(getLayout().getLineStart(i) - 1) == '\n') {
                String lineNumberStr = String.format("%2d", lineNumber);
                canvas.drawText(lineNumberStr, rect.left + LINE_NUMBER_MARGIN, baseline, paint);
                lineNumber++;
            }
        }
    }

}
