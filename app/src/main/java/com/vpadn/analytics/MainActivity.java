package com.vpadn.analytics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vpon.sdk.VpdataAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 2001;

    private VpdataAnalytics.Tracker tracker = null;

    private static final String LT = "MainActivity";

    //TODO set your licenseKey & customerId
    private String licenseKey = "mock_license_key";
    private String customerId = "mock_custom_id";

    private String payload = DEFAULT_EXTRA_DATA;


    private static final String DEFAULT_EXTRA_DATA = "{\"Key1\":\"value1\",\"Key2\":\"value2\"}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        //request optional permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        }


        VpdataAnalytics vpdataAnalytics = VpdataAnalytics.INSTANCE;

        //just for debug mode, remember to set to false before app release!!!
        //need to be set before vpdataAnalytics.initialize
        vpdataAnalytics.setDebugMode(true);

        //set VpdataAnalytics.OptIn according to users agreement
        vpdataAnalytics.initialize(this, licenseKey, customerId, VpdataAnalytics.OptIn.CONSENT);

        initViews();

        //construct a Tracker for
        tracker = new VpdataAnalytics.Tracker();
    }

    public static class SpinnerFragment extends DialogFragment {

        private Object o = null;

        public void setCustomListener(Object o) {
            this.o = o;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder;
            WeakReference<Activity> activityWeakReference = new WeakReference<Activity>(getActivity());
            builder = new AlertDialog.Builder(activityWeakReference.get());
            Spinner spinner = new Spinner(activityWeakReference.get());
            ArrayAdapter<CharSequence> eventList = ArrayAdapter.createFromResource(activityWeakReference.get(),
                    R.array.events,
                    android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(eventList);
            final StringBuilder sb = new StringBuilder();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        if (o != null) {
                            if (view instanceof TextView) {
                                TextView tv = (TextView) view;
                                sb.append(tv.getText());
                            }
                            EventSelectedListener listener = (EventSelectedListener) o;
                            listener.onEventSelected(sb.toString());
                            getDialog().dismiss();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.e(LT, "onNothingSelected");
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setMessage(getString(R.string.title_select_event));
            alertDialog.setView(spinner);

            return alertDialog;
        }
    }

    private void initViews() {
        TextInputEditText tieExtraData = findViewById(R.id.ie_extra_data);
        tieExtraData.setText(DEFAULT_EXTRA_DATA);
        tieExtraData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = editable.toString();
                try{
                    new JSONObject(txt);
                    payload = txt;
                } catch (JSONException ignore) {
                    Toast.makeText(getBaseContext(), "Not Valid JSON string",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void fireButton(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_send_event:
                SpinnerFragment spinnerFragment = new SpinnerFragment();

                if (eventSelectedListener != null) {
                    spinnerFragment.setCustomListener(eventSelectedListener);
                }

                spinnerFragment.show(getSupportFragmentManager(), "spinner");
                break;
        }
    }

    private void sendEvent(String event) {
        Log.e(LT, "sendEvent("+event+") invoked!!");
        if(tracker != null){
            try {
                JSONObject jsonObject = new JSONObject(payload);
                //send event
                tracker.sendEvent(event, jsonObject);
            } catch (JSONException ignore) {
            }
        }
    }

    private EventSelectedListener eventSelectedListener = new EventSelectedListener() {
        @Override
        public void onEventSelected(String event) {
            sendEvent(event);
        }
    };

    public interface EventSelectedListener {
        void onEventSelected(String event);
    }
}
