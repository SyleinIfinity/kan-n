package com.kan_n.ui.fragments.thongtin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThongTinViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ThongTinViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Đây là fragment: Thông tin");
    }

    public LiveData<String> getText() {
        return mText;
    }
}