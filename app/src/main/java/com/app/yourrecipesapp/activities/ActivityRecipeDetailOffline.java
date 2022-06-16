package com.app.yourrecipesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.app.yourrecipesapp.BuildConfig;
import com.app.yourrecipesapp.R;
import com.app.yourrecipesapp.config.AppConfig;
import com.app.yourrecipesapp.databases.prefs.SharedPref;
import com.app.yourrecipesapp.databases.sqlite.DbHandler;
import com.app.yourrecipesapp.models.Recipe;
import com.app.yourrecipesapp.utils.AppBarLayoutBehavior;
import com.app.yourrecipesapp.utils.Constant;
import com.app.yourrecipesapp.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivityRecipeDetailOffline extends AppCompatActivity {

    private Recipe post;
    TextView txt_recipe_title, txt_category, txt_recipe_time, txt_total_views;
    LinearLayout lyt_view;
    ImageView thumbnail_video;
    ImageView recipe_image;
    private WebView webView;
    DbHandler databaseHandler;
    CoordinatorLayout parent_view;
    SharedPref sharedPref;
    ImageButton btn_font_size, btn_favorite, btn_share;
    private String singleChoiceSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_recipe_detail_offline);

        sharedPref = new SharedPref(this);
        databaseHandler = new DbHandler(this);
        Tools.setNavigation(this);

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        parent_view = findViewById(R.id.lyt_content);

        thumbnail_video = findViewById(R.id.thumbnail_video);
        recipe_image = findViewById(R.id.recipe_image);
        txt_recipe_title = findViewById(R.id.recipe_title);
        txt_category = findViewById(R.id.category_name);
        txt_recipe_time = findViewById(R.id.recipe_time);
        webView = findViewById(R.id.recipe_description);
        txt_total_views = findViewById(R.id.total_views);
        lyt_view = findViewById(R.id.lyt_view_count);

        btn_font_size = findViewById(R.id.btn_font_size);
        btn_favorite = findViewById(R.id.btn_favorite);
        btn_share = findViewById(R.id.btn_share);

        post = (Recipe) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        initToolbar();
        initFavorite();
        displayData();


    }

    public void displayData() {

        txt_recipe_title.setText(post.recipe_title);
        txt_category.setText(post.category_name);
        txt_recipe_time.setText(post.recipe_time);

        if (AppConfig.ENABLE_RECIPES_VIEW_COUNT) {
            txt_total_views.setText(Tools.withSuffix(post.total_views) + " " + getResources().getString(R.string.views_count));
        } else {
            lyt_view.setVisibility(View.GONE);
        }

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMAGE_FRONT + post.video_id + Constant.YOUTUBE_IMAGE_BACK_MQ)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(recipe_image);
        } else {
            Picasso.get()
                    .load(sharedPref.getApiUrl() + "/upload/" + post.recipe_image)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(recipe_image);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityRecipeDetailOffline.this);
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

        addToFavorite();

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

    public void initFavorite() {
        List<Recipe> data = databaseHandler.getFavRow(post.recipe_id);
        if (data.size() == 0) {
            btn_favorite.setImageResource(R.drawable.ic_fav_outline);
        } else {
            if (data.get(0).getRecipe_id().equals(post.recipe_id)) {
                btn_favorite.setImageResource(R.drawable.ic_fav);
            }
        }
    }

    public void addToFavorite() {

        btn_favorite.setOnClickListener(view -> {
            List<Recipe> data1 = databaseHandler.getFavRow(post.recipe_id);
            if (data1.size() == 0) {
                databaseHandler.AddtoFavorite(new Recipe(
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
                    databaseHandler.RemoveFav(new Recipe(post.recipe_id));
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

}
