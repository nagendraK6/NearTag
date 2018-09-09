package com.relylabs.around;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.around.Utils.SquareImageView;
import com.relylabs.around.comments.ViewCommentsFragment;
import com.relylabs.around.models.User;
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
public class NewsFeedAdapter extends
        RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

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
        public TextView tag;
        public SquareImageView banngerImage;
        public CircleImageView profilePicURL, creator_profile_pic;
        public ProgressBar uploadingFile;
        public TextView upload_in_progress_text;
        public TextView userPostText;
        public ImageView like_icon;
        public ImageView share_button;
        public ImageView comment_button;
        public TextView user_name;
        public ProgressBar busy;
        public TextView post_creator_name;
        public TextView put_comment;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            //tag = itemView.findViewById(R.id.tag);
            banngerImage = itemView.findViewById(R.id.banner_image);
            profilePicURL = itemView.findViewById(R.id.user_profile_image);
            uploadingFile = itemView.findViewById(R.id.progress);
            upload_in_progress_text = itemView.findViewById(R.id.upload_in_progress_text);
            userPostText = itemView.findViewById(R.id.comment_text);
            like_icon = itemView.findViewById(R.id.like_icon);
            share_button = itemView.findViewById(R.id.whatsapp_sharing);
            comment_button = itemView.findViewById(R.id.comment_icon);
            user_name = itemView.findViewById(R.id.user_name);
            creator_profile_pic = itemView.findViewById(R.id.comment_creator_profile);
            post_creator_name = itemView.findViewById(R.id.comment_creator_name);
            put_comment = itemView.findViewById(R.id.put_comment);
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
        }


        viewHolder.user_name.setText(current_element.getUserName());

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
            setTags(viewHolder.userPostText, current_element.getUserPostText());
           // viewHolder.userPostText.setText(current_element.getUserPostText());
            viewHolder.post_creator_name.setText(current_element.getUserName() + ": ");
        } else {
            viewHolder.userPostText.setVisibility(View.GONE);
            viewHolder.post_creator_name.setVisibility(View.GONE);
        }

        if (!StringUtils.isEmpty(current_element.getBanngerImageURL())) {
            Picasso.with(getContext()).load(current_element.getBanngerImageURL())
                    .into(
                            viewHolder.banngerImage,
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

        if (current_element.getGalleryImageFile() != "") {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            File piccasso_file = new File(current_element.getGalleryImageFile());
            Picasso.with(getContext()).load(piccasso_file).
                    into(
                            viewHolder.banngerImage,
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
                viewHolder.banngerImage.setAlpha((float) 1);
                viewHolder.profilePicURL.setAlpha((float) 1);
                viewHolder.tag.setAlpha((float) 1);
              //  viewHolder.uploadingFile.setVisibility(View.GONE);
                viewHolder.upload_in_progress_text.setVisibility(View.GONE);
            }
        }

        if (current_element.getHasPublished()) {
            viewHolder.upload_in_progress_text.setVisibility(View.GONE);
        } else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            viewHolder.upload_in_progress_text.setVisibility(View.VISIBLE);
            viewHolder.banngerImage.setAlpha((float) 0.5);
            viewHolder.profilePicURL.setAlpha((float) 0.5);
          //  viewHolder.tag.setAlpha((float) 0.5);
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
        mVisibilityTracker.addView(viewHolder.itemView, 50);
        viewHolder.share_button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                viewHolder.itemView.setDrawingCacheEnabled(true);

                viewHolder.itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                viewHolder.itemView.layout(0, 0, viewHolder.itemView.getMeasuredWidth(), viewHolder.itemView.getMeasuredHeight());

                viewHolder.itemView.buildDrawingCache(true);
                Bitmap b = Bitmap.createBitmap(viewHolder.itemView.getDrawingCache());
                viewHolder.itemView.setDrawingCacheEnabled(false); // clear drawing cache


                Bitmap bitmap2 = b.copy(b.getConfig(), false);

                String filename = "bitmap.png";



                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/req_images");
                myDir.mkdirs();
                File file = new File(myDir, filename);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                viewHolder.banngerImage.setDrawingCacheEnabled(false);
                //File f = new File(filename);

                Uri apkURI = FileProvider.getUriForFile(
                        getContext(),
                        getContext()
                                .getPackageName() + ".provider", file);

                Uri imageUri = Uri.fromFile(myDir);


                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, apkURI);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                boolean installed_whatsapp = appInstalledOrNot("com.whatsapp", getContext());
                if (installed_whatsapp) {
                    shareIntent.setPackage("com.whatsapp");
                }
                getContext().startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        viewHolder.comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putInt("post_id", current_element.getPostId());
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
                Fragment frg = new ViewCommentsFragment();
                frg.setArguments(data_bundle);
                loadFragment(frg);
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

    private void setTags(TextView pTextView, String pTagString) {
        SpannableString string = new SpannableString(pTagString);

        int start = -1;
        for (int i = 0; i < pTagString.length(); i++) {
            if (pTagString.charAt(i) == '#') {
                start = i;
            } else if (pTagString.charAt(i) == ' ' || (i == pTagString.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == pTagString.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    final String tag = pTagString.substring(start, i);
                    string.setSpan(new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            Log.d("Hash", String.format("Clicked %s!", tag));
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            // link color
                            ds.setColor(Color.parseColor("#003569"));
                            ds.setUnderlineText(false);
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                }
            }
        }

        pTextView.setMovementMethod(LinkMovementMethod.getInstance());
        pTextView.setText(string);
    }
}