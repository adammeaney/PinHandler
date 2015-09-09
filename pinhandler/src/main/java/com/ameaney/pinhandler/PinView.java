package com.ameaney.pinhandler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class PinView extends ViewGroup
{
    public static class Defaults
    {
        public static final int NUM_DIGITS = 4;
        public static final int DIGIT_HEIGHT = 50;
        public static final int DIGIT_WIDTH = 50;
        public static final int DIGIT_SPACING = 20;
        public static final int DIGIT_TEXT_SIZE = 15;
        public static final int DIGIT_BACKGROUND_COLOR = Color.TRANSPARENT;
        public static final int DIGIT_BORDER_COLOR = Color.BLACK;
        public static final int DIGIT_ACCENT_COLOR = Color.MAGENTA;

        public static final int ACCENT_HEIGHT = 2;
    }

    private int _numDigits;
    private int _digitHeight;
    private int _digitWidth;
    private int _digitSpacing;
    private int _digitTextSize;
    private int _digitElevation = 0;

    private int _digitBackgroundColor;
    private int _digitBorderColor;
    private int _digitAccentColor;
    private int _accentHeight;

    private PinView _pinView;
    private EditText _pinInputField;

    private OnPinFinishedListener _pinFinishedListener;

    public PinView(Context context)
    {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attributeSet)
    {
        this(context, attributeSet, 0);
    }

    public PinView(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);

        _pinView = this;

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.PinView);

        // Number of digits
        _numDigits = array.getInt(R.styleable.PinView_numDigits, Defaults.NUM_DIGITS);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // Digit dimensions
        _digitWidth = array.getDimensionPixelSize(R.styleable.PinView_digitWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Defaults.DIGIT_WIDTH, metrics));
        _digitHeight = array.getDimensionPixelSize(R.styleable.PinView_digitHeight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Defaults.DIGIT_HEIGHT, metrics));
        _digitSpacing = array.getDimensionPixelSize(R.styleable.PinView_digitSpacing, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Defaults.DIGIT_SPACING, metrics));
        _digitTextSize = array.getDimensionPixelSize(R.styleable.PinView_digitTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Defaults.DIGIT_TEXT_SIZE, metrics));

        _digitBackgroundColor = array.getInt((R.styleable.PinView_digitBackgroundColor), Defaults.DIGIT_BACKGROUND_COLOR);
        _digitBorderColor = array.getInt(R.styleable.PinView_digitBorderColor, Defaults.DIGIT_BORDER_COLOR);

        Resources.Theme theme = context.getTheme();

        TypedValue accentColor = new TypedValue();
        theme.resolveAttribute(R.attr.colorAccent, accentColor, true);
        _digitAccentColor = array.getColor(R.styleable.PinView_digitAccentColor,
                accentColor.resourceId > 0 ? getResources().getColor(accentColor.resourceId) : accentColor.data);

        _accentHeight = array.getDimensionPixelSize(R.styleable.PinView_accentHeight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Defaults.ACCENT_HEIGHT, metrics));
        array.recycle();

        getViews();
    }

    @Override
    public boolean shouldDelayChildPressedState()
    {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // Measure children
        for (int i = 0; i < getChildCount(); i++)
        {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Calculate the size of the view
        int width = (_digitWidth * _numDigits) + (_digitSpacing * (_numDigits - 1));
        setMeasuredDimension(
                width + getPaddingLeft() + getPaddingRight() + (_digitElevation * 2),
                _digitHeight + getPaddingTop() + getPaddingBottom() + (_digitElevation * 2));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        for (int i = 0; i < _numDigits; i++)
        {
            View view = getChildAt(i);

            int left = i * _digitWidth + i * _digitSpacing + getPaddingLeft() + _digitElevation;
            int top = getPaddingTop() + _digitElevation / 2;
            int right = left + _digitWidth;
            int bottom = top + _digitHeight;
            view.layout(left, top, right, bottom);
        }

        // Add the edit text as a 1px wide view to allow it to focus
        getChildAt(_numDigits).layout(0, 0, 1, getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            // Make sure this view is focused
            _pinInputField.requestFocus();

            // Show keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(_pinInputField, 0);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private Drawable getDrawable()
    {
        ColorDrawable box = new ColorDrawable(Color.BLACK);
        ColorDrawable background = new ColorDrawable(Color.RED);
        ColorDrawable accent = new ColorDrawable(Color.BLUE);

        LayerDrawable selectedBackground = new LayerDrawable(new Drawable[] { box, accent, background });
        selectedBackground.setLayerInset(1, 5, 5, 5, 5);
        selectedBackground.setLayerInset(2, 5, 5, 5, 15);

        LayerDrawable defaultBackground = new LayerDrawable(new Drawable[] { box, background });
        defaultBackground.setLayerInset(1, 5, 5, 5, 5);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] { android.R.attr.state_selected }, selectedBackground);
        stateListDrawable.addState(new int[] {}, defaultBackground);

        return stateListDrawable;
    }

    @TargetApi(23)
    private void getViews()
    {
        Context context = getContext();

        this.removeAllViews();

        // Add a digit view for each digit
        for (int i = 0; i < _numDigits; i++)
        {
            TextView digitView = new DigitView(context);
            digitView.setWidth(_digitWidth);
            digitView.setHeight(_digitHeight);

            digitView.setBackground(getDrawable());

            //digitView.setTextColor(mDigitTextColor);
            digitView.setTextSize(_digitTextSize);
            digitView.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                digitView.setElevation(_digitElevation);
            }
            addView(digitView);
        }

        Resources resources = getResources();

        // Add an "invisible" edit text to handle input
        _pinInputField = new EditText(context);
        _pinInputField.setBackgroundColor(resources.getColor(android.R.color.transparent));
        _pinInputField.setTextColor(resources.getColor(android.R.color.transparent));
        _pinInputField.setCursorVisible(false);
        _pinInputField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(_numDigits)});
        _pinInputField.setInputType(InputType.TYPE_CLASS_NUMBER);
        _pinInputField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        _pinInputField.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                // Update the selected state of the views
                int length = _pinInputField.getText().length();
                for (int i = 0; i < _numDigits; i++)
                {
                    View view = getChildAt(i);
                    if (view == null)
                    {
                        break;
                    }
                    view.setSelected(hasFocus && length == i);
                }

                // Make sure the cursor is at the end
                _pinInputField.setSelection(length);

                // Provide focus change events to any listener
                //                if (mOnFocusChangeListener != null)
                //                {
                //                    mOnFocusChangeListener.onFocusChange(PinView.this, hasFocus);
                //                }
            }
        });
        _pinInputField.addTextChangedListener(new PinWatcher());
        addView(_pinInputField);
    }

    public void setOnPinFinishedListener(OnPinFinishedListener listener)
    {
        _pinFinishedListener = listener;
    }

    private class DigitView extends TextView
    {
        public DigitView(Context context)
        {
            super(context);
        }
    }

    private class PinWatcher implements TextWatcher
    {
        public PinWatcher()
        {
            super();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable string)
        {
            int length = string.length();
            for (int i = 0; i < _numDigits; i++)
            {
                DigitView digit = (DigitView) getChildAt(i);
                if (string.length() > i)
                {
                    String mask = "•"; // Bullet
                    digit.setText(mask);
                }
                else
                {
                    digit.setText("");
                }

                if (_pinInputField.hasFocus() || _pinInputField.hasWindowFocus())
                {
                    if (i == length)
                    {
                        digit.setSelected(true);
                    }
                    else
                    {
                        digit.setSelected(false);
                    }
                }
            }

            if (length == _numDigits)
            {
                // Finished
                if (_pinFinishedListener != null)
                {
                    _pinFinishedListener.pinEntered(string.toString());
                }
            }
        }
    }
}

