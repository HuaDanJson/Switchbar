package com.example.jason.switchbar;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements SwitchBar.OnClickListener {

    private SwitchBar mSwitchBar;
    private int mCurrentIndex = 0;
    private FrameLayout mFrameLayout;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment1 fragment1;
    private Fragment2 fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitchBar = findViewById(R.id.switchbar_main_activity);
        mFrameLayout = findViewById(R.id.fl_main_activity);
        mSwitchBar.setOnTabClickListener(this);
        mSwitchBar.setDuration(200);
        fragment1 = Fragment1.newInstance();
        fragment2 = Fragment2.newInstance();
    }

    @Override
    public void onClick(int position, String text) {
        if (mCurrentIndex == position) {
            return;
        } else {
            switch (position) {
                case 0:
                    switchOneP();
                    break;
                case 1:
                    switchTwoP();
                    break;
                default:
                    break;
            }
        }

        mCurrentIndex = position;
    }

    public void switchOneP() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main_activity, fragment1);
        fragmentTransaction.commit();
    }

    public void switchTwoP() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main_activity, fragment2);
        fragmentTransaction.commit();
    }
}
