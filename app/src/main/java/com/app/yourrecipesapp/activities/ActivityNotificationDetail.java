package com.app.yourrecipesapp.activities;

import static com.app.yourrecipesapp.utils.Constant.BANNER_RECIPE_DETAIL;
import static com.app.yourrecipesapp.utils.Constant.INTERSTITIAL_ON_RECIPES_LIST;
import static com.app.yourrecipesapp.utils.Constant.NATIVE_AD_RECIPES_DETAIL;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.app.yourrecipesapp.BuildConfig;
import com.app.yourrecipesapp.R;
import com.app.yourrecipesapp.adapters.AdapterImage;
import com.app.yourrecipesapp.adapters.AdapterSuggested;
import com.app.yourrecipesapp.callbacks.CallbackRecipeDetail;
import com.app.yourrecipesapp.config.AppConfig;
import com.app.yourrecipesapp.databases.prefs.AdsPref;
import com.app.yourrecipesapp.databases.prefs.SharedPref;
import com.app.yourrecipesapp.databases.sqlite.DbHandler;
import com.app.yourrecipesapp.models.Images;
import com.app.yourrecipesapp.models.Recipe;
import com.app.yourrecipesapp.rests.RestAdapter;
import com.app.yourrecipesapp.utils.AdsManager;
import com.app.yourrecipesapp.utils.AppBarLayoutBehavior;
import com.app.yourrecipesapp.utils.Constant;
import com.app.yourrecipesapp.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNotificationDetail extends AppCompatActivity {

    private Call<CallbackRecipeDetail> callbackCall = null;
    private LinearLayout lyt_main_content;
    TextView txt_recipe_title, txt_category, txt_recipe_time, txt_total_views;
    LinearLayout lyt_view;
    ImageView thumbnail_video;
    private WebView webView;
    DbHandler dbHandler;
    CoordinatorLayout parent_view;
    private ShimmerFrameLayout lyt_shimmer;
    RelativeLayout lyt_suggested;
    private SwipeRefreshLayout swipe_refresh;
    SharedPref sharedPref;
    ImageButton btn_font_size, btn_favorite, btn_share;
    ViewPager viewPager;
    private String recipe_id;
    AdsPref adsPref;
    AdsManager adsManager;
    private String singleChoiceSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_recipe_detail);
        Tools.setNavigation(this);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        dbHandler = new DbHandler(this);

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(BANNER_RECIPE_DETAIL);
        adsManager.loadInterstitialAd(INTERSTITIAL_ON_RECIPES_LIST, adsPref.getInterstitialAdInterval());
        adsManager.loadNativeAd(NATIVE_AD_RECIPES_DETAIL);

        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setRefreshing(false);

        lyt_main_content = findViewById(R.id.lyt_main_content);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        parent_view = findViewById(R.id.lyt_content);

        thumbnail_video = findViewById(R.id.thumbnail_video);
        txt_recipe_title = findViewById(R.id.recipe_title);
        txt_category = findViewById(R.id.category_name);
        txt_recipe_time = findViewById(R.id.recipe_time);
        webView = findViewById(R.id.recipe_description);
        txt_total_views = findViewById(R.id.total_views);
        lyt_view = findViewById(R.id.lyt_view_count);

        btn_font_size = findViewById(R.id.btn_font_size);
        btn_favorite = findViewById(R.id.btn_favorite);
        btn_share = findViewById(R.id.btn_share);

        lyt_suggested = findViewById(R.id.lyt_suggested);

        Intent intent = getIntent();
        recipe_id = intent.getStringExtra("id");

        requestAction();

        swipe_refresh.setOnRefreshListener(() -> {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
            requestAction();
        });

        initToolbar();
        loadViewed();

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(this::requestPostData, 200);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI(sharedPref.getApiUrl()).getRecipeDetail(recipe_id);
        this.callbackCall.enqueue(new Callback<CallbackRecipeDetail>() {
            public void onResponse(Call<CallbackRecipeDetail> call, Response<CallbackRecipeDetail> response) {
                CallbackRecipeDetail responseHome = response.body();
                if (responseHome == null || !responseHome.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                displayAllData(responseHome);
                swipeProgress(false);
                lyt_main_content.setVisibility(View.VISIBLE);
            }

            public void onFailure(Call<CallbackRecipeDetail> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lyt_main_content.setVisibility(View.GONE);
        if (Tools.isConnect(ActivityNotificationDetail.this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        lyt_main_content.setVisibility(View.GONE);
    }

    private void displayAllData(CallbackRecipeDetail responseHome) {
        displayImages(responseHome.images);
        displayData(responseHome.post);
        displaySuggested(responseHome.related);
    }

    private void displayImages(final List<Images> list) {

        viewPager = findViewById(R.id.view_pager_image);
        final AdapterImage adapter = new AdapterImage(this, list);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < list.size()) {

                }
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        if (list.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        if (AppConfig.ENABLE_RTL_MODE) {
            viewPager.setRotationY(180);
        }

        adapter.setOnItemClickListener((view, p, position) -> {
            switch (p.content_type) {
                case "youtube": {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", p.video_id);
                    startActivity(intent);
                    break;
                }
                case "Url": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", p.video_url);
                    startActivity(intent);
                    break;
                }
                case "Upload": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", sharedPref.getApiUrl() + "/upload/video/" + p.video_url);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageSlider.class);
                    intent.putExtra("position", position);
                    intent.putExtra("recipe_id", recipe_id);
                    startActivity(intent);
                    break;
                }
            }
        });

    }

    public void displayData(final Recipe post) {

        txt_recipe_title.setText(post.recipe_title);
        txt_category.setText(post.category_name);
        txt_recipe_time.setText(post.recipe_time);

        if (AppConfig.ENABLE_RECIPES_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(post.total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            lyt_view.setVisibility(View.GONE);
        }

        if (post.content_type != null && post.content_type.equals("Post")) {
            thumbnail_video.setVisibility(View.GONE);
        } else {
            thumbnail_video.setVisibility(View.VISIBLE);
        }

        Tools.displayPostDescription(this, webView, post.recipe_description);

        btn_share.setOnClickListener(view -> {
            String share_title = android.text.Html.fromHtml(post.recipe_title).toString();
            String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        btn_font_size.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityNotificationDetail.this);
            dialog.setTitle(getString(R.string.title_dialog_font_size));
            dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i]);
            dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                WebSettings webSettings = webView.getSettings();
                if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                    sharedPref.updateFontSize(0);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                    sharedPref.updateFontSize(1);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                    sharedPref.updateFontSize(3);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                    sharedPref.updateFontSize(4);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                } else {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                }
                dialogInterface.dismiss();
            });
            dialog.show();
        });

        addToFavorite(post);
        new Handler().postDelayed(() -> lyt_suggested.setVisibility(View.VISIBLE), 1000);

    }

    private void displaySuggested(List<Recipe> list) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        AdapterSuggested adapterSuggested = new AdapterSuggested(ActivityNotificationDetail.this, recyclerView, list);
        recyclerView.setAdapter(adapterSuggested);
        recyclerView.setNestedScrollingEnabled(false);
        adapterSuggested.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
            intent.putExtra("id", obj.recipe_id);
            startActivity(intent);
            adsManager.showInterstitialAd();
        });

        TextView txt_suggested = findViewById(R.id.txt_suggested);
        if (list.size() > 0) {
            txt_suggested.setText(getResources().getString(R.string.txt_suggested));
        } else {
            txt_suggested.setText("");
        }

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addToFavorite(Recipe post) {

        List<Recipe> data = dbHandler.getFavRow(post.recipe_id);
        if (data.size() == 0) {
            btn_favorite.setImageResource(R.drawable.ic_fav_outline);
        } else {
            if (data.get(0).getRecipe_id().equals(post.recipe_id)) {
                btn_favorite.setImageResource(R.drawable.ic_fav);
            }
        }

        btn_favorite.setOnClickListener(view -> {
            List<Recipe> data1 = dbHandler.getFavRow(post.recipe_id);
            if (data1.size() == 0) {
                dbHandler.AddtoFavorite(new Recipe(
                        post.category_name,
                        post.recipe_id,
                        post.recipe_title,
                        post.recipe_time,
                        post.recipe_image,
                        post.recipe_description,
                        post.video_url,
                        post.video_id,
                        post.content_type,
                        post.featured,
                        post.tags,
                        post.total_views
                ));
                Snackbar.make(parent_view, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                btn_favorite.setImageResource(R.drawable.ic_fav);
            } else {
                if (data1.get(0).getRecipe_id().equals(post.recipe_id)) {
                    dbHandler.RemoveFav(new Recipe(post.recipe_id));
                    Snackbar.make(parent_view, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                    btn_favorite.setImageResource(R.drawable.ic_fav_outline);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void loadViewed() {
        if (Tools.isConnect(this)) {
            new MyTask().execute(sharedPref.getApiUrl() + "/api/api.php?get_total_views&id=" + recipe_id);
        }
    }

    @SuppressWarnings("deprecation")
    private static class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return Tools.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                Log.d("TAG", "no data found!");
            } else {

                try {

                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray("result");
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
