package com.relylabs.around.comments;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.relylabs.around.NewsFeedAdapter;
import com.relylabs.around.NewsFeedElement;
import com.relylabs.around.R;
import com.relylabs.around.User;
import com.relylabs.around.VisibilityTracker;
import com.relylabs.around.models.Comment;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 9/8/18.
 */

public class CommentListAdapter  extends
        RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    List<Comment> all_comments;

    private Context mContext;
     private  User user;
    private AppCompatActivity activity;



    public CommentListAdapter(AppCompatActivity activity, Context context, List<Comment> all_comments) {
        this.all_comments = all_comments;
        mContext = context;
        this.activity = activity;
        this.user = User.getLoggedInUser();
    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            username = (TextView) itemView.findViewById(R.id.comment_username);
            timestamp = (TextView) itemView.findViewById(R.id.comment_time_posted);
            reply = (TextView) itemView.findViewById(R.id.comment_reply);
            like = (ImageView) itemView.findViewById(R.id.comment_like);
            likes = (TextView) itemView.findViewById(R.id.comment_likes);
            profileImage = (CircleImageView) itemView.findViewById(R.id.comment_profile_image);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CommentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View comment_view = inflater.inflate(R.layout.layout_comment, parent, false);
        //feed_view.setBackgroundColor(Color.RED);

        // Return a new holder instance
        CommentListAdapter.ViewHolder viewHolder = new CommentListAdapter.ViewHolder(comment_view);


        return viewHolder;
    }


    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final CommentListAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final Comment current_comment = this.all_comments.get(position);
        viewHolder.comment.setText(current_comment.getCommentText());
        viewHolder.username.setText(current_comment.getUserName());
        Picasso.with(mContext).load(current_comment.getCommenterProfilePicUrl()).into(
                viewHolder.profileImage
        );
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Comment comment){
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDateCreated();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return all_comments.size();
    }
}
