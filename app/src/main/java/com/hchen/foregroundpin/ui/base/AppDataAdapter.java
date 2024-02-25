package com.hchen.foregroundpin.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.foregroundpin.R;

import java.util.List;

public class AppDataAdapter extends ArrayAdapter<AppData> {
    private final int resource;

    public AppDataAdapter(@NonNull Context context, int resource, @NonNull List<AppData> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AppData appData = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }
        if (appData == null) return convertView;
        ImageView imageView = convertView.findViewById(R.id.app_icon);
        TextView name = convertView.findViewById(R.id.app_name);
        CheckBox checkBox = convertView.findViewById(R.id.check_box);

        checkBox.setChecked(true);
        imageView.setImageDrawable(appData.icon);
        name.setText(appData.label);

        return convertView;
    }
}
