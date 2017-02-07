package me.huafeng.ilovezappos;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by User on 02/05/2017.
 */

public class RetainedFragment extends Fragment {
    // data object we want to retain
    private BundleData bd;
    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(BundleData bd) {
        this.bd = bd;
    }

    public BundleData getBundleData() {
        return this.bd;
    }
}
