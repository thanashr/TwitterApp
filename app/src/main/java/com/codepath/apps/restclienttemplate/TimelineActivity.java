package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.ImageView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;

public class TimelineActivity extends AppCompatActivity {
    public static final String TAG ="TimelineActivity";
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

//
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
//        ImageView imageView = new ImageView(actionBar.getThemedContext());
//        imageView.setScaleType(ImageView.ScaleType.CENTER);
//        imageView.setImageResource(R.drawable.ic_launcher_twitter);
//        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams( ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
//        layoutParams.rightMargin = 40;
//        imageView.setLayoutParams(layoutParams); actionBar.setCustomView(imageView);

        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                @Override
                                                public void onRefresh() {
                                                    Log.i(TAG,"fetching new data");
                                                    populateHomeTimeline();
                                                    // Your code to refresh the list here.
                                                    // Make sure you call swipeContainer.setRefreshing(false)
                                                    // once the network request has completed successfully.

                                                }
                                            });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this,tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, HORIZONTAL);
        rvTweets.addItemDecoration(itemDecor);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG,"onLoadMore" + page);
                loadMoreData();
            }
        };
        rvTweets.addOnScrollListener(scrollListener);
        populateHomeTimeline();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main , menu);
        return super.onCreateOptionsMenu(menu);

    }
    public void loadMoreData(){
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"On Success for load more data" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try{
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch(JSONException e){
                    e.printStackTrace();

                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"On failure load more data" ,throwable);
            }
        }, tweets.get(tweets.size()-1).id);
    }
    private void populateHomeTimeline(){
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"On Success" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try{
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG,"Json Exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"On Failure");
            }
        });
    }

}