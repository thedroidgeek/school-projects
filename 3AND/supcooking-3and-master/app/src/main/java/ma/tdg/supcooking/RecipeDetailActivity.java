package ma.tdg.supcooking;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.util.ServerApi;

public class RecipeDetailActivity extends AppCompatActivity {

    private RatingBar mRatingBar;
    private TextView mRatingLabel;
    private Recipe mRecipe;
    private AsyncTask mLastTask;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
        final boolean isUserRecipe = getIntent().getBooleanExtra("is_user_recipe", false);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecipeEditActivity.class);
                intent.putExtra("recipe", mRecipe);
                startActivity(intent);
                finish();
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRatingBar = findViewById(R.id.detail_rating);
        mRatingLabel = findViewById(R.id.detail_rating_label);
        final ImageView recipeBanner = findViewById(R.id.image_recipe_detail);
        TextView textType = findViewById(R.id.detail_type);
        TextView textCookingTime = findViewById(R.id.detail_cooking_time);
        TextView textPrepTime = findViewById(R.id.detail_prep_time);
        TextView textIngredients = findViewById(R.id.detail_ingredients);
        TextView textPrepSteps = findViewById(R.id.detail_prep_steps);

        if (isUserRecipe) {
            mRatingBar.setVisibility(View.GONE);
            mRatingLabel.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.GONE);
            mRatingLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowRatingDialog();
                }
            });
            mRatingBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        ShowRatingDialog();
                    }
                    return true;
                }
            });
        }


        // populating ui elements

        Objects.requireNonNull(getSupportActionBar()).setTitle(mRecipe.getName());

        if (mRecipe.getPictureLocation() != null && !mRecipe.getPictureLocation().equals("null")) {
            Picasso.get().load(mRecipe.getPictureLocation())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(recipeBanner, new Callback() {
                        @Override
                        public void onSuccess() { }
                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(mRecipe.getPictureLocation())
                                    .into(recipeBanner);
                        }
                    });
        }

        mRatingBar.setRating(mRecipe.getRating());
        textType.setText(mRecipe.getType());
        textCookingTime.setText(mRecipe.getCookingTimeFormatted());
        textPrepTime.setText(mRecipe.getPreparationTimeFormatted());
        textIngredients.setText(mRecipe.getIngredients());
        textPrepSteps.setText(mRecipe.getPreparationSteps());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ShowRatingDialog() {
        // looks blue on api 19 (programmatically applying appcompat style only makes the rating bar blank for some reason)
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(RecipeDetailActivity.this);
        LinearLayout linearLayout = new LinearLayout(RecipeDetailActivity.this);
        final RatingBar rating = new RatingBar(RecipeDetailActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 32, 0, 0);
        linearLayout.setGravity(Gravity.CENTER);
        rating.setLayoutParams(lp);
        rating.setNumStars(5);
        rating.setStepSize(1);
        linearLayout.addView(rating);
        popDialog.setTitle("Rate " + mRecipe.getName());
        popDialog.setView(linearLayout);
        popDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                public void onClick(final DialogInterface dialog, int which) {
                    if (mLastTask == null || mLastTask.getStatus() == AsyncTask.Status.FINISHED) {
                        mLastTask = new AsyncTask<Void, Void, Void>() {
                            private String mErrorMessage;
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    ServerApi.evaluateRecipe(mRecipe, rating.getProgress());
                                } catch (Exception e) {
                                    mErrorMessage = e.getMessage();
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void v) {
                                Snackbar.make(findViewById(R.id.fab), mErrorMessage == null ? "Review submitted!" : mErrorMessage, Snackbar.LENGTH_LONG).show();
                                if (mErrorMessage == null) {
                                    mRatingLabel.setText(R.string.label_review_by_user);
                                    mRatingBar.setRating(rating.getProgress());
                                }
                                dialog.dismiss();
                            }
                        }.execute();
                    }
                }})
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                });
        popDialog.create();
        popDialog.show();
    }
}
