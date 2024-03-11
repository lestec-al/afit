package com.yurhel.alex.afit.core;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yurhel.alex.afit.MainActivity;
import com.yurhel.alex.afit.R;
import com.yurhel.alex.afit.calendar.CalendarActivity;
import com.yurhel.alex.afit.databinding.DialogEditBinding;
import com.yurhel.alex.afit.statsAll.AllStatsActivity;

import java.util.Date;

public class Help {

    public static void setImageButtonsColor(int color, ImageButton[] buttons) {
        for (ImageButton button: buttons) {
            button.setColorFilter(color);
        }
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
        Drawable d = AppCompatResources.getDrawable(context, R.drawable.ic_back);
        if (d != null) {
            d.setTint(color);
            actionBar.setHomeAsUpIndicator(d);
        }
    }

    public static void setButtonsTextColor(int color, Button[] buttons) {
        for (Button b: buttons) {
            b.setTextColor(color);
        }
    }

    public static Object[] editDialog(
            Context context,
            int color,
            Integer infoMsg,
            String editMsg,
            Boolean isEditNumber,
            boolean isReturnWithCancel
    ) {
        Dialog dialog = new Dialog(context);
        DialogEditBinding dViews = DialogEditBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dViews.getRoot());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setImageButtonsColor(color, new ImageButton[]{dViews.okButton, dViews.cancelButton});
        if (!isReturnWithCancel) dViews.cancelButton.setOnClickListener(view -> dialog.cancel());

        if (infoMsg != null) dViews.dialogLabel.setText(infoMsg);
        else dViews.dialogLabel.setVisibility(View.GONE);

        if (isEditNumber == null) {
            dViews.editText.setVisibility(View.GONE);
            dViews.editNumber.setVisibility(View.GONE);
            return (isReturnWithCancel) ? new Object[] {dialog, dViews.okButton, dViews.cancelButton}: new Object[] {dialog, dViews.okButton};
        } else {
            EditText edit;
            if (isEditNumber) {
                dViews.editText.setVisibility(View.GONE);
                edit = dViews.editNumber;
            } else {
                dViews.editNumber.setVisibility(View.GONE);
                edit = dViews.editText;
            }
            edit.setText(editMsg);
            edit.setHint(R.string.type_text);
            edit.setBackgroundTintList(ColorStateList.valueOf(color));
            return (isReturnWithCancel) ? new Object[] {dialog, dViews.okButton, edit, dViews.cancelButton}: new Object[] {dialog, dViews.okButton, edit};
        }
    }

    public static CharSequence dateFormat(Context context, Date date) {
        return DateFormat.getMediumDateFormat(context).format(date);
    }

    public static void setupBottomNavigation(
            Context context,
            int selectedAction,
            BottomNavigationView nav,
            Runnable finishAction
    ) {
        nav.setSelectedItemId(selectedAction);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() != selectedAction) {
                if (item.getItemId() == R.id.actionHome) {
                    startActivity(context, new Intent(context, MainActivity.class), null);
                    finishAction.run();
                    return true;

                } else if (item.getItemId() == R.id.actionCalendar) {
                    startActivity(context, new Intent(context, CalendarActivity.class), null);
                    finishAction.run();
                    return true;

                } else if (item.getItemId() == R.id.actionGraph) {
                    startActivity(context, new Intent(context, AllStatsActivity.class), null);
                    finishAction.run();
                    return true;
                }
            }
            return false;
        });
    }
}