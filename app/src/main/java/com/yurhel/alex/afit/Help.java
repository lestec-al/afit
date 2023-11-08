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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Date;

public class Help {

    public static int getMainColor(Context context) {
        boolean isNight = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        return (isNight) ? context.getColor(R.color.white): context.getColor(R.color.dark);
    }

    public static void setImageButtonsColor(int color, ImageButton[] buttons) {
        for (ImageButton button: buttons)
            button.setColorFilter(color);
    }

    public static void setActionIconsColor(int color, Menu menu, int[] itemsIds) {
        for (int item: itemsIds) {
            MenuItem action = menu.findItem(item);
            Drawable drawable1 = action.getIcon();
            if (drawable1 != null) {
                Drawable drawable2 = DrawableCompat.wrap(drawable1);
                drawable2.setTint(color);
                action.setIcon(drawable2);
            }
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

    public static Object[] editTextDialog(
            Context context,
            int color,
            Integer infoMsg,
            String editMsg,
            Boolean isEditNumber,
            boolean isReturnWithCancel
    ) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit);
        Window window = dialog.getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button ok = dialog.findViewById(R.id.OkButton);
        Button cancel = dialog.findViewById(R.id.cancelButton);
        setButtonsTextColor(color, new Button[] {ok, cancel});
        if (!isReturnWithCancel)
            cancel.setOnClickListener(view -> dialog.cancel());
        if (infoMsg != null) {
            TextView info = dialog.findViewById(R.id.dialogLabel);
            info.setText(infoMsg);
        } else {
            dialog.findViewById(R.id.dialogLabel).setVisibility(View.GONE);
        }
        if (isEditNumber == null) {
            dialog.findViewById(R.id.editText).setVisibility(View.GONE);
            dialog.findViewById(R.id.editNumber).setVisibility(View.GONE);
            return (isReturnWithCancel) ? new Object[] {dialog, ok, cancel}: new Object[] {dialog, ok};
        } else {
            EditText edit;
            if (isEditNumber) {
                dialog.findViewById(R.id.editText).setVisibility(View.GONE);
                edit = dialog.findViewById(R.id.editNumber);
            } else {
                dialog.findViewById(R.id.editNumber).setVisibility(View.GONE);
                edit = dialog.findViewById(R.id.editText);
            }
            edit.setText(editMsg);
            edit.setHint(R.string.type_text);
            edit.setBackgroundTintList(ColorStateList.valueOf(color));
            return (isReturnWithCancel) ? new Object[] {dialog, ok, edit, cancel}: new Object[] {dialog, ok, edit};
        }
    }

    public static CharSequence dateFormat(Context context, Date date) {
        return DateFormat.getMediumDateFormat(context).format(date);
    }
}