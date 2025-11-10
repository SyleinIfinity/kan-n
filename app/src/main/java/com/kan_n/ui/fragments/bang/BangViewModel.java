package com.kan_n.ui.fragments.bang;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BangViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BangViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Đây là fragment: Bảng");
    }

    public LiveData<String> getText() {
        return mText;
    }
}