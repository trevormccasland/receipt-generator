package com.example.trevor.listtothemax;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Float;
import java.util.List;


public class ListActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener{

    private static final String TAG = "TESSERACT" ;
    private Uri imageUri;//Class to hold address of the image for the bitmap
    static final int SIZE=140;//Specifies bitmap size
    static final String DESC_KEY = "desc";//Description key used in map for list items
    static final String PRICE_KEY = "price";//Price key used in map for list items
    static final String QUANTITY_KEY = "quantity";//Quantity key used in map for list items
    ListView lv;//Variable to hold the listview in the app
    static TextView subTotal;//Textview to hold the subtotal in the app
    Context context;
    ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    Bitmap bitmap= null;
    final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    private static final String TESSBASE_PATH = "/mnt/sdcard/tesseract/";
    private static final String DEFAULT_LANGUAGE = "eng";
    private static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/" + DEFAULT_LANGUAGE
            + ".traineddata";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Required code to set view
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_phone);
        context=this;

/*        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        askForInfo(getWindow().getDecorView());*/

    }



    private void addLineItem(Bitmap bitmap, String desc, String price, String quantity){
        //Hashmap to hold data to be insterted into list
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(DESC_KEY,desc);
        map.put(PRICE_KEY, price);
        map.put(QUANTITY_KEY, quantity);
        //Add bitmap to the photo gallery of the app
        if(bitmap == null){
            //Create a dummy bitmap
            bitmaps.add(Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.RGB_565));
        }
        else{
            //Scale bitmap
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, false);
            bitmaps.add(scaledBitmap);
        }

        //Get Subtotal from view
        subTotal =(TextView) findViewById(R.id.subtotal);
        String previousAmount;
        previousAmount =  subTotal.getText().toString().substring(11);

        //If the total is empty, only add the price of the item
        if(!tryParseFloat(price))
        {
            alert("The price you entered was too much.");
        }
        else if(!tryParseFloat(quantity))
        {
            alert("The quantity you entered was too much.");
        }

        float linePrice = Float.parseFloat(price)*Float.parseFloat(quantity);
        if(!tryParseFloat(Float.toString(linePrice)))
        {
            alert("The entered quantity and price together is too much.");
        }
        else if(previousAmount.equals("") ||  previousAmount.isEmpty()){
            subTotal.setText("Subtotal: $" + String.format("%.2f" ,(Float.parseFloat(price) * Integer.parseInt(quantity))));
            lv=(ListView) findViewById(R.id.listView);
            lv.setAdapter(new CustomAdapter(this, list, bitmaps));
            //Add data to list
            list.add(map);

        }
        else if(!tryParseFloat(Float.toString(Float.parseFloat(previousAmount)+Float.parseFloat(price) * Integer.parseInt(quantity)))) {
            alert("Your total is over our maximum value, this item will not be added. Maybe you should try putting something back.");
        }
        else{//Add the price to the current total
            subTotal.setText("Subtotal: $" + String.format("%.2f" ,Float.parseFloat(previousAmount)+(Float.parseFloat(price) * Integer.parseInt(quantity))));
            lv=(ListView) findViewById(R.id.listView);
            lv.setAdapter(new CustomAdapter(this, list, bitmaps));
            //Add data to list
            list.add(map);
        }

    }

    void alert(String message)
    {
        new AlertDialog.Builder(this)
                .setTitle("Warning!")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok,null)
                .setNegativeButton(android.R.string.no, null).show();
    }

    boolean tryParseFloat(String value)
    {
        try
        {
            Float.parseFloat(value);
            return true;
        } catch(NumberFormatException nfe)
        {
            Log.e("Value:" + value,nfe.toString());
            return false;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Alert box that prompts the user to exit
        new AlertDialog.Builder(this)
                .setTitle("Warning!")
                .setMessage("Do you really want to exit? Your Receipt will be erased. Use the Home key to preserve this session.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        System.exit(0);
                    }})
                .setNegativeButton("No", null).show();

        return true;
    }

    private void takePhoto(View v){
        //free memory from previous bitmap
        if(bitmap!=null) {
            bitmap.recycle();
            bitmap = null;
        }
        //Class used to reference another activity inside an activity
        Intent intent = new Intent(this,PhoneActivity.class);
        int TAKE_PICTURE = 1;
        startActivityForResult(intent, TAKE_PICTURE);

    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);

        if(resultCode == Activity.RESULT_OK){

            String filename = intent.getStringExtra("image");
            try {
                FileInputStream is = this.openFileInput(filename);
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "HERE");
            OCR(bitmap);

        }

    }
    private void copyAssets() {

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }
        File newFile = new File(DATA_PATH + "tessdata/" + DEFAULT_LANGUAGE+ ".traineddata");
        if (!newFile.exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + DEFAULT_LANGUAGE + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + DEFAULT_LANGUAGE + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + DEFAULT_LANGUAGE + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + DEFAULT_LANGUAGE + " traineddata " + e.toString());
            }
        }

    }

    String text;
    void OCR(Bitmap bmp) {
        TessBaseAPI baseApi = new TessBaseAPI();
        copyAssets();
        try {
            baseApi.init(DATA_PATH, DEFAULT_LANGUAGE);
        }
        catch (IllegalArgumentException E){
            Log.e(TAG, E.getMessage()+": "+DATA_PATH);

        }
        baseApi.setImage(bmp);
        final String outputText = baseApi.getUTF8Text();
        text = outputText;
        Log.i(TAG,outputText);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        takePhoto(v);
        askForInfo(v);
    }

    public void askForInfo(View v){

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //Gets the view data from he xml page (what it will look like)
        View dialogInfo = View.inflate(this, R.layout.dialog_additeminfo, null);
        //Set up these fields to get the data from the xml page, also final because its outside the scope
        final EditText Description = (EditText)dialogInfo.findViewById(R.id.item_Description);
        final EditText Price = (EditText)dialogInfo.findViewById(R.id.item_Price);
        final EditText Quantity = (EditText)dialogInfo.findViewById(R.id.quantity);
        Description.setText(text);
        //Creates a new builder for this page (main)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Sets the view info for the builder
        builder.setView(dialogInfo);
        //Has the builder build a button
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {//When the button is clicked
                String result = Description.getText().toString();
                String price = Price.getText().toString();
                String quantity = Quantity.getText().toString();
                if(result.equals("") || result.isEmpty()){
                    result = "No Description";
                }
                if(price.equals("") || price.isEmpty()){
                    price = "0.00";
                }
                else{
                    price = String.format("%.2f" , Float.parseFloat(Price.getText().toString()));
                }
                if(quantity.equals("") || quantity.isEmpty()){
                    quantity = "1";
                }

                addLineItem(bitmap,result, price, quantity);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //return to view
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        //position is the index of the list when touched
        // Log the item's position and contents
        //log logs the data .d is the debug level shows up in teh debug screen
        // to the console in Debug
        Log.d("test android", position + ": " + list.get(position));

        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //Gets the view data from he xml page (what it will look like)
        View dialogInfo = View.inflate(this, R.layout.dialog_iteminfo, null);
        //Set up these fields to get the data from the xml page, also final because its outside the scope
        final EditText Description = (EditText)dialogInfo.findViewById(R.id.item_Description);
        final EditText Price = (EditText)dialogInfo.findViewById(R.id.item_Price);
        final EditText Quantity = (EditText)dialogInfo.findViewById(R.id.quantity);
        //Creates a new builder for this page (main)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Sets the view info for the builder
        builder.setView(dialogInfo);
        //Has the builder build a button
        final String result = list.get(position).get("description");
        Description.setText(result, TextView.BufferType.EDITABLE);
        final String price = list.get(position).get("price");
        Price.setText(price, TextView.BufferType.EDITABLE);
        final String quantity = list.get(position).get("quantity");
        Price.setText(quantity, TextView.BufferType.EDITABLE);
        list.remove(position);
        builder.setPositiveButton("@string/ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {//When the button is clicked
                String newResult = Description.getText().toString();
                String newPrice = Price.getText().toString();
                String newQuantity = Quantity.getText().toString();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("description", newResult);
                map.put("price", newPrice);
                map.put("quantity", newQuantity);
                list.add(position, map);
            }
        }).setNegativeButton("@string/cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //return to view
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}