package de.payleven.inappdemo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import de.payleven.inappsdk.PaylevenInAppClient;
import de.payleven.inappsdk.PaymentInstrument;
import de.payleven.inappsdk.PaymentInstrumentAction;
import de.payleven.inappsdk.listeners.AddPaymentInstrumentListener;
import de.payleven.inappsdk.listeners.EditPaymentInstrumentListener;
import de.payleven.inappsdk.listeners.GetPaymentInstrumentsListener;
import de.payleven.inappsdk.listeners.RemovePaymentInstrumentListener;
import de.payleven.inappsdk.listeners.SetPaymentInstrumentsOrderListener;

/**
 * Wrapper for the payleven API
 */
public class PaylevenWrapper {

//    public static final String API_KEY = BuildConfig.PAYLEVEN_API_KEY;
    public static final String API_KEY = "55b501b74bce4d0ca51df8b70c2ae38a";

    private static final String SHARED_PREFERENCES_FILE =
            "de.payleven.inappdemo.SHARED_PREFERENCES_FILE";

    private static PaylevenWrapper instance;

    private PaylevenInAppClient mPaylevenInAppClient;
    private String email;

    private Context applicationContext;

    public static PaylevenWrapper getInstance() {
        if (null == instance) {
            instance = new PaylevenWrapper();
        }
        return instance;
    }

    public void initPaylevenWrapper(Context context, PaylevenInAppClient paylevenInAppClient) {
        this.applicationContext = context.getApplicationContext();
        this.mPaylevenInAppClient = paylevenInAppClient;
    }

    /**
     * Save the user token associated to the email address. For the sake of the simplicity of the
     * sample app, we're just saving the user token in the shared preferences
     * Since the user token will be used to make payments on behalf of the user, we strongly
     * recommend saving the user token in your backend
     *
     * @param userToken
     */
    public void saveUserToken(final String userToken) {
        saveInSharedPreferences(email, userToken);
    }

    private void saveInSharedPreferences(final String email, final String userToken) {
        SharedPreferences sharedPref = applicationContext.getSharedPreferences(
                SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(email, userToken);
        editor.commit();
    }

    public boolean hasUserToken() {
    	return getUserToken() != null;
    }
    
    private String getUserToken() {
        if (null != email) {
            return getUserTokenFromSharedPreferences(email);
        }
        return null;
    }

    private String getUserTokenFromSharedPreferences(final String email) {
        SharedPreferences sharedPref = applicationContext.getSharedPreferences(
                SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(email, null);
    }

    /**
     * Adds the payment instrument to the current user token, in the use case selected
     *
     * @param paymentInstrument the payment instrument that will be added to the user token
     * @param useCase           the use case to which the payment instrument is added
     * @param listener          listener that reacts on the response of the Payleven InApp Client for adding
     *                          a payment instrument
     */
    public void addPaymentInstrument(final PaymentInstrument paymentInstrument,
                                     final String useCase,
                                     AddPaymentInstrumentListener listener) {

        String userToken = getUserToken();
        if (null == userToken) {
            mPaylevenInAppClient.createUserTokenWithPaymentInstrument(
                    email,
                    paymentInstrument,
                    useCase,
                    listener);
        } else {
            mPaylevenInAppClient.addPaymentInstrument(userToken, paymentInstrument, useCase,
                    listener);
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * Request to the {@link de.payleven.inappsdk.PaylevenInAppClient} the list of payment
     * instruments for the current user, for a specific use case. The result of the request is
     * handled by the listener implementation
     *
     * @param listener listener that handles the result of the request
     */
    public void getPaymentInstruments(final String useCase,
                                      GetPaymentInstrumentsListener listener) {
        mPaylevenInAppClient.getPaymentInstrumentsList(getUserToken(), useCase, listener);
    }

    public void setPaymentInstrumentsOrder(final List<PaymentInstrument> paymentInstruments,
                                           final String useCase,
                                           SetPaymentInstrumentsOrderListener listener) {
        mPaylevenInAppClient.setPaymentInstrumentsOrder(
                getUserToken(),
                useCase,
                paymentInstruments,
                listener);
    }

    /**
     * Request the {@link de.payleven.inappsdk.PaylevenInAppClient} to edit a payment instrument
     * that is associated to the current user token
     * The result of this request is notified by the listener
     *
     * @param paymentInstrument payment instrument to be disabled
     * @param listener          listener that notifies about the result of the operation
     */
    public void editPaymentInstrument(final PaymentInstrument paymentInstrument,
                                      final PaymentInstrumentAction action,
                                      final String cvv,
                                      EditPaymentInstrumentListener listener) {
        mPaylevenInAppClient.editPaymentInstrument(getUserToken(), paymentInstrument,
                action, cvv, listener);
    }

    /**
     * Request the {@link de.payleven.inappsdk.PaylevenInAppClient} to edit a payment instrument
     * that is associated to the current user token
     * The result of this request is notified by the listener
     *
     * @param paymentInstrument payment instrument to be disabled
     * @param listener          listener that notifies about the result of the operation
     */
    public void removePaymentInstrument(final PaymentInstrument paymentInstrument,
                                        RemovePaymentInstrumentListener listener) {
        mPaylevenInAppClient.removePaymentInstrument(getUserToken(), paymentInstrument, listener);
    }
}
