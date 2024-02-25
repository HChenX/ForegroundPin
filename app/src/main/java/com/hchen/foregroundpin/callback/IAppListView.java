package com.hchen.foregroundpin.callback;

import android.content.Context;

public interface IAppListView {
    void hide();

    void search(Context context, String keyword);
}
