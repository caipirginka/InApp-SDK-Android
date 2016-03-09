package de.payleven.inappdemo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import de.payleven.inappsdk.CreditCardPaymentInstrument;
import de.payleven.inappsdk.PaymentInstrument;

/**
 * Adapter for the list of payment instruments
 */
public class PayListAdapter extends ArrayAdapter<PaymentInstrument> {

    public interface AddPaymentInstrumentButtonListener {
        void addPaymentInstrument(int index);
    }

    public interface RemovePaymentInstrumentButtonListener {
        void removePaymentInstrument(final PaymentInstrument paymentInstrument);
    }

    private LayoutInflater inflater;
    private Context context;

    private AddPaymentInstrumentButtonListener addPaymentInstrumentButtonListener;
    private RemovePaymentInstrumentButtonListener removePaymentInstrumentButtonListener;

    private List<PaymentInstrument> objects;

    public PayListAdapter(
            Context context,
            List<PaymentInstrument> objects,
            AddPaymentInstrumentButtonListener addPaymentInstrumentButtonListener,
            RemovePaymentInstrumentButtonListener removePaymentInstrumentButtonListener) {

        super(context, R.layout.pay_item, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.addPaymentInstrumentButtonListener = addPaymentInstrumentButtonListener;
        this.removePaymentInstrumentButtonListener = removePaymentInstrumentButtonListener;
        this.objects = objects;
    }


    @Override
    public int getViewTypeCount() {
    	return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
    	return getItem(position) == null ? 0 : 1;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	int type = getItemViewType(position);
    	
        if (convertView == null) {
    		if(type == 0) {
                convertView = inflater.inflate(R.layout.pay_empty_item, parent, false);
                convertView.setTag(new ViewEmptyHolder(convertView));
    		} else {
                convertView = inflater.inflate(R.layout.pay_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
    		}
        }

        final PaymentInstrument paymentInstrument = getItem(position);

        if(paymentInstrument == null)
        	setupEmptyItem(position, (ViewEmptyHolder) convertView.getTag());
        else
        	setupItem((ViewHolder) convertView.getTag(),paymentInstrument);
        	
        return convertView;
    }

    private void setupEmptyItem(final int index, ViewEmptyHolder holder) {
        holder.addPaymentInstrument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaymentInstrumentButtonListener.addPaymentInstrument(index);
            }
        });
	}

	private void setupItem(ViewHolder holder, final PaymentInstrument paymentInstrument) {
        setupPaymentInstrumentView(holder.pan, paymentInstrument);

        int textColor = android.R.color.black;
        if (PaymentInstrument.Status.INVALID == paymentInstrument.getStatus()) {
            textColor = android.R.color.darker_gray;
        }

        holder.pan.setTextColor(context.getResources().getColor(textColor));

        int blockedVisibility = paymentInstrument.isBlocked() ? View.VISIBLE : View.GONE;
        holder.blocked.setVisibility(blockedVisibility);

        holder.removePaymentInstrument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePaymentInstrumentButtonListener.removePaymentInstrument(paymentInstrument);
            }
        });
	}

	/**
     * Construct a description for a payment instrument based on the type of the payment instrument
     *
     * @param paymentInstrument
     * @return
     */
    private void setupPaymentInstrumentView(
            TextView pan,
            final PaymentInstrument paymentInstrument) {
        switch (paymentInstrument.getPaymentInstrumentType()) {
            case CC:
                final CreditCardPaymentInstrument creditCardPaymentInstrument =
                        (CreditCardPaymentInstrument) paymentInstrument;
                pan.setText(String.format("XXXX-%s %s/%s", 
                		creditCardPaymentInstrument.getPanTruncated(),
                		creditCardPaymentInstrument.getExpiryMonth(),
                		creditCardPaymentInstrument.getExpiryYear()));
        }
    }


    private static class ViewEmptyHolder {
        Button addPaymentInstrument;

        private ViewEmptyHolder(View itemView) {
            addPaymentInstrument = (Button) itemView.findViewById(
                    R.id.add_payment_instrument);
        }
    }

    private static class ViewHolder {
        TextView pan;
        TextView blocked;
        View usePaymentInstrument;
        View removePaymentInstrument;

        private ViewHolder(View itemView) {
            pan = (TextView) itemView.findViewById(R.id.pan);
            blocked = (TextView) itemView.findViewById(R.id.blocked);
            usePaymentInstrument = (View) itemView.findViewById(
                    R.id.use_payment_instrument);
            removePaymentInstrument = (View) itemView.findViewById(
                    R.id.remove_payment_instrument);
        }
    }

    public List<PaymentInstrument> getObjects() {
        return objects;
    }
}
