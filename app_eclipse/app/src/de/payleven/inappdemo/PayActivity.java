package de.payleven.inappdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.payleven.inappsdk.PaylevenFactory;
import de.payleven.inappsdk.PaylevenInAppClient;
import de.payleven.inappsdk.PaymentInstrument;
import de.payleven.inappsdk.errors.CallbackError;
import de.payleven.inappsdk.listeners.GetPaymentInstrumentsListener;
import de.payleven.inappsdk.listeners.RemovePaymentInstrumentListener;

/**
 * Activity used to request and display the list of payment instruments for the active user
 * depending on the use case
 */
public class PayActivity extends ToplevelActivity implements
		PayListAdapter.AddPaymentInstrumentButtonListener,
        PayListAdapter.RemovePaymentInstrumentButtonListener {

    private ListView paymentInstrumentsListView;
    private TextView emptyListTextView;

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
        
        paymentInstrumentsListView = (ListView) findViewById(R.id.payment_instruments);
        emptyListTextView = (TextView) findViewById(R.id.empty);
        paymentInstrumentsListView.setEmptyView(emptyListTextView);
        
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
                            getPaymentInstruments();
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
    
    private void getPaymentInstruments() {
    	boolean hasUserToken = paylevenWrapper.hasUserToken();
        paymentInstruments.clear();
        for (int i = 0; i < 4; i++) {
            paymentInstruments.add(null);
            if(hasUserToken)
            	getPaymentInstruments(i);			//do it only if userToken is present
        }
        
        updateListView();
    }
    
    private void getPaymentInstruments(final int index) {
        showProgressDialog();
        paylevenWrapper.getPaymentInstruments(String.valueOf(index), new GetPaymentInstrumentsListener() {
            @Override
            public void onPaymentInstrumentsRetrieved(List<PaymentInstrument> paymentInstruments) {
        		dismissProgressDialog();                	
        		if(paymentInstruments.size() > 0) {
	                PayActivity.this.paymentInstruments.set(index, paymentInstruments.get(0));
	                updateListView();
        		}
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
                PayActivity.this,
                PayActivity.this);
        paymentInstrumentsListView.setAdapter(listAdapter);
//        if (paymentInstruments.size() == 0) {
//            emptyListTextView.setText(getString(R.string.no_pi));
//        }
    }

    @Override
    public void addPaymentInstrument(int index) {
        Intent intent = new Intent(PayActivity.this, AddCreditCardActivity.class);
        intent.putExtra(AddCreditCardActivity.EXTRA_USECASE, String.valueOf(index));
        startActivityForResult(intent, 0);
    }
    
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
    	super.onActivityResult(arg0, arg1, arg2);
    	
        getPaymentInstruments();
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
