package com.yurhel.alex.afit;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Date;

public class Help {

    // COLORS
    public static int getMainColor(Context context) {
        return (isNightMode(context)) ? context.getColor(R.color.white): context.getColor(R.color.dark);
    }

    public static void setImageButtonsColor(int color, ImageButton[] buttons) {
        for (ImageButton button: buttons)
            button.setColorFilter(color);
    }

    public static void setActionIconsColor(int color, Menu menu, int[] itemsIds) {
        for (int item: itemsIds) {
            MenuItem action = menu.findItem(item);
            Drawable drawable1 = action.getIcon();
            Drawable drawable2 = DrawableCompat.wrap(drawable1);
            drawable2.setTint(color);
            action.setIcon(drawable2);
        }
    }

    public static void setActionBackIconColor(Context context, int color, ActionBar actionBar) {
        Drawable b = AppCompatResources.getDrawable(context, R.drawable.ic_up_back);
        assert b != null;
        b.setTint(color);
        actionBar.setHomeAsUpIndicator(b);
    }

    public static void setButtonsTextColor(int color, Button[] buttons) {
        for (Button b: buttons)
            b.setTextColor(color);
    }

    // DIALOGS
    public static Object[] editTextDialog(Context context, int color, Integer msg, String text, Boolean editNumber, boolean withCancel) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button ok = dialog.findViewById(R.id.OkButton);
        Button cancel = dialog.findViewById(R.id.cancelButton);
        setButtonsTextColor(color, new Button[] {ok, cancel});
        if (!withCancel)
            cancel.setOnClickListener(view -> dialog.cancel());
        if (msg != null) {
            TextView info = dialog.findViewById(R.id.dialogLabel);
            info.setText(msg);
        } else {
            dialog.findViewById(R.id.dialogLabel).setVisibility(View.GONE);
        }
        if (editNumber == null) {
            dialog.findViewById(R.id.editText).setVisibility(View.GONE);
            dialog.findViewById(R.id.editNumber).setVisibility(View.GONE);
            return (withCancel) ? new Object[] {dialog, ok, cancel}: new Object[] {dialog, ok};
        } else {
            EditText edit;
            if (editNumber) {
                dialog.findViewById(R.id.editText).setVisibility(View.GONE);
                edit = dialog.findViewById(R.id.editNumber);
            } else {
                dialog.findViewById(R.id.editNumber).setVisibility(View.GONE);
                edit = dialog.findViewById(R.id.editText);
            }
            edit.setText(text);
            edit.setHint(R.string.type_text);
            edit.setBackgroundTintList(ColorStateList.valueOf(color));
            return (withCancel) ? new Object[] {dialog, ok, edit, cancel}: new Object[] {dialog, ok, edit};
        }
    }

    // OTHERS
    public static CharSequence dateFormat(Context context, Date date) {
        return DateFormat.getMediumDateFormat(context).format(date);
    }

    public static boolean isNightMode(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}