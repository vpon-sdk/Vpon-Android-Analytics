package com.vpadn.analytics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
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

import com.vpadn.dmp.VpadnAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

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

    private static final String LT = "MainActivity";

    private String licenseKey = MOCK_LICENSE_KEY;
    private String customerId = MOCK_CUSTOM_ID;
    private String payload = DEFAULT_EXTRA_DATA;

    private TextInputEditText tieServerUrl = null;
    private TextInputEditText tieLicenseKey = null;
    private TextInputEditText tieCustomId = null;

    private static final String DEFAULT_EXTRA_DATA = "{\"Key1\":\"value1\",\"Key2\":\"value2\"}";
    private static final String MOCK_LICENSE_KEY = "mock_license_key";
    private static final String MOCK_CUSTOM_ID = "mock_custom_id";

    private void initViews() {
        TextInputEditText tieExtraData = findViewById(R.id.ie_extra_data);
        tieCustomId = findViewById(R.id.ie_custom_id);
        tieLicenseKey = findViewById(R.id.ie_license_key);
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
        tieLicenseKey.setText(MOCK_LICENSE_KEY);
        tieCustomId.setText(MOCK_CUSTOM_ID);

        tieCustomId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                customerId = editable.toString();
            }
        });

        tieLicenseKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                licenseKey = editable.toString();
                VpadnAnalytics.getInstance(getBaseContext(), licenseKey);
            }
        });

    }

    public void fireButton(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_reset:
                tieServerUrl.setText(null);
                tieCustomId.setText(null);
                tieLicenseKey.setText(null);
                break;
            case R.id.btn_send_event:

                SpinnerFragment spinnerFragment = new SpinnerFragment();

                if (eventSelectedListener != null) {
                    spinnerFragment.setCustomListener(eventSelectedListener);
                }

                spinnerFragment.show(getSupportFragmentManager(), "sipnner");

                break;
        }
    }

    private void sendEvent(String event) {
        Log.e(LT, "sendEvent("+event+") invoked!!");
        if(tracker != null){
            try {
                JSONObject jsonObject = new JSONObject(payload);
                tracker.sendEvent(event, jsonObject,customerId);
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

    private VpadnAnalytics.Tracker tracker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        WeakReference<Context> contextWeakReference = new WeakReference<>(getBaseContext());

        VpadnAnalytics vpadnAnalytics = VpadnAnalytics.getInstance(contextWeakReference.get(), licenseKey);

        tracker = vpadnAnalytics.newTracker();

        if (tracker != null) {
            tracker.sendLaunchEvent();
        }
    }

    public interface EventSelectedListener {
        void onEventSelected(String event);
    }

}
