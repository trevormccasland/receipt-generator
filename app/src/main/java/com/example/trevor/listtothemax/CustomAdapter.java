package com.example.trevor.listtothemax;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapter extends BaseAdapter{

    Context context;
    ArrayList<HashMap<String,String>> result;
    ArrayList<Bitmap> images;
    private static LayoutInflater inflater=null;
    public CustomAdapter(ListActivity mainActivity, ArrayList<HashMap<String,String>> map, ArrayList<Bitmap> bitmaps) {
        // TODO Auto-generated constructor stub
        result=map;
        context=mainActivity;
        images = bitmaps;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        TextView tv2;
        TextView tv3;
        ImageView img;
        Button remove_button;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder=new Holder();
        View rowView;
        HashMap<String,String> data = new HashMap<String,String>();
        Bitmap image;
        data = result.get(position);
        image = images.get(position);
        rowView=convertView;
        if(convertView==null)
            rowView = inflater.inflate(R.layout.mylist, null);
        holder.remove_button=(Button) rowView.findViewById(R.id.button_remove);
        holder.remove_button.setTag(position);
        rowView.setTag(position);
        holder.tv=(TextView) rowView.findViewById(R.id.firstLine);
        holder.tv2=(TextView) rowView.findViewById(R.id.secondLine);
        holder.tv3=(TextView) rowView.findViewById(R.id.thirdLine);
        holder.img=(ImageView) rowView.findViewById(R.id.icon);
        holder.tv.setText(data.get(ListActivity.DESC_KEY));
        holder.tv2.setText(data.get(ListActivity.PRICE_KEY));
        holder.tv3.setText(data.get(ListActivity.QUANTITY_KEY));

        try {
            holder.img.setImageBitmap(image);
        }
        catch (NumberFormatException e)
        {
            Log.e("asdf",e.getMessage());
        }
        holder.remove_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Integer index = (Integer) v.getTag();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder((Activity) v.getContext());
                alertDialog.setTitle("Delete this item?");
                alertDialog.setMessage("Are you sure you want to delete this?");
                alertDialog.setPositiveButton(

                        "Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do the stuff..

                                ListActivity.subTotal.setText("Subtotal: $" + String.format("%.2f",(Float.parseFloat(ListActivity.subTotal.getText().toString().substring(11))
                                        - Float.parseFloat(result.get(index).get(ListActivity.PRICE_KEY))* Integer.parseInt(result.get(index).get(ListActivity.QUANTITY_KEY)))));
                                Log.e("data set: ", index + ":"+result.get(index));
                                holder.tv.setText("@string/total");
                                result.remove(index.intValue());
                                images.remove(index.intValue());
                                notifyDataSetChanged();
                            }
                        }
                );
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //return to view
                    }
                });
                alertDialog.show();
            }
        });

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Activity) v.getContext());
                View view = ((Activity) v.getContext()).getLayoutInflater().inflate(R.layout.dialog_additeminfo, null);
                builder.setView(view);
                final Integer index = (Integer)v.getTag();
                final HashMap<String,String> data = result.get(index);

                final TextView Title = (TextView)view.findViewById(R.id.dialog_title);
                Title.setText("Edit Item");
                final EditText Description = (EditText)view.findViewById(R.id.item_Description);
                final EditText Price = (EditText)view.findViewById(R.id.item_Price);
                final EditText Quantity = (EditText)view.findViewById(R.id.quantity);

                String description = data.get(ListActivity.DESC_KEY);
                Description.setText(description);
                String price = data.get(ListActivity.PRICE_KEY);
                Price.setText(price);
                String quantity = data.get(ListActivity.QUANTITY_KEY);
                Quantity.setText(quantity);
                Log.e("Price :",price);
                Log.e("Quantity :",quantity);
                Log.e("Desc :", description);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {//When the button is clicked
                        //Subtract current price
                        ListActivity.subTotal.setText("Subtotal: $" + String.format("%.2f",(Float.parseFloat(ListActivity.subTotal.getText().toString().substring(11))
                        - Float.parseFloat(result.get(index).get(ListActivity.PRICE_KEY))* Integer.parseInt(result.get(index).get(ListActivity.QUANTITY_KEY)))));
                        Log.d("test android", index + ": " + result.get(index));
                        //result.remove(position);
                        String newResult = Description.getText().toString();
                        String newPrice = Price.getText().toString();
                        String newQuantity = Quantity.getText().toString();

                        if(newResult.equals("") || newResult.isEmpty()){
                            newResult = "No Description";
                        }
                        if(newPrice.equals("") || newPrice.isEmpty()){
                            newPrice = "0.00";
                        }
                        else{
                            newPrice = String.format("%.2f" , Float.parseFloat(Price.getText().toString()));
                        }
                        if(newQuantity.equals("") || newQuantity.isEmpty()){
                            newQuantity = "1";
                        }
                        //final Holder holder=new Holder();
                        holder.tv.setText(newResult);
                        holder.tv2.setText(newPrice);
                        holder.tv3.setText(newQuantity);
                        data.put(ListActivity.DESC_KEY,newResult);
                        data.put(ListActivity.PRICE_KEY,newPrice);
                        data.put(ListActivity.QUANTITY_KEY,newQuantity);
                        //add the new price
                        ListActivity.subTotal.setText("Subtotal: $" + String.format("%.2f",(Float.parseFloat(ListActivity.subTotal.getText().toString().substring(11))
                                + Float.parseFloat(newPrice)* Integer.parseInt(newQuantity))));

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //return to view
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        });
        return rowView;
    }

}