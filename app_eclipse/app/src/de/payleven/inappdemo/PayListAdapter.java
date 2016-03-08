package de.payleven.inappdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.payleven.inappsdk.CreditCardPaymentInstrument;
import de.payleven.inappsdk.PaymentInstrument;
import de.payleven.inappsdk.PaymentInstrumentAction;

/**
 * Adapter for the list of payment instruments
 */
public class PayListAdapter extends ArrayAdapter<PaymentInstrument> {

    public interface RemovePaymentInstrumentButtonListener {
        void removePaymentInstrument(final PaymentInstrument paymentInstrument);
    }

    private LayoutInflater inflater;
    private Context context;

    private RemovePaymentInstrumentButtonListener removePaymentInstrumentButtonListener;

    private List<PaymentInstrument> objects;

    public PayListAdapter(
            Context context,
            List<PaymentInstrument> objects,
            RemovePaymentInstrumentButtonListener removePaymentInstrumentButtonListener) {

        super(context, R.layout.pay_item, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.removePaymentInstrumentButtonListener = removePaymentInstrumentButtonListener;
        this.objects = objects;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pay_item, parent, false);
            ViewHolder holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final PaymentInstrument paymentInstrument = getItem(position);

        setupPaymentInstrumentView(holder.pan, holder.expiryDate, paymentInstrument);

        int textColor = android.R.color.black;
        if (PaymentInstrument.Status.INVALID == paymentInstrument.getStatus()) {
            textColor = android.R.color.darker_gray;
        }

        holder.expiryDate.setTextColor(context.getResources().getColor(textColor));
        holder.pan.setTextColor(context.getResources().getColor(textColor));

        int blockedVisibility = paymentInstrument.isBlocked() ? View.VISIBLE : View.GONE;
        holder.blocked.setVisibility(blockedVisibility);

        holder.removePaymentInstrument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePaymentInstrumentButtonListener.removePaymentInstrument(paymentInstrument);
            }
        });

        return convertView;
    }

    /**
     * Construct a description for a payment instrument based on the type of the payment instrument
     *
     * @param paymentInstrument
     * @return
     */
    private void setupPaymentInstrumentView(
            TextView pan,
            TextView expiryDate,
            final PaymentInstrument paymentInstrument) {
        switch (paymentInstrument.getPaymentInstrumentType()) {
            case CC:
                final CreditCardPaymentInstrument creditCardPaymentInstrument =
                        (CreditCardPaymentInstrument) paymentInstrument;
                pan.setText(creditCardPaymentInstrument.getPanMasked());
                final String date = creditCardPaymentInstrument.getExpiryMonth()
                        + "/" + creditCardPaymentInstrument.getExpiryYear();
                expiryDate.setText(date);
        }
    }


    private static class ViewHolder {
        TextView pan;
        TextView expiryDate;
        TextView blocked;
        Button removePaymentInstrument;

        private ViewHolder(View itemView) {
            expiryDate = (TextView) itemView.findViewById(
                    R.id.expiry_date);
            pan = (TextView) itemView.findViewById(R.id.pan);
            blocked = (TextView) itemView.findViewById(R.id.blocked);
            removePaymentInstrument = (Button) itemView.findViewById(
                    R.id.remove_payment_instrument);
        }
    }

    public List<PaymentInstrument> getObjects() {
        return objects;
    }
}
