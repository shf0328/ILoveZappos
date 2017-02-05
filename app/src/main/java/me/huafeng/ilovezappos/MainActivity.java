package me.huafeng.ilovezappos;

import android.app.FragmentManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.huafeng.ilovezappos.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Result item;
    private ZapposApi myZappoSerice;
    private RetainedFragment dataFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
            // load the data from the web
            item = new Result();
            dataFragment.setData(item);
        }else {
            item = dataFragment.getData();
        }



        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setItem(item);
        if (item.getBrandName() == null){
            Log.v("soon you", "wont");
            View card = findViewById(R.id.card_view);
            card.setVisibility(View.GONE);
            View cart = findViewById(R.id.floatingActionButton);
            cart.setVisibility(View.INVISIBLE);
        }else {
            Log.v("now you", " see me");
            View card = findViewById(R.id.card_view);
            card.setVisibility(View.VISIBLE);
            View cart = findViewById(R.id.floatingActionButton);
            cart.setVisibility(View.VISIBLE);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.zappos.com/")
                .build();
        myZappoSerice = retrofit.create(ZapposApi.class);


        TextView text_link = (TextView) findViewById(R.id.text_item_url);
        text_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebBrower();
            }
        });
    }


    public void startSearch(View view) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String query = editText.getText().toString();
        if (query.length() != 0){
            Call<ItemBundle> call = myZappoSerice.getUser(query, "b743e26728e16b81da139182bb2094357c31d331");
            call.enqueue(new Callback<ItemBundle>() {
                @Override
                public void onResponse(Call<ItemBundle> call, Response<ItemBundle> response) {
                    ItemBundle body = response.body();
                    Result firstResult = body.getResults().get(0);
                    updateView(firstResult);
                    // display card and cart
                    View card = findViewById(R.id.card_view);
                    card.setVisibility(View.VISIBLE);
                    View cart = findViewById(R.id.floatingActionButton);
                    cart.setVisibility(View.VISIBLE);
                }
                @Override
                public void onFailure(Call<ItemBundle> call, Throwable t) {
                    Log.i("onResponse:   =", t.getMessage());
                }
            });
            View card = findViewById(R.id.card_view);
            card.requestFocus();
            View viewFocus = this.getCurrentFocus();
            if (viewFocus != null) {
                InputMethodManager imManager = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
                imManager.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            }
        }else {
            Toast.makeText(this, "you need to type something to search", Toast.LENGTH_SHORT).show();
        }
    }

    public void addToCart(View view) {
        View card = (View) findViewById(R.id.card_view);
        card.setVisibility(View.GONE);
        View cart = (View) findViewById(R.id.floatingActionButton);
        cart.setVisibility(View.GONE);
        Toast.makeText(this, "you add it to cart", Toast.LENGTH_SHORT).show();
    }

    public void updateView(Result firstResult) {
        // notify data binding
        item.setBrandName(firstResult.getBrandName());
        item.setProductName(firstResult.getProductName());
        item.setOriginalPrice(firstResult.getOriginalPrice());
        item.setPercentOff(firstResult.getPercentOff());
        item.setPrice(firstResult.getPrice());
        item.setProductUrl(firstResult.getProductUrl());
        item.setThumbnailImageUrl(firstResult.getThumbnailImageUrl());
    }

    public void startWebBrower(){
        if (item.getProductUrl() != null){
            String url = item.getProductUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

}
