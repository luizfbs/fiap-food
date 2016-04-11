package br.com.fiap.fiapfood.adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.fragments.FormFragment;
import br.com.fiap.fiapfood.helpers.BitmapHelper;
import br.com.fiap.fiapfood.models.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private final List<Restaurant> mValues;
    private int thumbHeight = 120;

    public RestaurantAdapter(List<Restaurant> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if(mValues.get(position).pictureURL != null) {
            holder.picture.setImageBitmap(BitmapHelper.reduce(mValues.get(position).pictureURL, thumbHeight));
        }else{
            holder.picture.setImageResource(R.mipmap.ic_pin);
        }

        holder.name.setText(mValues.get(position).name);

        if(mValues.get(position).phone.length() > 0){
            holder.phone.setVisibility(View.VISIBLE);
            holder.phone.setText(mValues.get(position).phone);
        }else{
            holder.phone.setVisibility(View.GONE);
        }

        if(mValues.get(position).type != null){
            holder.type.setVisibility(View.VISIBLE);
            holder.type.setText(mValues.get(position).type.name);
        }else{
            holder.type.setVisibility(View.GONE);
        }

        if(holder.cost != null){
            holder.cost.setVisibility(View.VISIBLE);
            holder.cost.setText(mValues.get(position).cost.name);
        }else{
            holder.cost.setVisibility(View.GONE);
        }

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity activity = (MainActivity) holder.mView.getContext();
                FormFragment fragment = new FormFragment();

                Bundle args=new Bundle();
                args.putLong(FormFragment.BUNDLE_RESTAURANT_TO_EDIT_KEY, holder.mItem.getId());

                fragment.setArguments(args);
                activity.changeFragment(fragment);

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView picture;
        public final TextView name;
        public final TextView phone;
        public final TextView type;
        public final TextView cost;
        public Restaurant mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            picture = (ImageView) view.findViewById(R.id.picture);
            name = (TextView) view.findViewById(R.id.name);
            phone = (TextView) view.findViewById(R.id.phone);
            type = (TextView) view.findViewById(R.id.type);
            cost = (TextView) view.findViewById(R.id.cost);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
