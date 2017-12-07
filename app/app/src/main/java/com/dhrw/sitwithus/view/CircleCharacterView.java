package com.dhrw.sitwithus.view;

import android.view.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;

public class CircleCharacterView extends View {

    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

    //
    private Paint circlePaint;
    private TextPaint letterPaint;

    //
    private char character;

    public CircleCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create the paint object for the background color
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(attrs.getAttributeUnsignedIntValue(ANDROID_NAMESPACE,
                "background", Color.GRAY));

        // Create the paint object for the view text
        letterPaint = new TextPaint();
        letterPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        letterPaint.setTextAlign(Paint.Align.CENTER);

        //
        letterPaint.setColor(attrs.getAttributeUnsignedIntValue(ANDROID_NAMESPACE,
                "textColor", Color.WHITE));
        letterPaint.setTextAlign(Paint.Align.CENTER);
        letterPaint.setAntiAlias(true);

        //
        String text =  attrs.getAttributeValue(ANDROID_NAMESPACE, "text");
        if (text != null) {
            character = text.charAt(0);
        } else {
            character = 'A';
        }
    }

    /** */
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // Draw the background circle
        canvas.drawCircle(width / 2f, height / 2f,
                Math.min(width, height) / 4f, circlePaint);

        // Draw the character in the center of the circle
        canvas.drawText(Character.toString(character), width / 2f,
                height / 2f - ((letterPaint.descent() + letterPaint.ascent()) / 2f),
                letterPaint);
    }

    /** */
    @Override
    public void setBackgroundColor(int color) {

        // Create the paint object for the circular button
        circlePaint.setColor(color);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    /** */
    public void setLetterColor(int color) {

        // Create the paint object for the circular button
        letterPaint.setColor(color);
        letterPaint.setStyle(Paint.Style.FILL);
    }

    /** */
    public void setLetter(Character character) {
        this.character = character;
    }
}
