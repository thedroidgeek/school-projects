package ma.tdg.supcooking.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.model.UserRecipe;

@Database(entities = {Recipe.class, UserRecipe.class}, version = 1, exportSchema = false)
public abstract class SupCookingDb extends RoomDatabase {
    public abstract RecipeDao recipeDao();
    public abstract UserRecipeDao userRecipeDao();

    private static SupCookingDb sInstance;

    static SupCookingDb getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (SupCookingDb.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            SupCookingDb.class, "supcooking_db").build();
                }
            }
        }
        return sInstance;
    }
}
