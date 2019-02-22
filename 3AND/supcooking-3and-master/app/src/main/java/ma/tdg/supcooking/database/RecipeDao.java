package ma.tdg.supcooking.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import java.util.List;

import ma.tdg.supcooking.model.Recipe;

@Dao
public interface RecipeDao {
    @Query("SELECT * FROM recipe")
    List<Recipe> getAll();

    @Insert(onConflict = REPLACE)
    void insertAll(List<Recipe> recipes);

    @Query("DELETE FROM recipe")
    void deleteAll();
}
