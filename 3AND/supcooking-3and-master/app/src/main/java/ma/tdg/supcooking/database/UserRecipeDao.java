package ma.tdg.supcooking.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ma.tdg.supcooking.model.UserRecipe;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserRecipeDao {
    @Query("SELECT * FROM user_recipe")
    List<UserRecipe> getAll();

    @Insert(onConflict = REPLACE)
    void insert(UserRecipe recipe);

    @Delete
    void delete(UserRecipe recipe);
}
