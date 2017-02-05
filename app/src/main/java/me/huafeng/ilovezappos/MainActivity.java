package me.huafeng.ilovezappos;

import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private String querySent;
    private ClipboardManager clipboard = null;

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

        TextView text_share = (TextView) findViewById(R.id.text_share);
        text_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                String keyTerm = getString(R.string.key_sentence) + querySent;
                intent.putExtra(Intent.EXTRA_TEXT, keyTerm);
                startActivity(Intent.createChooser(intent, "Share to"));
            }
        });


        SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // listen clipboard
        if (clipboard == null ) {
            clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }
        if (clipboard.hasPrimaryClip()) {
            if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData clipData = clipboard.getPrimaryClip();
                CharSequence copyText = clipData.getItemAt(0).getText();
                String keySentence = getString(R.string.key_sentence);
                if ((copyText.length() > keySentence.length()) &&
                        (copyText.subSequence(0,keySentence.length()).toString().equals(keySentence))) {
                    startSearch(copyText.subSequence(keySentence.length(),copyText.length()).toString());
                }
            }
//            for (int i = 0; i < count; ++i) {
//                int count = clipData.getItemCount();
//                ClipData.Item item = clipData.getItemAt(i);
//                CharSequence str = item.coerceToText(MainActivity.this);
//                Log.i("User", "item : " + i + ": " + str);
//            }
        }
    }

    public void startSearch(final String query) {
        if (query.length() != 0){
            Call<ItemBundle> call = myZappoSerice.getUser(query, "b743e26728e16b81da139182bb2094357c31d331");
            call.enqueue(new Callback<ItemBundle>() {
                @Override
                public void onResponse(Call<ItemBundle> call, Response<ItemBundle> response) {
                    ItemBundle body = response.body();
                    if (body.getResults().size()==0){
                        Toast.makeText(MainActivity.this, "Oh, nothing comes. Try other key words", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Result firstResult = body.getResults().get(0);
                    updateView(firstResult);
                    querySent = query;
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
            SearchView searchView = (SearchView) findViewById(R.id.search);
            searchView.clearFocus();
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
