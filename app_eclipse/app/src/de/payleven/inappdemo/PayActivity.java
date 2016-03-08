package de.payleven.inappdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.payleven.inappsdk.PaylevenFactory;
import de.payleven.inappsdk.PaylevenInAppClient;
import de.payleven.inappsdk.PaymentInstrument;
import de.payleven.inappsdk.PaymentInstrumentAction;
import de.payleven.inappsdk.errors.CallbackError;
import de.payleven.inappsdk.listeners.EditPaymentInstrumentListener;
import de.payleven.inappsdk.listeners.GetPaymentInstrumentsListener;
import de.payleven.inappsdk.listeners.RemovePaymentInstrumentListener;
import de.payleven.inappsdk.listeners.SetPaymentInstrumentsOrderListener;

/**
 * Activity used to request and display the list of payment instruments for the active user
 * depending on the use case
 */
public class PayActivity extends ToplevelActivity implements
        PayListAdapter.RemovePaymentInstrumentButtonListener {

    private ListView paymentInstrumentsListView;
    private TextView emptyListTextView;
    private ProgressDialogFragment progressDialogFragment;
    private EditText useCaseEditText;

    private PayListAdapter listAdapter;

    private PaylevenWrapper paylevenWrapper;

    private List<PaymentInstrument> paymentInstruments;

    private static final String REGEX_PATTERN = "^[A-Z0-9._%+-]+@[A-Z0-9-]+\\.[A-Z]{2,6}$";

    private EditText emailEditText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        paylevenWrapper = PaylevenWrapper.getInstance();
        paymentInstruments = new ArrayList<PaymentInstrument>();
        initUI();
        
        emailEditText = (EditText) findViewById(R.id.email_edittext);
        
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	String email = getEmail();
                        String error = emailValidation(email);
                        if(error != null){
                            emailEditText.setError(error);
                        }else {
                            registerWithPaylevenClient();
                            paylevenWrapper.setEmail(email);
                        }
                    }
                });        
    }

    /**
     * Register with {@link de.payleven.inappsdk.PaylevenInAppClient} and initialize the
     * {@link de.payleven.inappdemo.PaylevenWrapper}
     */
    private void registerWithPaylevenClient() {
        PaylevenInAppClient paylevenInAppClient = PaylevenFactory.registerWithAPIKey(
                this, PaylevenWrapper.API_KEY);
        paylevenWrapper.initPaylevenWrapper(PayActivity.this, paylevenInAppClient);
    }

    private String getEmail() {
        return emailEditText.getText().toString();
    }

    private String emailValidation(String email) {
        if (null == email || email.isEmpty()) {
            return getString(R.string.missing_email);
        }
        final Pattern pattern = Pattern.compile(REGEX_PATTERN, Pattern.CASE_INSENSITIVE);

        if (!pattern.matcher(email).matches()) {
            return getString(R.string.invalid_email);
        }
        return null;

    }
    
    private void initUI() {
        paymentInstrumentsListView = (ListView) findViewById(R.id.payment_instruments);
        emptyListTextView = (TextView) findViewById(R.id.empty);
        paymentInstrumentsListView.setEmptyView(emptyListTextView);
        useCaseEditText = (EditText) findViewById(R.id.use_case_edittext);

        final Button getPaymentInstruments = (Button) findViewById(R.id.get_PI_button);
        getPaymentInstruments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaymentInstruments();
            }
        });
    }

    private void getPaymentInstruments() {
        showProgressDialog();

        String useCase = useCaseEditText.getText().toString();

        paylevenWrapper.getPaymentInstruments(useCase, new GetPaymentInstrumentsListener() {
            @Override
            public void onPaymentInstrumentsRetrieved(List<PaymentInstrument> paymentInstruments) {
                dismissProgressDialog();
                PayActivity.this.paymentInstruments = paymentInstruments;
                updateListView();
            }

            @Override
            public void onPaymentInstrumentsRetrieveFailed(Throwable throwable) {
                dismissProgressDialog();
                paymentInstruments.clear();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
                String errorText;
                if (throwable instanceof CallbackError) {
                    errorText = ((CallbackError) throwable).getErrorCode()
                            + " " + throwable.getMessage();
                } else {
                    errorText = throwable.getMessage();
                }
                emptyListTextView.setText(errorText);
            }
        });
    }

    private void updateListView() {
        listAdapter = new PayListAdapter(
                PayActivity.this,
                paymentInstruments,
                PayActivity.this);
        paymentInstrumentsListView.setAdapter(listAdapter);
        if (paymentInstruments.size() == 0) {
            emptyListTextView.setText(getString(R.string.no_pi));
        }
    }


    @Override
    public void removePaymentInstrument(final PaymentInstrument paymentInstrument) {
        showProgressDialog();
        paylevenWrapper.removePaymentInstrument(
                paymentInstrument,
                new RemovePaymentInstrumentListener() {
                    @Override
                    public void onPaymentInstrumentRemovedSuccessfully() {
                        dismissProgressDialog();
                        // request again the list of payment instruments to make sure that the
                        // payment instrument was disabled
                        getPaymentInstruments();
                    }

                    @Override
                    public void onPaymentInstrumentRemoveFailed(Throwable throwable) {
                        dismissProgressDialog();
                        Toast.makeText(PayActivity.this,
                                getString(R.string.pi_removing_failed,
                                        throwable.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
