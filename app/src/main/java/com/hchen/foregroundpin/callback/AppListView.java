package com.hchen.foregroundpin.callback;

import android.content.Context;

public interface AppListView {
    void hide();

    void search(Context context, String keyword);
}
