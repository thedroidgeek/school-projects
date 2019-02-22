package ma.tdg.supcooking;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.List;

import ma.tdg.supcooking.model.Recipe;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> implements Filterable {

    private Context mContext;
    private List<Recipe> mUnfilteredRecipes;
    private List<Recipe> mRecipes;
    private boolean mIsUserRecipes;

    RecipeListAdapter(List recipes, boolean isUserRecipes) {
        mRecipes = new ArrayList<>();
        mIsUserRecipes = isUserRecipes;
        if (recipes != null) {
            for (Object obj : recipes) {
                if (obj instanceof Recipe) {
                    mRecipes.add((Recipe) obj);
                }
            }
        }
        mUnfilteredRecipes = new ArrayList<>();
        mUnfilteredRecipes.addAll(mRecipes);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView mRecipeName;
        TextView mRecipeType;
        TextView mRecipeDuration;
        ImageView mRecipePict;
        RatingBar mRecipeRating;

        RecipeViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);
            mRecipeName = itemView.findViewById(R.id.card_recipe_name);
            mRecipeType = itemView.findViewById(R.id.card_recipe_type);
            mRecipeDuration = itemView.findViewById(R.id.card_recipe_duration);
            mRecipePict = itemView.findViewById(R.id.card_recipe_picture);
            mRecipeRating = itemView.findViewById(R.id.card_recipe_rating);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(mContext).inflate(R.layout.recipe_card_view, parent, false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecipeViewHolder holder, int position) {
        holder.mRecipeName.setText(mRecipes.get(position).getName());
        holder.mRecipeType.setText(mRecipes.get(position).getType());
        holder.mRecipeDuration.setText(mRecipes.get(position).getFullDurationFormatted());
        if (mIsUserRecipes) {
            holder.mRecipeRating.setVisibility(View.GONE);
        } else {
            holder.mRecipeRating.setRating(mRecipes.get(position).getRating());
        }
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RecipeDetailActivity.class);
                intent.putExtra("recipe", mRecipes.get(holder.getAdapterPosition()));
                intent.putExtra("is_user_recipe", mIsUserRecipes);
                mContext.startActivity(intent);
            }
        });
        final int thumbSizeInPx = (int) (72 * Resources.getSystem().getDisplayMetrics().density);
        String pictureLocation = mRecipes.get(position).getPictureLocation();
        if (pictureLocation != null && !pictureLocation.equals("null")) {
            Picasso.get().load(mRecipes.get(position).getPictureLocation())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .resize(thumbSizeInPx, thumbSizeInPx)
                    .centerCrop().error(R.drawable.no_image_available)
                    .into(holder.mRecipePict, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError(Exception e) {
                    Picasso.get()
                            .load(mRecipes.get(holder.getAdapterPosition()).getPictureLocation())
                            .resize(thumbSizeInPx, thumbSizeInPx)
                            .centerCrop().error(R.drawable.no_image_available)
                            .into(holder.mRecipePict);
                }
            });
        }
        else {
            Picasso.get().load(R.drawable.no_image_available)
                    .resize(thumbSizeInPx, thumbSizeInPx)
                    .centerCrop().into(holder.mRecipePict);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView view) {
        super.onAttachedToRecyclerView(view);
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mRecipes.clear();
                mRecipes.addAll((List<Recipe>) results.values);
                notifyDataSetChanged();
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Recipe> filteredResults;
                if (constraint.length() == 0) {
                    filteredResults = mUnfilteredRecipes;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                results.count = filteredResults.size();
                return results;
            }
        };
    }

    private List<Recipe> getFilteredResults(String constraint) {
        List<Recipe> results = new ArrayList<>();
        for (Recipe r : mUnfilteredRecipes) {
            if (r.getName().toLowerCase().contains(constraint)) {
                results.add(r);
            }
        }
        return results;
    }
}
