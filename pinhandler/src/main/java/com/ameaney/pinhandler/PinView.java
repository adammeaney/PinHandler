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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PinView extends HorizontalScrollView
{
    public static class Defaults
    {
        public static final int NUM_DIGITS = 4;
        public static final int DIGIT_HEIGHT = 50;
        public static final int DIGIT_WIDTH = 50;
        public static final int DIGIT_SPACING = 20;
        public static final int DIGIT_TEXT_SIZE = 15;
        public static final int DIGIT_BORDER_COLOR = Color.TRANSPARENT;

        public static final int ACCENT_HEIGHT = 5;
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
    private int _digitTextColor;
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

        this.setFillViewport(true);

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

        _digitBorderColor = array.getColor(R.styleable.PinView_digitBorderColor, Defaults.DIGIT_BORDER_COLOR);

        Resources.Theme theme = context.getTheme();

        TypedValue resolvedColor = new TypedValue();

        theme.resolveAttribute(android.R.attr.textColorPrimary, resolvedColor, true);
        _digitTextColor = array.getColor((R.styleable.PinView_digitTextColor),
                resolvedColor.resourceId > 0 ? getResources().getColor(resolvedColor.resourceId) : resolvedColor.data);

        theme.resolveAttribute(android.R.attr.windowBackground, resolvedColor, true);
        _digitBackgroundColor = array.getColor((R.styleable.PinView_digitBackgroundColor),
                resolvedColor.resourceId > 0 ? getResources().getColor(resolvedColor.resourceId) : resolvedColor.data);

        theme.resolveAttribute(android.R.attr.colorAccent, resolvedColor, true);
        _digitAccentColor = array.getColor(R.styleable.PinView_digitAccentColor,
                resolvedColor.resourceId > 0 ? getResources().getColor(resolvedColor.resourceId) : resolvedColor.data);

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
        ColorDrawable box = new ColorDrawable(_digitBorderColor);
        ColorDrawable background = new ColorDrawable(_digitBackgroundColor);
        ColorDrawable accent = new ColorDrawable(_digitAccentColor);

        LayerDrawable selectedBackground = new LayerDrawable(new Drawable[] { box, accent, background });
        selectedBackground.setLayerInset(1, 5, 5, 5, 5);
        selectedBackground.setLayerInset(2, 5, 5, 5, 5 + _accentHeight);

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

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(layoutParams);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        addView(layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(_digitSpacing / 2, 0, _digitSpacing / 2, 0);
        params.gravity = Gravity.CENTER;

        // Add a digit view for each digit
        for (int i = 0; i < _numDigits; i++)
        {
            TextView digitView = new DigitView(context);
            digitView.setLayoutParams(params);
            digitView.setWidth(_digitWidth);
            digitView.setHeight(_digitHeight);

            digitView.setBackground(getDrawable());

            digitView.setTextColor(_digitTextColor);
            digitView.setTextSize(_digitTextSize);
            digitView.setGravity(Gravity.CENTER);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                digitView.setElevation(_digitElevation);
            }

            layout.addView(digitView);
        }

        Resources resources = getResources();

        // Add an "invisible" edit text to handle input
        _pinInputField = new EditText(context);
        _pinInputField.setTextSize(0);
        _pinInputField.setBackgroundColor(resources.getColor(android.R.color.transparent));
        _pinInputField.setTextColor(resources.getColor(android.R.color.transparent));
        _pinInputField.setCursorVisible(false);
        _pinInputField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(_numDigits)});
        _pinInputField.setInputType(InputType.TYPE_CLASS_NUMBER);
        _pinInputField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        _pinInputField.setMovementMethod(null);
        _pinInputField.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                // Update the selected state of the views
                int length = _pinInputField.getText().length();
                LinearLayout linearLayout = (LinearLayout) getChildAt(0);

                for (int i = 0; i < _numDigits; i++)
                {
                    View view = linearLayout.getChildAt(i + 1);
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
        layout.addView(_pinInputField, 0);
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
            LinearLayout layout = (LinearLayout) getChildAt(0);

            for (int i = 0; i < _numDigits; i++)
            {
                DigitView digit = (DigitView) layout.getChildAt(i + 1);
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
                        int width = getResources().getDisplayMetrics().widthPixels / 2;
                        _pinView.smoothScrollTo((int)digit.getX() - width, (int)digit.getY());
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

