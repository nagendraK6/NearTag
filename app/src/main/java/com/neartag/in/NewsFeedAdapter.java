package com.neartag.in;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.R;
import com.neartag.in.Utils.Helper;
import com.neartag.in.Utils.SquareImageView;
import com.neartag.in.Utils.TimeAgo;
import com.neartag.in.comments.ViewCommentsFragment;
import com.neartag.in.models.NewsFeedElement;
import com.neartag.in.models.User;
import com.neartag.in.webview.WebviewFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    private  VisibilityTracker mVisibilityTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();

    // Store a member variable for the contacts
    private List<NewsFeedElement> news_feed_elements;
    // Store the context for easy access
    private Context mContext;

    private AppCompatActivity activity;
    View feed_view;
    User user;
    // Pass in the contact array into the constructor
    public NewsFeedAdapter(AppCompatActivity activity, Context context, List<NewsFeedElement> all_feed_elements) {
        mVisibilityTracker = new VisibilityTracker(activity);
        mVisibilityTracker.setVisibilityTrackerListener(new VisibilityTracker.VisibilityTrackerListener() {
            @Override
            public void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews) {
                handleVisibleViews(visibleViews);
            }
        });

        news_feed_elements = all_feed_elements;
        mContext = context;
        this.activity = activity;
        this.user = User.getLoggedInUser();
    }

    private void handleVisibleViews(List<View> visibleViews) {
        Log.d(NewsFeedAdapter.class.getSimpleName(), "Currently visible views \n");
        for (View v : visibleViews) {
            Integer viewPosition = mViewPositionMap.get(v);
            if (viewPosition < news_feed_elements.size()) {
                news_feed_elements.get(viewPosition).incrementDuration();
                if (news_feed_elements.get(viewPosition).shouldShowComments()) {
                    v.findViewById(R.id.add_ur_comment).setVisibility(View.VISIBLE);
                }
            }
            Log.d("debug_data", "VP " + viewPosition.toString());
        }
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView tag, empty_creator_profile_pic;
        private SquareImageView bannerImage;
        private ImageView banner_image_width_gt;
        private CircleImageView profilePicURL, creator_profile_pic;
        private ProgressBar uploadingFile;
        private TextView upload_in_progress_text;
        private TextView userPostText;
        private ImageView like_icon;
        private ImageView share_button;
        private ImageView comment_button;
        private TextView user_name, time_ago, status_bar, user_location, title, post_creator_profile_image_2;
        private ProgressBar busy;
        private TextView post_creator_name;
        private TextView put_comment, learn_more;
        private View shared_content_view, logo, action_section, learn_more_view;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
            title = itemView.findViewById(R.id.title);
            banner_image_width_gt = itemView.findViewById(R.id.banner_image_width_gt);
            profilePicURL = itemView.findViewById(R.id.user_profile_image);
            uploadingFile = itemView.findViewById(R.id.progress);
            upload_in_progress_text = itemView.findViewById(R.id.upload_in_progress_text);
            userPostText = itemView.findViewById(R.id.comment_text);
            like_icon = itemView.findViewById(R.id.like_icon);
            share_button = itemView.findViewById(R.id.whatsapp_sharing);
            comment_button = itemView.findViewById(R.id.comment_icon);
            user_name = itemView.findViewById(R.id.user_name);
            user_location = itemView.findViewById(R.id.user_location);
            creator_profile_pic = itemView.findViewById(R.id.comment_creator_profile);
            empty_creator_profile_pic = itemView.findViewById(R.id.empty_comment_creator_profile);
            post_creator_name = itemView.findViewById(R.id.comment_creator_name);
            put_comment = itemView.findViewById(R.id.put_comment);
            time_ago = itemView.findViewById(R.id.time_ago);
            status_bar = itemView.findViewById(R.id.status_bar);
            shared_content_view = itemView.findViewById(R.id.center_content);
            action_section = itemView.findViewById(R.id.action_section);
            logo = itemView.findViewById(R.id.logo);
            learn_more_view = itemView.findViewById(R.id.learn_more_view);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public NewsFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        feed_view = inflater.inflate(R.layout.news_item, parent, false);
        //feed_view.setBackgroundColor(Color.RED);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(feed_view);


        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final NewsFeedAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final NewsFeedElement current_element = news_feed_elements.get(position);
        // Set item views based on your views and data model
       // final TextView tag = viewHolder.tag;
       // tag.setText(current_element.getTag());
        // Set item views based on your views and data model

        ImageView profile = viewHolder.profilePicURL;
        if (!StringUtils.isEmpty(current_element.getProfileImageURL())) {
            Picasso.with(getContext()).load(current_element.getProfileImageURL()).
                    into(
                    profile,
                    new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            //do smth when there is picture loading error
                        }
                    }
            );

        }

        if (!StringUtils.isEmpty(this.user.ProfilePicURL)) {
            Picasso.with(getContext()).load(this.user.ProfilePicURL).
                    into(
                            viewHolder.creator_profile_pic,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
        } else {
            viewHolder.empty_creator_profile_pic.setText(user.Name.substring(0, 1));
            viewHolder.empty_creator_profile_pic.setVisibility(View.VISIBLE);
        }


        viewHolder.user_name.setText(current_element.getUserName());
        viewHolder.user_location.setText(current_element.getUserLocation());

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putInt("user_id", current_element.getUserId());
                data_bundle.putString("profile_image_url", current_element.getProfileImageURL());
                data_bundle.putString("user_name", current_element.getUserName());
                Fragment frag = new UserProfileFragment();
                frag.setArguments(data_bundle);
                loadFragment(frag);
            }
        });


        if (!StringUtils.isEmpty(current_element.getUserPostText())) {
            if (!current_element.getIsSystemUser()) {
                Helper.setTags(viewHolder.userPostText, current_element.getUserPostText());
                // viewHolder.userPostText.setText(current_element.getUserPostText());
                viewHolder.post_creator_name.setText(current_element.getUserName() + " ");
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.userPostText.setVisibility(View.VISIBLE);
            } else {
                viewHolder.title.setText(current_element.getUserPostText());
                viewHolder.title.setVisibility(View.VISIBLE);
                viewHolder.userPostText.setVisibility(View.GONE);
            }
        } else {
            viewHolder.userPostText.setVisibility(View.GONE);
            viewHolder.post_creator_name.setVisibility(View.GONE);
        }


       if (!StringUtils.isEmpty(current_element.getBannerImageURLLow())) {
           if (current_element.isWidthGt()) {
               viewHolder.bannerImage.setVisibility(View.GONE);
               viewHolder.banner_image_width_gt.setVisibility(View.VISIBLE);
           } else {
               viewHolder.bannerImage.setVisibility(View.VISIBLE);
               viewHolder.banner_image_width_gt.setVisibility(View.GONE);
           }

            Picasso.with(getContext()).load(current_element.getBannerImageURLLow())
                    .placeholder(R.color.light_transparent)
                    .into(
                            current_element.isWidthGt() ?
                                    viewHolder.banner_image_width_gt :
                                    viewHolder.bannerImage,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    viewHolder.itemView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
       }

        if (!StringUtils.isEmpty(current_element.getBannerImageURLHigh())) {
            if (current_element.isWidthGt()) {
                viewHolder.bannerImage.setVisibility(View.GONE);
                viewHolder.banner_image_width_gt.setVisibility(View.VISIBLE);
            } else {
                viewHolder.bannerImage.setVisibility(View.VISIBLE);
                viewHolder.banner_image_width_gt.setVisibility(View.GONE);
            }
            Picasso.with(getContext()).load(current_element.getBannerImageURLHigh())
                    .placeholder(R.color.light_transparent)
                    .into(
                            current_element.isWidthGt() ?
                                    viewHolder.banner_image_width_gt :
                                    viewHolder.bannerImage,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    viewHolder.itemView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
        }


        if (!StringUtils.isEmpty(current_element.getGalleryImageFile())) {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            File piccasso_file = new File(current_element.getGalleryImageFile());
            if (current_element.isWidthGt()) {
                viewHolder.bannerImage.setVisibility(View.GONE);
                viewHolder.banner_image_width_gt.setVisibility(View.VISIBLE);
            } else {
                viewHolder.bannerImage.setVisibility(View.VISIBLE);
                viewHolder.banner_image_width_gt.setVisibility(View.GONE);
            }
            Picasso.with(getContext()).load(piccasso_file).
                    into(
                            current_element.isWidthGt() ?
                                    viewHolder.banner_image_width_gt :
                                    viewHolder.bannerImage,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("debug_data", "loading from local file");
                                }


                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
            if(current_element.getHasPublished()) {
                viewHolder.bannerImage.setAlpha((float) 1);
                viewHolder.profilePicURL.setAlpha((float) 1);
                viewHolder.upload_in_progress_text.setVisibility(View.GONE);
            }
        }

        if (current_element.getHasPublished()) {
            viewHolder.upload_in_progress_text.setVisibility(View.GONE);
        } else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            viewHolder.upload_in_progress_text.setVisibility(View.VISIBLE);
            viewHolder.bannerImage.setAlpha((float) 0.5);
            viewHolder.profilePicURL.setAlpha((float) 0.5);
            viewHolder.uploadingFile.setVisibility(View.VISIBLE);
        }


        if (current_element.getHasLiked()) {
            viewHolder.like_icon.setImageResource(R.mipmap.ic_heart_red_like);
        } else {
            viewHolder.like_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.like_icon.setImageResource(R.mipmap.ic_heart_red_like);
                    markPostLike(current_element.getPostId());
                }
            });
        }


        mViewPositionMap.put(viewHolder.itemView, position);
        mVisibilityTracker.addView(viewHolder.itemView, 80);
        viewHolder.share_button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                if (hasPermission(getContext())) {
                    startSharing(viewHolder, current_element);
                } else {
                    if (waListener != null) {
                        waListener.onClickWA(viewHolder, current_element);
                    }
                }
            }
        });

        viewHolder.comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putInt("post_id", current_element.getPostId());
                data_bundle.putString("post_creator_profile_image", current_element.getProfileImageURL());
                data_bundle.putString("post_creator_name", current_element.getUserName());
                data_bundle.putString("post_message", current_element.getUserPostText());

                Fragment frg = new ViewCommentsFragment();
                frg.setArguments(data_bundle);
                loadFragment(frg);
            }
        });

        viewHolder.put_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putInt("post_id", current_element.getPostId());
                data_bundle.putString("post_creator_profile_image", current_element.getProfileImageURL());
                data_bundle.putString("post_creator_name", current_element.getUserName());
                data_bundle.putString("post_message", current_element.getUserPostText());
                Fragment frg = new ViewCommentsFragment();
                frg.setArguments(data_bundle);
                loadFragment(frg);
            }
        });

        String time_ago_text = TimeAgo.getTimeAgo(current_element.getTimeStamp());
        if (StringUtils.isEmpty(time_ago_text)) {
            viewHolder.time_ago.setVisibility(View.GONE);
        } else {
            viewHolder.time_ago.setText(time_ago_text);
        }

        String stats_text = getStatsString(current_element);
        if (StringUtils.isEmpty(stats_text)) {
            viewHolder.status_bar.setVisibility(View.GONE);
        } else {
            viewHolder.status_bar.setText(stats_text);
        }

        if (StringUtils.isEmpty(current_element.getLearnMoreLink())) {
            viewHolder.learn_more_view.setVisibility(View.GONE);
        } else {
            viewHolder.learn_more_view.setVisibility(View.VISIBLE);
        }

        TextView lt =  viewHolder.learn_more_view.findViewById(R.id.learn_more);
        lt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bndle = new Bundle();
                bndle.putString("url", current_element.getLearnMoreLink());
                Fragment fr = new WebviewFragment();
                fr.setArguments(bndle);
                loadFragment(fr);
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return news_feed_elements.size();
    }

    private void markPostLike(Integer post_id) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("post_id", post_id.toString());
        User user = User.getLoggedInUser();

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("debug_data", "liked");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Log.d("debug_data", "liked failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                Log.d("debug_data", "liked failed");
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "post/like", params, response_json);
    }

    private boolean appInstalledOrNot(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    private String getStatsString(NewsFeedElement current_element) {
        String status = "";
        if (!StringUtils.isEmpty(current_element.getLikesCount())) {
            status = current_element.getLikesCount() + " पसंद . ";
        }

        if (!StringUtils.isEmpty(current_element.getSharesCount())) {
            status += current_element.getSharesCount() + " शेयर . ";
        }

        if (!StringUtils.isEmpty(current_element.getCommentsCount())) {
            status += current_element.getCommentsCount() + " राय";
        }

        if (StringUtils.isEmpty(status)) {
            status = "सबसे पहले पसंद करने वाले बनिये \uD83D\uDE42";
        }

        return status;
    }

    public void OnSharingCallback(Integer post_id) {
        // update server to increment the share count
        postShareCountIncrement(post_id);
    }

    private void postShareCountIncrement(Integer post_id) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("post_id", post_id.toString());
        User user = User.getLoggedInUser();

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("debug_data", "liked");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Log.d("debug_data", "liked failed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                Log.d("debug_data", "liked failed");
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "post/share", params, response_json);
    }

    public boolean hasPermission(Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void startSharing(ViewHolder viewHolder, NewsFeedElement current_element) {
        viewHolder.status_bar.setVisibility(View.INVISIBLE);
        viewHolder.logo.setVisibility(View.VISIBLE);
        TextView reporter_name = viewHolder.logo.findViewById(R.id.reporter_name);
        reporter_name.setText(current_element.getUserName());
        viewHolder.action_section.setVisibility(View.INVISIBLE);
        viewHolder.learn_more_view.setVisibility(View.GONE);


        viewHolder.shared_content_view.invalidate();
        viewHolder.shared_content_view.setDrawingCacheEnabled(true);
        viewHolder.shared_content_view.measure(View.MeasureSpec.makeMeasureSpec(
                viewHolder.shared_content_view.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(viewHolder.shared_content_view.getHeight(), View.MeasureSpec.EXACTLY));

        viewHolder.shared_content_view.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(viewHolder.shared_content_view.getDrawingCache());
        viewHolder.shared_content_view.setDrawingCacheEnabled(false); // clear drawing cache

        viewHolder.status_bar.setVisibility(View.VISIBLE);
        viewHolder.logo.setVisibility(View.INVISIBLE);
        viewHolder.action_section.setVisibility(View.VISIBLE);
        if (!StringUtils.isEmpty(current_element.getLearnMoreLink())) {
            viewHolder.learn_more_view.setVisibility(View.VISIBLE);
        }


        Bitmap bitmap2 = b.copy(b.getConfig(), false);

        String filename = "bitmap.png";



        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/tmp");
        myDir.mkdirs();
        File file = new File(myDir, filename);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap2.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri apkURI = FileProvider.getUriForFile(
                getContext(),
                getContext().getApplicationContext()
                        .getPackageName() + ".provider", file);


        Uri imageUri = Uri.fromFile(file);


        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, apkURI);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        boolean installed_whatsapp = appInstalledOrNot("com.whatsapp", getContext());
        if (installed_whatsapp) {
            shareIntent.setPackage("com.whatsapp");
        }

        ((AppCompatActivity)getContext()).startActivityForResult(shareIntent, current_element.getPostId());
    }

    public interface ClickWhatsApp {
        void onClickWA(ViewHolder viewHolder, NewsFeedElement current_element);
    }

    public void setWAListener(ClickWhatsApp listener) {
        this.waListener = listener;
    }

    ClickWhatsApp waListener;
}