package ma.tdg.supcooking.database;

import android.app.Application;

import java.util.List;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.model.UserRecipe;

public class RecipeRepository {
    private RecipeDao mRecipeDao;
    private UserRecipeDao mUserRecipeDao;

    public RecipeRepository(Application application) {
        SupCookingDb db = SupCookingDb.getDatabase(application);
        mRecipeDao = db.recipeDao();
        mUserRecipeDao = db.userRecipeDao();
    }

    public List<Recipe> getAllRecipes() {
        return mRecipeDao.getAll();
    }

    public List<UserRecipe> getAllUserRecipes() {
        return mUserRecipeDao.getAll();
    }

    public void updateAllRecipes(List<Recipe> recipes) {
        mRecipeDao.deleteAll();
        mRecipeDao.insertAll(recipes);
    }

    public void insertOrUpdateUserRecipe(UserRecipe recipe) {
        mUserRecipeDao.insert(recipe);
    }

    public void deleteUserRecipe(UserRecipe recipe) {
        mUserRecipeDao.delete(recipe);
    }
}
