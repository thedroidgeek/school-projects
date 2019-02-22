package ma.tdg.supcooking.model;

import android.app.Application;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ma.tdg.supcooking.util.ServerApi;
import ma.tdg.supcooking.database.RecipeRepository;

@Entity(tableName = "recipe")
public class Recipe implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;
    @ColumnInfo(name = "name")
    private String mName;
    @ColumnInfo(name = "type")
    private String mType;
    @ColumnInfo(name = "cooking_time")
    private int mCookingTime;
    @ColumnInfo(name = "prep_time")
    private int mPreparationTime;
    @ColumnInfo(name = "ingredients")
    private String mIngredients;
    @ColumnInfo(name = "prep_steps")
    private String mPreparationSteps;
    @ColumnInfo(name = "rating")
    private int mRating;
    @ColumnInfo(name = "picture_path")
    private String mPictureLocation;

    private static List<Recipe> sRecipes;
    private static List<UserRecipe> sUserRecipes;

    private static RecipeRepository sRepo;


    public static void initDataRepo(Application application) {
        if (sRepo == null) {
            sRepo = new RecipeRepository(application);
        }
    }

    public static List<Recipe> getRecipes() throws Exception {
        return getRecipes(false);
    }

    public static List<Recipe> getRecipes(boolean forceRemoteFetch) throws Exception {
        if (sRecipes == null || forceRemoteFetch) {
            if (User.isLoggedIn() && ServerApi.testConnection()) {
                sRecipes = ServerApi.fetchRecipes();
                sRepo.updateAllRecipes(sRecipes);
            } else if (sRepo != null) {
                sRecipes = sRepo.getAllRecipes();
                if (sRecipes != null && sRecipes.size() == 0) {
                    sRecipes = null;
                    return new ArrayList<>();
                }
            }
        }
        return sRecipes;
    }

    public static void addOrModifyUserRecipe(UserRecipe recipe) {
        if (sRepo != null) {
            sRepo.insertOrUpdateUserRecipe(recipe);
            sUserRecipes = sRepo.getAllUserRecipes();
        }
    }

    public static void deleteUserRecipe(UserRecipe recipe) {
        if (sRepo != null) {
            sRepo.deleteUserRecipe(recipe);
            sUserRecipes = sRepo.getAllUserRecipes();
        }
    }

    public static List<UserRecipe> getUserRecipes() {
        if (sUserRecipes == null && sRepo != null) {
            sUserRecipes = sRepo.getAllUserRecipes();
        }
        return sUserRecipes;
    }


    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getCookingTime() {
        return mCookingTime;
    }

    public String getCookingTimeFormatted() {
        String formatted = "";
        if (mCookingTime / 60 > 0) {
            formatted += mCookingTime / 60 + "h ";
        }
        if (mCookingTime % 60 > 0) {
            formatted += mCookingTime % 60 + "m";
        }
        if (formatted.isEmpty()) {
            return "--";
        }
        return formatted;
    }

    public void setCookingTime(int cookingTime) {
        mCookingTime = cookingTime;
    }

    public int getPreparationTime() {
        return mPreparationTime;
    }

    public String getPreparationTimeFormatted() {
        String formatted = "";
        if (mPreparationTime / 60 > 0) {
            formatted += mPreparationTime / 60 + "h ";
        }
        if (mPreparationTime % 60 > 0) {
            formatted += mPreparationTime % 60 + "m";
        }
        if (formatted.isEmpty()) {
            return "--";
        }
        return formatted;
    }

    public String getFullDurationFormatted() {
        int duration = mPreparationTime + mCookingTime;
        String formatted = "";
        if (duration / 60 > 0) {
            formatted += duration / 60 + "h ";
        }
        if (duration % 60 > 0) {
            formatted += duration % 60 + "m";
        }
        if (formatted.isEmpty()) {
            return "--";
        }
        return formatted;
    }

    public void setPreparationTime(int preparationTime) {
        mPreparationTime = preparationTime;
    }

    public String getIngredients() {
        return mIngredients;
    }

    public void setIngredients(String ingredients) {
        mIngredients = ingredients;
    }

    public String getPreparationSteps() {
        return mPreparationSteps;
    }

    public void setPreparationSteps(String preparationSteps) {
        mPreparationSteps = preparationSteps;
    }

    public int getRating() {
        return mRating;
    }

    public void setRating(int rating) {
        mRating = rating;
    }

    public String getPictureLocation() {
        return mPictureLocation;
    }

    public void setPictureLocation(String pictureLocation) {
        mPictureLocation = pictureLocation;
    }
}
