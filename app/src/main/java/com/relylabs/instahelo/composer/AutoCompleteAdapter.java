package com.relylabs.instahelo.composer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.HttpGet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.instahelo.App;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.Utils.Logger;
import com.relylabs.instahelo.models.Tag;
import com.relylabs.instahelo.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by nagendra on 9/17/18.
 */

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    ArrayList<String> items;
    Context context;
    private CursorPositionListener listener;
    private HashTagFilter filter;
    public AutoCompleteAdapter(Context context, int textViewResourceId){
        super(context, textViewResourceId);
        this.items = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount(){
        return items.size();
    }

    @Override
    public String getItem(int index){
        return items.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_row, parent, false);
        }

        TextView suggestionElement = view.findViewById(R.id.suggestionElement);
        suggestionElement.setText(items.get(position));
        return view;
    }

    public interface CursorPositionListener {
        int currentCursorPosition();
    }

    public void setCursorPositionListener(CursorPositionListener listener) {
        this.listener = listener;
    }

    @Override
    public Filter getFilter(){

        if (filter == null) {
            filter = new HashTagFilter();
        }

        return filter;

    }

    public class HashTagFilter extends Filter {

        private final Pattern pattern = Pattern.compile("[#＃]([Ａ-Ｚａ-ｚ\u0900-\u097FA-Za-z一-\u9FC60-9０-９ぁ-ヶｦ-ﾟー])+");

        public int start;
        public int end;

        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            FilterResults filterResults = new FilterResults();
            items.clear();
            if(constraint != null) {
                Matcher m = pattern.matcher(constraint.toString());
                int cursorPosition = listener.currentCursorPosition();
                while (m.find()) {
                    if (m.start() < cursorPosition && cursorPosition <= m.end()) {

                        start = m.start();
                        end = m.end();

                        String keyword = constraint.subSequence(m.start() + 1, m.end()).toString();
                        if (keyword.length() >= 1) {
                            items = Tag.getTagsString(keyword);
                            if (items.size() == 0) {
                                Intent intent=new Intent("on_message");
                                Bundle bundle = new Bundle();
                                bundle.putString("enable", "1");
                                intent.putExtras(bundle);
                                context.sendBroadcast(intent);
                                items = fetchRecommendedList(keyword);

                                bundle.putString("enable", "0");
                                intent.putExtras(bundle);
                                context.sendBroadcast(intent);
                            } else {
                                getTagsAsync(keyword);
                            }
                        }
                    }
                }
            }

            filterResults.values = items;
            filterResults.count = items.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            if(results != null && results.count > 0) {
                notifyDataSetChanged();
            }
            else {
                notifyDataSetInvalidated();
            }
        }
    }

    private ArrayList<String> fetchRecommendedList(String keyword)  {
        Logger.log(Logger.RECOMMENDED_LIST_FETCH_START);
        HttpClient client = HttpClientBuilder.create().build();
        URIBuilder builder = null;
        HttpGet request = null;
        try {
            builder = new URIBuilder(App.getBaseURL() + "post/getSuggestedTags");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        builder.setParameter("keyword", keyword);

        try {
            request = new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        User user = User.getLoggedInUser();
        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", "Bearer " + user.AccessToken);
        HttpResponse response = null;
        String json = null;
        ArrayList<String> all_tags = new ArrayList<>();
        try {
            response = client.execute(request);
            json = EntityUtils.toString(response.getEntity());
            JSONObject server_d = new JSONObject(json);
            JSONArray all_tags_data = server_d.getJSONArray("data");
            if (all_tags_data.length() > 0) {
                for (int i = 0; i < all_tags_data.length(); i++) {
                    JSONObject obj = all_tags_data.getJSONObject(i);
                    String tag_text = "#" + obj.getString("Name");
                    Tag tag = new Tag();
                    tag.Name = obj.getString("Name");
                    tag.save();
                    all_tags.add(tag_text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return all_tags;
    }


    private void getTagsAsync(String keyword) {
        Logger.log(Logger.NEWS_FEED_FETCH_START);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("keyword", keyword);
        User user = User.getLoggedInUser();
        // response

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("debug_keyword", "fetched");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "post/getSuggestedTags", params, response_json);
    }
}