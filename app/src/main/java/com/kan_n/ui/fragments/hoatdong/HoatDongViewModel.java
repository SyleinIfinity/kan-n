package com.kan_n.ui.fragments.hoatdong;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HoatDongViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HoatDongViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Đây là fragment: Hoạt động");
    }

    public LiveData<String> getText() {
        return mText;
    }
}