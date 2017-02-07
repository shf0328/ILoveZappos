package me.huafeng.ilovezappos;

import android.app.FragmentManager;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import com.squareup.picasso.Picasso;

import me.huafeng.ilovezappos.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // use a self defined class BundleData to store
    // persist data: api, search result, search query
    // in case they lose when in behaivor like rotation
    private BundleData bd;
    private RetainedFragment dataFragment;


    final Handler handler = new Handler();
    SearchView mySearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for screen rotation and maintain activity
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");
        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();

            // initial retrofit api
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.zappos.com/")
                    .build();
            ZapposApi myZappoSerice = retrofit.create(ZapposApi.class);
            // initial search result
            Result result = new Result();

            bd = new BundleData();
            bd.setQuery(null);
            bd.setMyZappoSerice(myZappoSerice);
            bd.setResult(result);

            dataFragment.setData(bd);
        }else {
            bd = dataFragment.getBundleData();
        }



        // data binding
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setItem(bd.getResult());
        if (bd.getResult().getBrandName() == null){
            hideCartAndCard();
        }else {
            showCartAndCard();
        }

        // enable click url
        TextView text_link = (TextView) findViewById(R.id.text_item_url);
        text_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation((TextView) v);
                startWebBrower();
            }
        });

        // enable click share
        TextView text_share = (TextView) findViewById(R.id.text_share);
        text_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation((TextView) v);
                shareThisItem();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mySearchView = (SearchView) MenuItemCompat.getActionView(item);
        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // detect sharelink
        Intent intent = getIntent();
        if (intent != null) {
            String dataString = intent.getDataString();
            if(dataString!=null){
                Log.v("user", dataString);
                String query = dataString.substring(getString(R.string.key_sentence).length());
                if((bd.getQuery()== null) || (bd.getQuery().equals(query))) {
                    startSearch(query);
                }
            }

        }
    }

    public void hideCartAndCard() {
        View card = findViewById(R.id.card_view);
        card.setVisibility(View.GONE);
        View cart = findViewById(R.id.floatingActionButton);
        cart.setVisibility(View.GONE);
    }

    public void showCartAndCard() {
        View card = findViewById(R.id.card_view);
        card.setVisibility(View.VISIBLE);
        View cart = findViewById(R.id.floatingActionButton);
        cart.setVisibility(View.VISIBLE);
    }

    public void startSearch(final String query) {
        if (query.length() != 0){
            // call progress bar
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            Call<ItemBundle> call = bd.getMyZappoSerice().getUser(query, "b743e26728e16b81da139182bb2094357c31d331");
            call.enqueue(new Callback<ItemBundle>() {
                @Override
                public void onResponse(Call<ItemBundle> call, Response<ItemBundle> response) {
                    // retrieve result
                    ItemBundle body = response.body();
                    if (body.getResults().size()==0){
                        Toast.makeText(MainActivity.this, "Oh, nothing comes. Try other key words", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Result firstResult = body.getResults().get(0);
                    // update data and view
                    updateView(firstResult);
                    bd.setQuery(query);
                    showCartAndCard();

                    // leave focus to show all data
                    mySearchView.clearFocus();

                }

                @Override
                public void onFailure(Call<ItemBundle> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Oh, the network might have some problem", Toast.LENGTH_SHORT).show();
                    Log.i("Api Response: ", t.getMessage());
                }
            });

            // close progress bar
            progressBar.setVisibility(View.GONE);
        }else {
            Toast.makeText(this, "you need to type something to search", Toast.LENGTH_SHORT).show();
        }
    }

    public void addToCart(View view) {
        hideCartAndCard();
        Toast.makeText(this, "you add it to cart", Toast.LENGTH_SHORT).show();
        bd.getResult().setBrandName(null);
        mySearchView.setQuery("", false);
    }

    // search over and update view
    public void updateView(Result firstResult) {
        // notify data binding
        bd.getResult().setBrandName(firstResult.getBrandName());
        bd.getResult().setProductName(firstResult.getProductName());
        bd.getResult().setOriginalPrice(firstResult.getOriginalPrice());
        bd.getResult().setPercentOff(firstResult.getPercentOff());
        bd.getResult().setPrice(firstResult.getPrice());
        bd.getResult().setProductUrl(firstResult.getProductUrl());
        bd.getResult().setThumbnailImageUrl(firstResult.getThumbnailImageUrl());
    }

    // some adapters to execute complex behavior
    @BindingAdapter({"bind:visible"})
    public static void changeVisiblity(TextView view, String percentOff) {
        if (percentOff == null) {
            return;
        }
        if (percentOff.equals("0%")){
            view.setVisibility(View.GONE);
        }else {
            view.setVisibility(View.VISIBLE);
            view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @BindingAdapter({"bind:discountColor"})
    public static void changeColor(TextView view, String percentOff) {
        if (percentOff == null) {
            return;
        }
        if (percentOff.equals("0%")){
            view.setTextColor(0xFF757575);
        }else {
            view.setTextColor(0xFFFF4081);
        }

    }

    @BindingAdapter({"bind:thumbnailImageUrl"})
    public static void loadImage(ImageView view, String thumbnailImageUrl) {
        Picasso.with(view.getContext())
                .load(thumbnailImageUrl)
                .into(view);
    }


    public void startWebBrower(){
        if (bd.getResult().getProductUrl() != null){
            String url = bd.getResult().getProductUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    public void shareThisItem(){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check this");
        String keyTerm = getString(R.string.key_sentence) + bd.getQuery();
        intent.putExtra(Intent.EXTRA_TEXT, keyTerm);
        startActivity(Intent.createChooser(intent, "Share to"));
    }

    // simple click effect for share and webbroswer
    public void animation(final TextView tv) {
        tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                } catch (Exception e) {
                    // Auto-generated catch block
                    Toast.makeText(MainActivity.this, "Oh, something wrongs", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.postDelayed(runnable, 200);
    }
}
