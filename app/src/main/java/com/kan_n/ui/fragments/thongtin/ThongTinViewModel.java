package com.kan_n.ui.fragments.thongtin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kan_n.data.models.User;

public class ThongTinViewModel extends ViewModel {

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void setUser(User user) {
        userLiveData.setValue(user);
    }
}
